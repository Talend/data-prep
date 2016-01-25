package org.talend.dataprep.api.service.command.dataset;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.InputStream;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.common.Defaults;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

@Component
@Scope("request")
public class UpdateColumn extends GenericCommand<Void> {

    private UpdateColumn(final HttpClient client, final String dataSetId, final String columnId, final InputStream body) {
        super(PreparationAPI.DATASET_GROUP, client);
        execute(() -> {
            final HttpPost post = new HttpPost(datasetServiceUrl + "/datasets/" + dataSetId + "/column/" + columnId); //$NON-NLS-1$ //$NON-NLS-2$
            post.setHeader("Content-Type", APPLICATION_JSON_VALUE);
            post.setEntity(new InputStreamEntity(body));
            return post;
        });
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_CREATE_OR_UPDATE_DATASET, e,
                ExceptionContext.build().put("id", dataSetId)));
        on(HttpStatus.OK).then((req, res) -> Defaults.<Void> asNull().apply(req, res));
    }

}
