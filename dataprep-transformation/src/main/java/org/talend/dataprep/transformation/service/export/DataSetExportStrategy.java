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

package org.talend.dataprep.transformation.service.export;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.dataset.adapter.DatasetClient;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.format.CSVFormat;
import org.talend.dataprep.transformation.service.BaseExportStrategy;
import org.talend.dataprep.transformation.service.ExportUtils;

/**
 * A {@link BaseExportStrategy strategy} to export a data set, without using a preparation.
 */
@Component
public class DataSetExportStrategy extends BaseSampleExportStrategy {

    @Autowired
    private DatasetClient datasetClient;

    @Override
    public boolean accept(ExportParameters parameters) {
        if (parameters == null) {
            return false;
        }
        return parameters.getContent() == null //
                && !StringUtils.isEmpty(parameters.getDatasetId()) //
                && StringUtils.isEmpty(parameters.getPreparationId());
    }

    @Override
    public StreamingResponseBody execute(ExportParameters parameters) {
        final String formatName = parameters.getExportType();
        final ExportFormat format = getFormat(formatName);
        ExportUtils.setExportHeaders(parameters.getExportName(), //
                parameters.getArguments().get(ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCODING), //
                format);
        return outputStream -> {
            // get the dataset content (in an auto-closable block to make sure it is properly closed)
            final String datasetId = parameters.getDatasetId();
            DataSet dataSet = datasetClient.getDataSet(datasetId);
            // get the actions to apply (no preparation ==> dataset export ==> no actions)
            Configuration configuration = Configuration.builder() //
                    .args(parameters.getArguments()) //
                    .outFilter(rm -> filterService.build(parameters.getFilter(), rm)) //
                    .format(format.getName()) //
                    .volume(Configuration.Volume.SMALL) //
                    .output(outputStream) //
                    .limit(limit) //
                    .build();
            factory.get(configuration).buildExecutable(dataSet, configuration).execute();
        };
    }
}
