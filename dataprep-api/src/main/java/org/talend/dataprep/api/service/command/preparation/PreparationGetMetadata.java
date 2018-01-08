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

package org.talend.dataprep.api.service.command.preparation;

import static org.talend.dataprep.command.Defaults.convertResponse;

import javax.annotation.PostConstruct;

import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.command.GenericCommand;

/**
 * Command used to retrieve the preparation content.
 */
@Component
@Scope("request")
public class PreparationGetMetadata extends GenericCommand<DataSetMetadata> {

    /** The preparation id. */
    private final String id;

    /** The preparation version. */
    private final String version;

    /**
     * @param id the preparation id.
     * @param version the preparation version.
     */
    private PreparationGetMetadata(String id, String version) {
        super(PREPARATION_GROUP);
        this.id = id;
        this.version = version;
        execute(() -> new HttpGet(transformationServiceUrl + "/apply/preparation/" + id + "/" + version + "/metadata"));
    }

    @PostConstruct
    public void init() {
        on(HttpStatus.OK).then(convertResponse(objectMapper, DataSetMetadata.class));
    }

}
