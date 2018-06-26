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

package org.talend.dataprep.dataset.adapter.conversions;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.dataprep.api.dataset.DataSetContent;
import org.talend.dataprep.api.dataset.DataSetGovernance;
import org.talend.dataprep.api.dataset.DataSetGovernance.Certification;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.adapter.Dataset;
import org.talend.dataprep.dataset.adapter.Dataset.CertificationState;
import org.talend.dataprep.dataset.adapter.Datastore;
import org.talend.dataprep.processor.BeanConversionServiceWrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.talend.dataprep.conversions.BeanConversionService.fromBean;

@Component
public class DatasetBeanConversion extends BeanConversionServiceWrapper {

    private final ObjectMapper objectMapper;

    public DatasetBeanConversion(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public BeanConversionService doWith(BeanConversionService conversionService, String beanName,
            ApplicationContext applicationContext) {
        conversionService.register(fromBean(Dataset.class) //
                .toBeans(DataSetMetadata.class) //
                .using(DataSetMetadata.class, (dataset, dataSetMetadata) -> {
                    dataSetMetadata.setName(dataset.getLabel());
                    dataSetMetadata.setCreationDate(dataset.getCreated());
                    dataSetMetadata.setLastModificationDate(dataset.getUpdated());
                    dataSetMetadata.setAuthor(dataset.getOwner());

                    CertificationState certificationState = dataset.getCertification();
                    if (certificationState != null) {
                        DataSetGovernance governance = new DataSetGovernance();
                        governance.setCertificationStep(Certification.valueOf(certificationState.name()));
                        dataSetMetadata.setGovernance(governance);
                    }

                    JsonNode datasetProperties = dataset.getProperties();
                    if (datasetProperties == null) {
                        datasetProperties = objectMapper.createObjectNode();
                    }

                    Datastore datastore = dataset.getDatastore();
                    //FIXME bypass for content / location information about local file
                    if (datastore == null) {
                        try {
                            DataSetContent content =
                                    objectMapper.treeToValue(datasetProperties.get("content"), DataSetContent.class);
                            dataSetMetadata.setContent(content);

                            DataSetLocation location =
                                    objectMapper.treeToValue(datasetProperties.get("location"), DataSetLocation.class);
                            dataSetMetadata.setLocation(location);
                        } catch (JsonProcessingException e) {
                            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                        }
                    }

                    // Manage legacy fields that doesn't match data catalog concept
                    Dataset.DataSetMetadataLegacy dataSetMetadataLegacy = dataset.getDataSetMetadataLegacy();
                    if (dataSetMetadataLegacy != null) {
                        dataSetMetadata.setSheetName(dataSetMetadataLegacy.getSheetName());
                        dataSetMetadata.setSchemaParserResult(dataSetMetadataLegacy.getSchemaParserResult());
                        dataSetMetadata.setDraft(dataSetMetadataLegacy.isDraft());
                        dataSetMetadata.setEncoding(dataSetMetadataLegacy.getEncoding());
                        dataSetMetadata.setTag(dataSetMetadataLegacy.getTag());
                    }

                    return dataSetMetadata;
                }) //
                .build());

        return conversionService;
    }

}
