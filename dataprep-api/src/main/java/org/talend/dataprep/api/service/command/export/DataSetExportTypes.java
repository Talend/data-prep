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

package org.talend.dataprep.api.service.command.export;

import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;

import java.io.InputStream;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.talend.dataprep.command.Defaults.pipeStream;

@Component
@Scope(SCOPE_PROTOTYPE)
public class DataSetExportTypes extends GenericCommand<InputStream> {

    private DataSetExportTypes(String dataSetId) {
        super(GenericCommand.TRANSFORM_GROUP);
        execute(() -> new HttpGet(this.transformationServiceUrl + "/export/formats/datasets/" + dataSetId));
        on(HttpStatus.OK).then(pipeStream());
    }

}
