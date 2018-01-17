//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.command.preparation;

import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

/**
 * Command used to copy the steps from a preparation to an empty one.
 */
@Component
@Scope("request")
public class PreparationCopyStepsFrom extends GenericCommand<Void> {

    /**
     * Constructor.
     * @param to the preparation id to copy the steps to.
     * @param from preparation id to copy the steps from.
     */
    // private constructor to ensure use of IoC
    private PreparationCopyStepsFrom(final String to, final String from) {
        super(PREPARATION_GROUP);
        execute(() -> onExecute(to, from));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute(final String to, final String from) {
        try {
            URIBuilder builder = new URIBuilder(preparationServiceUrl + "/preparations/" + to + "/steps/copy");
            builder.addParameter("from", from);
            return new HttpPut(builder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
    }
}
