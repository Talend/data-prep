// ============================================================================
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

package org.talend.dataprep.api.service.command.dataset;

import static org.talend.dataprep.command.Defaults.asString;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

/**
 * Command in charge of copying a dataset.
 */
@Component
@Scope("request")
public class CopyDataSet extends GenericCommand<String> {

    /**
     * Private constructor.
     *
     * @param id the dataset id to copy.
     * @param name the copy name.
     */
    private CopyDataSet(String id, String name) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> {
            try {
                URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/datasets/" + id + "/copy");
                if(StringUtils.isNotBlank(name)) {
                    uriBuilder.addParameter("copyName", name);
                }
                return new HttpPost(uriBuilder.build());
            } catch (URISyntaxException e) {
                throw new TDPException(UNEXPECTED_EXCEPTION, e);
            }
        });
        on(HttpStatus.OK).then(asString());
    }
}
