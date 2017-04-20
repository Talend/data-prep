// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.service.delegate;

import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_SEARCH_DATAPREP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.api.EnrichedPreparation;
import org.talend.dataprep.api.service.command.dataset.SearchDataSets;
import org.talend.dataprep.api.service.command.folder.SearchFolders;
import org.talend.dataprep.api.service.command.preparation.LocatePreparation;
import org.talend.dataprep.api.service.command.preparation.PreparationSearchByName;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.preparation.service.UserPreparation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class SearchDelegate extends APIService {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SearchDelegate.class);

    public void search(List<String> filter, String name, boolean strict, OutputStream output) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching dataprep for '{}' (pool: {})...", name, getConnectionStats());
        }
        try (final JsonGenerator generator = mapper.getFactory().createGenerator(output)) {
            generator.writeStartObject();
            search(filter, name, strict, generator);
            generator.writeEndObject();

        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_SEARCH_DATAPREP, e);
        }
        LOG.info("Searching Done on dataprep for {} done with filter: {} and strict mode: {}", name, filter, strict);
    }

    protected void search(List<String> filter, String name, boolean strict, JsonGenerator generator) throws IOException {
        if (filter == null || filter.contains("folder")) {
            final int foldersFound = searchAndWriteFolders(name, strict, generator);
            LOGGER.info("{} folder(s) found", foldersFound);
        }
        if (filter == null || filter.contains("dataset")) {
            final int dataSetsFound = searchAndWriteDatasets(name, strict, generator);
            LOGGER.info("{} dataset(s) found", dataSetsFound);
        }
        if (filter == null || filter.contains("preparation")) {
            final int preparationsFound = searchAndWritePreparations(name, strict, generator);
            LOGGER.info("{} preparation(s) found", preparationsFound);
        }
    }

    /**
     * Search for the given name in the folders and write the result straight to output in json.
     *
     * @param name the name searched.
     * @param strict strict mode (the name should be the full name)
     * @param output where to write the json. @return the number of folders that match the searched name.
     */
    private int searchAndWriteFolders(final String name, final boolean strict, final JsonGenerator output) throws IOException {
        final int foldersFound;
        final SearchFolders commandListFolders = getCommand(SearchFolders.class, name, strict);
        try (InputStream input = commandListFolders.execute()) {
            List<Folder> folders = mapper.readValue(input, new TypeReference<List<Folder>>() {
            });
            foldersFound = folders.size();
            output.writeArrayFieldStart("folders");
            for (Folder folder : folders) {
                output.writeObject(folder);
            }
            output.writeEndArray();
        }
        return foldersFound;
    }

    /**
     * Search for the given name in the datasets and write the result straight to output in json.
     *
     * @param name the name searched.
     * @param strict strict mode (the name should be the full name)
     * @param output where to write the json. @return the number of datasets that match the searched name.
     */
    private int searchAndWriteDatasets(final String name, final boolean strict, final JsonGenerator output) throws IOException {

        final int datasetsFound;
        final SearchDataSets command = getCommand(SearchDataSets.class, name, strict);
        try (InputStream input = command.execute()) {
            List<DataSetMetadata> datasets = mapper.readValue(input, new TypeReference<List<DataSetMetadata>>() {
            });
            datasetsFound = datasets.size();
            output.writeArrayFieldStart("datasets");
            for (DataSetMetadata metadata : datasets) {
                output.writeObject(metadata);
            }
            output.writeEndArray();
        }
        return datasetsFound;
    }

    /**
     * Search for the given name in preparations and write them straight to the ouput on json.
     *
     * @param name the searched name.
     * @param strict strict mode (the name should be the full name)
     * @param output where to write the preparations. @return the number of preparation found.
     */
    private int searchAndWritePreparations(final String name, final boolean strict, final JsonGenerator output)
            throws IOException {

        final int preparationsFound;
        final PreparationSearchByName command = getCommand(PreparationSearchByName.class, name, strict);
        try (InputStream input = command.execute()) {
            List<UserPreparation> preparations = mapper.readValue(input, new TypeReference<List<UserPreparation>>() {
            });
            preparationsFound = preparations.size();
            output.writeArrayFieldStart("preparations");
            for (UserPreparation preparation : preparations) {
                EnrichedPreparation locatedPreparation = locatePreparation(preparation);
                output.writeObject(locatedPreparation);
            }
            output.writeEndArray();
        }
        return preparationsFound;
    }

    /**
     * Find where the preparation is located.
     *
     * @param preparation the preparation to locate.
     * @return an enriched preparation with additional folder parameters.
     */
    private EnrichedPreparation locatePreparation(UserPreparation preparation) {
        final LocatePreparation command = getCommand(LocatePreparation.class, preparation.id());
        final Folder folder = command.execute();
        return new EnrichedPreparation(preparation, folder);
    }

}
