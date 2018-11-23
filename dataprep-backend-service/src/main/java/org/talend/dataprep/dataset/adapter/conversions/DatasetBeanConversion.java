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

import static org.talend.dataprep.conversions.BeanConversionService.fromBean;

import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.dataprep.api.dataset.DataSetContent;
import org.talend.dataprep.api.dataset.DataSetGovernance;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DatasetDTO;
import org.talend.dataprep.api.dataset.DatasetDetailsDTO;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.adapter.Dataset;
import org.talend.dataprep.dataset.adapter.Datastore;
import org.talend.dataprep.processor.BeanConversionServiceWrapper;
import org.talend.dataprep.schema.csv.CSVFormatFamily;
import org.talend.dataprep.schema.xls.XlsFormatFamily;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Bean Conversion from {@link Dataset} to {@link DataSetMetadata}
 */
@Component
public class DatasetBeanConversion extends BeanConversionServiceWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetBeanConversion.class);

    private final ObjectMapper objectMapper;

    public DatasetBeanConversion(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public BeanConversionService doWith(BeanConversionService conversionService, String beanName,
            ApplicationContext applicationContext) {

        conversionService.register(fromBean(Dataset.class) //
                .toBeans(DatasetDTO.class) //
                .using(DatasetDTO.class, convertDatasetToDatasetDTO())//
                .build());

        conversionService.register(fromBean(Dataset.class) //
                .toBeans(DataSetMetadata.class) //
                .using(DataSetMetadata.class, convertDatasetToDatasetMetadata()) //
                .build());

        conversionService.register(fromBean(Dataset.class) //
                .toBeans(DatasetDetailsDTO.class) //
                .using(DatasetDetailsDTO.class, convertDatasetToDatasetDetailsDTO()) //
                .build());

        conversionService.register(fromBean(DatasetDTO.class) //
                .toBeans(Dataset.class) //
                .using(Dataset.class, convertDatasetDTOToDataset()) //
                .build());

        conversionService.register(fromBean(DataSetMetadata.class) //
                .toBeans(DatasetDTO.class) //
                .using(DatasetDTO.class, convertDatasetMetadataToDatasetDTO())//
                .build());

        return conversionService;
    }

    private BiFunction<Dataset, DataSetMetadata, DataSetMetadata> convertDatasetToDatasetMetadata() {
        return (dataset, dataSetMetadata) -> {
            dataSetMetadata.setName(dataset.getLabel());
            dataSetMetadata.setCreationDate(dataset.getCreated());
            dataSetMetadata.setLastModificationDate(dataset.getUpdated());
            dataSetMetadata.setAuthor(dataset.getOwner());

            Dataset.CertificationState certificationState = dataset.getCertification();
            if (certificationState != null) {
                DataSetGovernance governance = new DataSetGovernance();
                governance.setCertificationStep(DataSetGovernance.Certification.valueOf(certificationState.name()));
                dataSetMetadata.setGovernance(governance);
            }

            JsonNode datasetProperties = dataset.getProperties();
            if (datasetProperties == null) {
                datasetProperties = objectMapper.createObjectNode();
            }

            Datastore datastore = dataset.getDatastore();
            //FIXME bypass for content / location information about local file or live dataset
            if (datastore == null) {
                try {
                    if (datasetProperties.has("content")) {
                        DataSetContent content =
                                objectMapper.treeToValue(datasetProperties.get("content"), DataSetContent.class);
                        dataSetMetadata.setContent(content);
                    } else {
                        LOGGER.warn("no dataset content for the dataset [{}]", dataSetMetadata.getId());
                    }

                    if (datasetProperties.has("location")) {
                        DataSetLocation location =
                                objectMapper.treeToValue(datasetProperties.get("location"), DataSetLocation.class);
                        dataSetMetadata.setLocation(location);
                    } else {
                        LOGGER.warn("no dataset location for the dataset [{}]", dataSetMetadata.getId());
                    }
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
                dataSetMetadata.getContent().setNbRecords(dataSetMetadataLegacy.getNbRecords());
            }

            return dataSetMetadata;
        };
    }

    private BiFunction<Dataset, DatasetDTO, DatasetDTO> convertDatasetToDatasetDTO() {
        return (dataset, datasetDTO) -> {
            datasetDTO.setName(dataset.getLabel());
            datasetDTO.setCreationDate(dataset.getCreated());
            datasetDTO.setLastModificationDate(dataset.getUpdated());
            datasetDTO.setAuthor(dataset.getOwner());
            datasetDTO.setFavorite(dataset.isFavorite());
            datasetDTO.setCertification(dataset.getCertification());
            datasetDTO.setShared(dataset.getSharing().isSharedWithOthers());

            if (Datastore.islocal(dataset.getDatastore())) {
                final String format = dataset.getFormat();
                if (format != null) {
                    switch (format) {
                    case "EXCEL":
                        datasetDTO.setType(XlsFormatFamily.MEDIA_TYPE);
                        break;
                    case "CSV":
                        datasetDTO.setType(CSVFormatFamily.MEDIA_TYPE);
                        break;
                    default:
                        LOGGER.warn("Unable to map [{}] dataset format to a supported format", format);
                        datasetDTO.setType(CSVFormatFamily.MEDIA_TYPE);
                        break;
                    }
                }
            }

            // Manage legacy fields that doesn't match data catalog concept
            Dataset.DataSetMetadataLegacy dataSetMetadataLegacy = dataset.getDataSetMetadataLegacy();
            if (dataSetMetadataLegacy != null) {
                datasetDTO.setDraft(dataSetMetadataLegacy.isDraft());
                datasetDTO.setRecords(dataSetMetadataLegacy.getNbRecords());
            }

            return datasetDTO;
        };
    }

    private BiFunction<DataSetMetadata, DatasetDTO, DatasetDTO> convertDatasetMetadataToDatasetDTO() {
        return (datasetMetadata, datasetDTO) -> {
            datasetDTO.setCreationDate(datasetMetadata.getCreationDate());
            datasetDTO.setLastModificationDate(datasetMetadata.getLastModificationDate());

            if (datasetDTO.getOwner() == null) {
                datasetDTO.setOwner(new Owner());
            }
            datasetDTO.getOwner().setId(datasetMetadata.getAuthor());

            if (datasetMetadata.getGovernance() != null) {
                datasetDTO.setCertification(Dataset.CertificationState
                        .valueOf(datasetMetadata.getGovernance().getCertificationStep().name()));
            }
            datasetDTO.setType(datasetMetadata.getContent().getMediaType());
            datasetDTO.setRecords(datasetMetadata.getContent().getNbRecords());

            return datasetDTO;
        };
    }

    private BiFunction<DatasetDTO, Dataset, Dataset> convertDatasetDTOToDataset() {
        return (datasetDTO, dataset) -> {
            dataset.setLabel(datasetDTO.getName());
            dataset.setCreated(datasetDTO.getCreationDate());
            dataset.setUpdated(datasetDTO.getLastModificationDate());
            dataset.setOwner(datasetDTO.getAuthor());

            if (dataset.getDataSetMetadataLegacy() == null) {
                dataset.setDataSetMetadataLegacy(new Dataset.DataSetMetadataLegacy());
            }
            dataset.getDataSetMetadataLegacy().setDraft(datasetDTO.isDraft());
            dataset.getDataSetMetadataLegacy().setNbRecords(datasetDTO.getRecords());

            return dataset;

        };
    }

    private BiFunction<Dataset, DatasetDetailsDTO, DatasetDetailsDTO> convertDatasetToDatasetDetailsDTO() {
        return (dataset, datasetDetailsDTO) -> {

            // call super class conversion
            datasetDetailsDTO = (DatasetDetailsDTO) convertDatasetToDatasetDTO().apply(dataset, datasetDetailsDTO);

            // and add conversion specific to DatasetDetailsDTO
            Dataset.DataSetMetadataLegacy dataSetMetadataLegacy = dataset.getDataSetMetadataLegacy();
            if (dataSetMetadataLegacy != null) {
                datasetDetailsDTO.setEncoding(dataSetMetadataLegacy.getEncoding());
            }

            return datasetDetailsDTO;

        };
    }
}
