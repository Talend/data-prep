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

package org.talend.dataprep.dataset.adapter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.dataprep.api.dataset.DataSetContent;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.jsonschema.ComponentProperties;
import org.talend.dataprep.processor.BeanConversionServiceWrapper;
import org.talend.dataprep.schema.FormatFamily;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.talend.dataprep.conversions.BeanConversionService.fromBean;

@Configuration
public class DatasetConversionConfiguration {

    private final ObjectMapper objectMapper;

    public DatasetConversionConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Component
    public class DatasetTodataSetMetadataConversions extends BeanConversionServiceWrapper {

        @Override
        public BeanConversionService doWith(BeanConversionService conversionService, String beanName,
                ApplicationContext applicationContext) {
            conversionService.register(fromBean(Dataset.class) //
                    .toBeans(DataSetMetadata.class).using(DataSetMetadata.class, (dataset, dataSetMetadata) -> {
                        dataSetMetadata.setName(dataset.getLabel());
                        dataSetMetadata.setCreationDate(dataset.getCreated());
                        dataSetMetadata.setLastModificationDate(dataset.getUpdated());
                        dataSetMetadata.setAuthor(dataset.getOwner());

                        JsonNode datasetProperties;
                        try {
                            datasetProperties = objectMapper.readTree(dataset.getProperties());
                        } catch (IOException e) {
                            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                        }

                        Datastore datastore = dataset.getDatastore();
                        if (datastore != null) {
                            //TODO implement TcompLocation converter in ee-dataset and use information from datastore and properties (datasetProperties)
                            String type = "application/prs.tcomp-ds.tcomp-" + dataset.getType();
                            SimpleDataSetLocation dataSetLocation = new SimpleDataSetLocation();
                            dataSetLocation.setLocationType(type);
                            dataSetLocation.setDynamic(true);
                            dataSetMetadata.setLocation(dataSetLocation);
                        } else {
                            try {
                                DataSetContent content = objectMapper.treeToValue(datasetProperties.get("content"),
                                        DataSetContent.class);
                                dataSetMetadata.setContent(content);

                                DataSetLocation location = objectMapper.treeToValue(datasetProperties.get("location"),
                                        DataSetLocation.class);
                                dataSetMetadata.setLocation(location);
                            } catch (JsonProcessingException e) {
                                throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                            }
                        }

                        return dataSetMetadata;
                    }) //
                    .build());

            return conversionService;
        }

    }

    private static class SimpleDataSetLocation implements DataSetLocation {

        private boolean dynamic;

        private String locationType;

        private List<Parameter> parameters;

        private ComponentProperties parametersAsSchema;

        private boolean schemaOriented;

        private String acceptedContentType;

        private boolean enabled;

        private Function<Locale, ComponentProperties> parameterSupplyier = l -> null;

        @Override
        public boolean isDynamic() {
            return dynamic;
        }

        public void setDynamic(boolean dynamic) {
            this.dynamic = dynamic;
        }

        @Override
        public String getLocationType() {
            return locationType;
        }

        @Override
        public List<Parameter> getParameters(Locale locale) {
            return null;
        }

        @Override
        public ComponentProperties getParametersAsSchema(Locale locale) {
            return parameterSupplyier.apply(locale);
        }

        public void setLocationType(String locationType) {
            this.locationType = locationType;
        }

        public List<Parameter> getParameters() {
            return parameters;
        }

        public void setParameters(List<Parameter> parameters) {
            this.parameters = parameters;
        }

        public ComponentProperties getParametersAsSchema() {
            return parametersAsSchema;
        }

        public void setParametersAsSchema(ComponentProperties parametersAsSchema) {
            this.parametersAsSchema = parametersAsSchema;
        }

        @Override
        public boolean isSchemaOriented() {
            return schemaOriented;
        }

        public void setSchemaOriented(boolean schemaOriented) {
            this.schemaOriented = schemaOriented;
        }

        @Override
        public String getAcceptedContentType() {
            return acceptedContentType;
        }

        public void setAcceptedContentType(String acceptedContentType) {
            this.acceptedContentType = acceptedContentType;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String toMediaType(FormatFamily formatFamily) {
            return formatFamily.getMediaType();
        }
    }
}
