package org.talend.dataprep.api.service.command.dataset;

import static org.talend.dataprep.api.service.command.common.Defaults.asString;
import static org.talend.dataprep.api.service.command.common.Defaults.emptyString;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * Command in charge of mostly updating a dataset content.
 */
@Component
@Scope("request")
public class CreateOrUpdateDataSet extends GenericCommand<String> {

    /** The content cache. */
    @Autowired
    private ContentCache contentCache;

    /**
     * Private constructor.
     *
     * @param client the http client.
     * @param id the dataset id.
     * @param name the dataset name.
     * @param dataSetContent the new dataset content.
     */
    private CreateOrUpdateDataSet(HttpClient client, String id, String name, InputStream dataSetContent, String folderPath) {
        super(PreparationAPI.DATASET_GROUP, client);
        execute(() -> {
            try {
                URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/datasets/" + id + "/raw/") //
                    .addParameter( "name", name ) //
                    .addParameter( "folderPath", folderPath );
                final HttpPut put = new HttpPut(uriBuilder.build()); // $NON-NLS-1$ //$NON-NLS-2$
                put.setEntity(new InputStreamEntity(dataSetContent));
                return put;
            } catch (URISyntaxException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        });
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_CREATE_OR_UPDATE_DATASET, e));
        on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(emptyString());
        on(HttpStatus.OK).then((req, res) -> {
            contentCache.evict(new ContentCacheKey(id)); // clear the cache (dataset and all its preparations)
            return asString().apply(req, res); // Return response as String
        });
    }
}
