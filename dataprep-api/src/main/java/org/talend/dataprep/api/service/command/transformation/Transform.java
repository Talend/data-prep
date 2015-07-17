package org.talend.dataprep.api.service.command.transformation;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.ReleasableInputStream;
import org.talend.dataprep.api.service.command.common.DataPrepCommand;
import org.talend.dataprep.exception.CommonErrorCodes;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.TDPExceptionContext;

@Component
@Scope("request")
public class Transform extends DataPrepCommand<InputStream> {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(Transform.class);

    private final InputStream content;

    private final String actions;

    private Transform(HttpClient client, InputStream content, String actions) {
        super(APIService.TRANSFORM_GROUP, client);
        this.content = content;
        this.actions = actions;
    }

    @Override
    protected InputStream run() throws Exception {
        String uri = transformationServiceUrl + "/transform/JSON"; //$NON-NLS-1$
        HttpPost transformationCall = new HttpPost(uri);

        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("actions", new StringBody(actions, ContentType.TEXT_PLAIN.withCharset("UTF-8"))) //$NON-NLS-1$
                .addPart("content", new InputStreamBody(content, ContentType.APPLICATION_JSON)) //$NON-NLS-1$
                .build();

        transformationCall.setEntity(reqEntity);
        try {
            HttpResponse response = client.execute(transformationCall);
            int statusCode = response.getStatusLine().getStatusCode();
            // 400 and 500 errors
            if (statusCode >= 400) {
                TDPExceptionContext context = TDPExceptionContext.build() //
                        .put("url", uri) //
                        .put("cause", response.getStatusLine());
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, context);
            }
            InputStream content = response.getEntity().getContent();
            return new ReleasableInputStream(content, transformationCall::releaseConnection);
        } catch (Exception e) {
            LOG.error("exception while processing transformation : " + e.getMessage(), e);
            throw e;
        }

    }
}
