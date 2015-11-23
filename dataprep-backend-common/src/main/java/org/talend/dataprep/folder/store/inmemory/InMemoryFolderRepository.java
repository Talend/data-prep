package org.talend.dataprep.folder.store.inmemory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.folder.store.FolderRepositoryAdapter;

@Component("folderRepository#in-memory")
@ConditionalOnProperty(name = "folder.store", havingValue = "in-memory", matchIfMissing = false)
public class InMemoryFolderRepository extends FolderRepositoryAdapter implements FolderRepository {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryFolderRepository.class);

    /**
     * all folders key is the path
     */
    private Map<String, Folder> foldersMap = new ConcurrentSkipListMap<>();

    /**
     * folder entries per path
     */
    private Map<String, List<FolderEntry>> folderEntriesMap = new ConcurrentSkipListMap<>();

    @PostConstruct
    private void init() {
        // no op
    }

    /**
     * we remove / from start and end to use consistent paths
     * 
     * @param path
     * @return
     */
    private String cleanPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "/";
        }
        path = StringUtils.removeStart(path, "/");
        path = StringUtils.removeEnd(path, "/");
        return path;
    }

    @Override
    public Folder addFolder(String path) {
        path = cleanPath(path);
        Folder folder = new Folder(path);
        folder.setName( extractName( path ) );
        foldersMap.put(path, folder);
        return folder;
    }

    @Override
    public Iterable<Folder> allFolder() {
        return foldersMap.values();
    }

    @Override
    public Iterable<Folder> childs(String path) {
        final List<Folder> childs = new CopyOnWriteArrayList<>();
        final String cleanedPath = cleanPath(path);
        this.foldersMap.values().stream().parallel().forEach(folder -> {
            // root path need special favour...
            if (StringUtils.equals(cleanedPath, "/")) {
                if (StringUtils.countMatches(folder.getPath(), "/") < 1) {
                    childs.add(folder);
                }
            } else {

                if (StringUtils.startsWith(folder.getPath(), cleanedPath)) {
                    // path asked /foo
                    // /foo/bar/beer /foo/bar
                    // /bar/beer /bar
                    // remove path start then count occurences of /

                    String endPath = StringUtils.removeStart(folder.getPath(), cleanedPath);
                    if (StringUtils.countMatches(endPath, "/") > 0) {
                        childs.add(folder);
                    }
                }
            }
        });

        return childs;
    }

    @Override
    public void renameFolder(String path, String newPath) {
        // TODO we should have a lock here around those operations
        String cleanPath = cleanPath( path );
        String cleanNewPath = cleanPath( newPath );

        this.foldersMap.keySet().parallelStream().forEach( key -> {
            if (StringUtils.startsWith( key, cleanPath )){
                Folder folder = this.foldersMap.get( key );
                this.foldersMap.remove( key );
                String newFolderPath = StringUtils.replace( folder.getPath(), cleanPath, cleanNewPath, 1 );
                folder.setPath(newFolderPath );
                folder.setName( extractName( folder.getPath() ) );
                String newKey = StringUtils.replace( key, cleanPath, cleanNewPath, 1 );
                this.foldersMap.put( newKey, folder );
            }
        });

        this.folderEntriesMap.keySet().parallelStream().forEach( key -> {
            if (StringUtils.startsWith( key, cleanPath )){
                List<FolderEntry> entries = this.folderEntriesMap.get( key );
                String newKey = StringUtils.replace( key, cleanPath, cleanNewPath, 1 );
                this.folderEntriesMap.put( newKey, entries );
                this.folderEntriesMap.remove( key );
                entries.parallelStream().forEach( folderEntry -> {
                    String newFolderPath = StringUtils.replace( folderEntry.getPath(), cleanPath, cleanNewPath, 1 );
                    folderEntry.setPath( newFolderPath );
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
    public FolderEntry addFolderEntry(FolderEntry folderEntry) {
        folderEntry.setPath(cleanPath(folderEntry.getPath()));
        List<FolderEntry> folderEntries = folderEntriesMap.get(folderEntry.getPath());
        if (folderEntries == null) {
            folderEntries = new CopyOnWriteArrayList<>();
            folderEntriesMap.put(folderEntry.getPath(), folderEntries);
        }
        folderEntry.buildId();
        folderEntries.add(folderEntry);
        return folderEntry;
    }

    @Override
    public void copyFolderEntry(FolderEntry folderEntry, String destinationPath) {
        FolderEntry cloned = new FolderEntry(folderEntry.getContentType(), //
                folderEntry.getContentId(), //
                cleanPath(destinationPath));
        this.addFolderEntry(cloned);
    }

    @Override
    public void moveFolderEntry(FolderEntry folderEntry, String destinationPath) {
        destinationPath = cleanPath(destinationPath);
        List<FolderEntry> entries = folderEntriesMap.get(cleanPath(folderEntry.getPath()));
        if (entries != null) {
            entries.remove(folderEntry);
        }
        folderEntry.setPath(destinationPath);
        addFolderEntry(folderEntry);
    }

    @Override
    public Iterable<FolderEntry> entries(String path, String contentType) {
        return folderEntriesMap.get(cleanPath(path));
    }

    @Override
    public Iterable<FolderEntry> findFolderEntries(String contentId, String contentType) {
        List<FolderEntry> entries = new CopyOnWriteArrayList<>();

        this.folderEntriesMap.values().stream().parallel().forEach(folderEntries -> {
            folderEntries.stream().parallel().forEach(folderEntry -> {
                if (StringUtils.equalsIgnoreCase(contentId, folderEntry.getContentId()) //
                        && StringUtils.equalsIgnoreCase(contentType, folderEntry.getContentType())) {
                    entries.add(folderEntry);
                }
            });
        });

        return entries;
    }

    @Override
    public void removeFolder(String path) {
        path = cleanPath(path);
        Iterator<String> paths = this.foldersMap.keySet().iterator();
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
    public void removeFolderEntry(String folderPath, String contentId, String contentType) {
        folderPath = cleanPath(folderPath);
        List<FolderEntry> entries = folderEntriesMap.get(folderPath);
        entries.remove(new FolderEntry(contentType, contentId, folderPath));
    }

    @Override
    public int size() {
        return this.foldersMap.size();
    }
}
