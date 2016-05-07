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

package org.talend.dataprep.api.service.command.datepattern;

import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_CREATE_DATEPATTERN;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.datepattern.DatePattern;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@Scope("request")
public class CreateDatepattern extends GenericCommand<Void> {

    public CreateDatepattern(DatePattern datePattern) {
        super(GenericCommand.PREPARATION_GROUP);
        execute(() -> onExecute(datePattern));
        onError(e -> new TDPException(UNABLE_TO_CREATE_DATEPATTERN, e, ExceptionContext.build()));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute(DatePattern datePattern) {
        try {

            URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/datepatterns");
            uriBuilder.addParameter( "datePattern", datePattern.getPattern() );

            return new HttpPut(uriBuilder.build());

        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
