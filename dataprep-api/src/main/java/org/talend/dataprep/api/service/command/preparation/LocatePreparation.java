// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.service.command.preparation;

import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.function.BiFunction;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

/**
 * Command used to locate a preparation (find out what folder it is stored in).
 */
@Component
@Scope("prototype")
public class LocatePreparation extends GenericCommand<Folder> {

    /**
     * Constructor.
     * 
     * @param id the preparation id to locate.
     */
    // private constructor to ensure IoC
    private LocatePreparation(String id) {
        super(PREPARATION_GROUP);
        execute(() -> onExecute(id));
        on(HttpStatus.OK).then(toFolder());
    }

    private HttpRequestBase onExecute(String id) {
        try {
            URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/preparations/" + id + "/folder");
            return new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Take care of mapping the response input stream to the expected result.
     * @return the parsed folder.
     */
    private BiFunction<HttpRequestBase, HttpResponse, Folder> toFolder() {
        return (req, resp) -> {
            try (InputStream input = resp.getEntity().getContent()) {
                return objectMapper.readValue(input, Folder.class);
            } catch (IOException e) {
                throw new TDPException(UNEXPECTED_EXCEPTION, e);
            } finally {
                req.releaseConnection();
            }
        };
    }

}
