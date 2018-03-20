/*
 *  ============================================================================
 *
 *  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 *  This source code is available under agreement available at
 *  https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 *  You should have received a copy of the agreement
 *  along with this program; if not, write to Talend SA
 *  9 rue Pages 92150 Suresnes, France
 *
 *  ============================================================================
 */

package org.talend.dataprep.api.service.command.preparation;

import static org.talend.dataprep.command.Defaults.asString;

import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * Hystrix command used to copy a preparation.
 */
@Component
@Scope("request")
public class PreparationCopy extends GenericCommand<String> {

    /**
     * Default constructor.
     *
     * @param preparationId the preparation id.
     * @param destination where to copy the preparation to, if empty, root folder is used.
     * @param newName optional new name for the copy, if empty, the original preparation name is used.
     */
    // private constructor to ensure the use of IoC
    private PreparationCopy(final String preparationId, final String destination, final String newName) {
        super(PREPARATION_GROUP);
        execute(() -> onExecute(preparationId, destination, newName));
        on(HttpStatus.OK).then(asString());
    }

    private HttpRequestBase onExecute(final String preparationId, final String destination, final String newName) {
        try {
            URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/preparations/"+ preparationId +"/copy");
            if (StringUtils.isNotBlank(destination)) {
                uriBuilder.addParameter("destination", destination);
            }
            if (StringUtils.isNotBlank(newName)) {
                uriBuilder.addParameter("name", newName);
            }
            return new HttpPost(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
