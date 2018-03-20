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

import static org.talend.dataprep.command.Defaults.asNull;

import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * Command used to move a preparation.
 */
@Component
@Scope("request")
public class PreparationMove extends GenericCommand<Void> {


    /**
     * Default constructor.
     *
     * @param id The preparation id to move.
     * @param folder where to find the preparation.
     * @param destination Where to move the preparation to.
     * @param newName Optional new preparation name.
     */
    // private constructor to ensure the IoC
    private PreparationMove(String id, String folder, String destination, String newName) {
        super(GenericCommand.DATASET_GROUP);

        execute(() -> onExecute(id, folder, destination, newName));
        on(HttpStatus.OK).then(asNull());
    }


    private HttpRequestBase onExecute(String id, String folder, String destination, String newName) {
        try {
            URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/preparations/" + id +"/move");
            if (StringUtils.isNotBlank(folder)) {
                uriBuilder.addParameter("folder", folder);
            }
            if (StringUtils.isNotBlank(destination)) {
                uriBuilder.addParameter("destination", destination);
            }
            if (StringUtils.isNotBlank(newName)) {
                uriBuilder.addParameter("newName", newName);
            }
            return new HttpPut(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}