//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.folder.store.inmemory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderContentType;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.folder.store.FolderRepositoryAdapter;
import org.talend.dataprep.folder.store.NotEmptyFolderException;

@Component("folderRepository#in-memory")
@ConditionalOnProperty(name = "folder.store", havingValue = "in-memory")
public class InMemoryFolderRepository extends FolderRepositoryAdapter {

    /**
     * all folders key is the path
     */
    private Map<String, Folder> foldersMap = new ConcurrentSkipListMap<>();

    /**
     * folder entries per path
     */
    private Map<String, List<FolderEntry>> folderEntriesMap = new ConcurrentSkipListMap<>();


    /**
     * Return true if the given folder exists.
     *
     * @param givenPath the wanted folder path.
     * @return true if the given folder exists.
     */
    @Override
    public boolean exists(String givenPath) {
        String path = cleanPath(givenPath);
        // special case of the root folder. This test seems a better solution than adding the root folder to the
        // map because it does not "pollute" the folders map
        return StringUtils.equals(path, String.valueOf(PATH_SEPARATOR)) || foldersMap.containsKey(path);
    }

    @Override
    public Folder addFolder(String givenPath) {
        String path = cleanPath(givenPath);
        Folder folder = new Folder(path, extractName(path));
        foldersMap.put(path, folder);
        return folder;
    }

    @Override
    public Iterable<Folder> allFolder() {
        return foldersMap.values();
    }

    @Override
    public Iterable<Folder> children(String givenPath) {
        String path = givenPath;
        if (StringUtils.equals(path, "/")) {
            path = "";
        }
        final List<Folder> children = new ArrayList<>();
        final String cleanedPath = cleanPath(path);
        this.foldersMap.values().stream().forEach(folder -> {
            // root path need special favour...
            if (StringUtils.equals(cleanedPath, "/")) {
                if (StringUtils.countMatches(folder.getPath(), "/") < 1) {
                    children.add(folder);
                }
            } else {
                if (StringUtils.startsWith(folder.getPath(), cleanedPath)) {
                    // path asked /foo
                    // /foo/bar/beer /foo/bar
                    // /bar/beer /bar
                    // remove path start then count occurrences of /

                    String endPath = StringUtils.removeStart(folder.getPath(), cleanedPath);
                    if (StringUtils.countMatches(endPath, "/") == 1) {
                        children.add(folder);
                    }
                }
            }
        });

        return children;
    }

    @Override
    public Iterable<Folder> searchFolders(String queryString) {
        final List<Folder> children = new ArrayList<>();
        this.foldersMap.values().stream().forEach(folder -> {
            String cleanPath = cleanPath(folder.getPath());

            if (StringUtils.contains(cleanPath, '/')) {
                if (StringUtils.containsIgnoreCase(StringUtils.substringAfterLast(cleanPath, "/"), queryString)) {
                    children.add(folder);
                }
            } else {
                if (StringUtils.containsIgnoreCase(cleanPath, queryString)) {
                    children.add(folder);
                }
            }

        });

        return children;
    }

    @Override
    public void renameFolder(String path, String newPath) {
        String cleanPath = cleanPath(path);
        String cleanNewPath = cleanPath(newPath);

        this.foldersMap.keySet().forEach(key -> {
            if (StringUtils.startsWith(key, cleanPath)) {
                Folder folder = this.foldersMap.get(key);
                this.foldersMap.remove(key);
                String newFolderPath = StringUtils.replace(folder.getPath(), cleanPath, cleanNewPath, 1);
                folder.setPath(newFolderPath);
                folder.setName(extractName(folder.getPath()));
                String newKey = StringUtils.replace(key, cleanPath, cleanNewPath, 1);
                this.foldersMap.put(newKey, folder);
            }
        });

        this.folderEntriesMap.keySet().forEach(key -> {
            if (StringUtils.startsWith(key, cleanPath)) {
                List<FolderEntry> entries = this.folderEntriesMap.get(key);
                String newKey = StringUtils.replace(key, cleanPath, cleanNewPath, 1);
                this.folderEntriesMap.put(newKey, entries);
                this.folderEntriesMap.remove(key);
                entries.forEach(folderEntry -> {
                    String newFolderPath = StringUtils.replace(folderEntry.getFolderId(), cleanPath, cleanNewPath, 1);
                    folderEntry.setFolderId(newFolderPath);
                });
            }
        });
    }

    @Override
    public void clear() {
        this.foldersMap.clear();
        this.folderEntriesMap.clear();
    }

    @Override
    public FolderEntry addFolderEntry(FolderEntry folderEntry, String path) {
        folderEntry.setFolderId(cleanPath(path));
        List<FolderEntry> folderEntries = folderEntriesMap.get(folderEntry.getFolderId());
        if (folderEntries == null) {
            folderEntries = new ArrayList<>();
            folderEntriesMap.put(folderEntry.getFolderId(), folderEntries);
        }
        folderEntries.add(folderEntry);
        return folderEntry;
    }

    @Override
    public void copyFolderEntry(FolderEntry folderEntry, String destinationPath) {
        FolderEntry cloned = new FolderEntry(folderEntry.getContentType(), folderEntry.getContentId());
        cloned.setFolderId(cleanPath(destinationPath));
        this.addFolderEntry(cloned, cloned.getFolderId());
    }

    @Override
    public void moveFolderEntry(FolderEntry folderEntry, String sourcePath, String givenPath) {
        String destinationPath = cleanPath(givenPath);
        List<FolderEntry> entries = folderEntriesMap.get(cleanPath(folderEntry.getFolderId()));
        if (entries != null) {
            entries.remove(folderEntry);
        }
        folderEntry.setFolderId(destinationPath);
        addFolderEntry(folderEntry, destinationPath);
    }

    @Override
    public Iterable<FolderEntry> entries(String path, FolderContentType contentType) {
        List<FolderEntry> folderEntries = folderEntriesMap.get(cleanPath(path));
        if (folderEntries == null) {
            folderEntries = new ArrayList<>();
        }

        return folderEntries.stream().filter(e -> e.getContentType() == contentType).collect(Collectors.toList());
    }

    @Override
    public Iterable<FolderEntry> findFolderEntries(String contentId, FolderContentType contentType) {
        List<FolderEntry> entries = new ArrayList<>();

        this.folderEntriesMap.values().stream().forEach(folderEntries -> folderEntries.stream().forEach(folderEntry -> {
            if (StringUtils.equalsIgnoreCase(contentId, folderEntry.getContentId()) //
                    && contentType.equals(folderEntry.getContentType())) {
                entries.add(folderEntry);
            }
        }));

        return entries;
    }

    @Override
    public void removeFolder(String givenPath) throws NotEmptyFolderException {
        String path = cleanPath(givenPath);

        // check if any content in the tree
        // remove folder entries as well
        Iterator<String>  paths = this.folderEntriesMap.keySet().iterator();
        while (paths.hasNext()) {
            String currentPath = paths.next();
            if (StringUtils.startsWith(currentPath, path) && !this.folderEntriesMap.get( currentPath ).isEmpty()) {
                throw new NotEmptyFolderException( "The folder or a child contains data" );
            }
        }


        paths = this.foldersMap.keySet().iterator();
        while (paths.hasNext()) {
            String currentPath = paths.next();
            if (StringUtils.startsWith(currentPath, path)) {
                this.foldersMap.remove(currentPath);
            }
        }
        // remove folder entries as well
        paths = this.folderEntriesMap.keySet().iterator();
        while (paths.hasNext()) {
            String currentPath = paths.next();
            if (StringUtils.startsWith(currentPath, path)) {
                this.folderEntriesMap.remove(currentPath);
            }
        }
    }

    @Override
    public void removeFolderEntry(String givenPath, String contentId, FolderContentType contentType) {
        String folderPath = cleanPath(givenPath);
        List<FolderEntry> entries = folderEntriesMap.get(folderPath);
        final FolderEntry entry = new FolderEntry(contentType, contentId);
        entry.setFolderId(folderPath);
        entries.remove(entry);
    }

    @Override
    public long size() {
        return this.foldersMap.size();
    }


    /**
     * @see FolderRepository#locateEntry(String, FolderContentType)
     */
    @Override
    public Folder locateEntry(String contentId, FolderContentType type) {
        return folderEntriesMap.entrySet().stream() //
                .filter(e -> e.getValue().stream().filter(filterFolderEntry(contentId, type)).findFirst().orElse(null) != null) //
                .map(e -> foldersMap.get(e.getKey())) //
                // return the first (should be the only one) or null if not found.
                .findFirst().orElse(null);
    }


    /**
     * Return a filter for content id and folder entry type.
     *
     * @param contentId the wanted content id.
     * @param type the wanted folder entry type.
     * @return true if the given entry matches the content id and content type.
     */
    private Predicate<FolderEntry> filterFolderEntry(String contentId, FolderContentType type) {
        return entry -> entry.getContentId().equals(contentId) && entry.getContentType() == type;
    }


}
