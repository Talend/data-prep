// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.common.ActionFactory;
import org.talend.dataprep.transformation.actions.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for all related unit tests that deal with metadata
 */
public abstract class AbstractMetadataBaseTest {

    /** The dataprep ready jackson builder. */
    protected final ObjectMapper mapper = new ObjectMapper();

    protected final ActionFactory factory = new ActionFactory();

    protected final ActionRegistry actionRegistry = new ClassPathActionRegistry("org.talend.dataprep.transformation.actions");

    protected final AnalyzerService analyzerService = new AnalyzerService();

    protected String generateJson(String token, String operator) {
        ReplaceOnValueHelper r = new ReplaceOnValueHelper(token, operator);
        try {
            return mapper.writeValueAsString(r);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    protected ColumnMetadata createMetadata(String id, String name, Type type, String statisticsFileName) throws IOException {
        ColumnMetadata column = createMetadata(id, name, type);
        ObjectMapper mapper = new ObjectMapper();
        final Statistics statistics = mapper.reader(Statistics.class).readValue(
                getClass().getResourceAsStream("/org/talend/dataprep/transformation/actions/date/" + statisticsFileName));
        column.setStatistics(statistics);
        return column;
    }

    protected ColumnMetadata createMetadata(String id, String name) {
        return createMetadata(id, name, Type.STRING);
    }

    protected ColumnMetadata createMetadata(String id, String name, Type type) {
        return columnBaseBuilder().computedId(id).name(name).type(type).build();
    }

    protected ColumnMetadata.Builder columnBaseBuilder() {
        return ColumnMetadata.Builder.column();
    }

    public static class ValuesBuilder {

        private final List<ValueBuilder> valueBuilders = new ArrayList<>();

        Map<ColumnMetadata, String> values = new LinkedHashMap<>();

        public static ValuesBuilder builder() {
            return new ValuesBuilder();
        }

        public ValuesBuilder value(String value, Type type) {
            valueBuilders.add(ValueBuilder.value(value).type(type));
            return this;
        }

        public DataSetRow build() {
            int current = 0;
            for (ValueBuilder valueBuilder : valueBuilders) {
                values.put(valueBuilder.buildColumn(current++), valueBuilder.buildValue());
            }

            final RowMetadata schema = new RowMetadata(new ArrayList<>(values.keySet()));
            final DataSetRow dataSetRow = new DataSetRow(schema);
            for (Map.Entry<ColumnMetadata, String> entry : values.entrySet()) {
                dataSetRow.set(entry.getKey().getId(), entry.getValue());
            }
            return dataSetRow;
        }

        public ValuesBuilder with(ValueBuilder valueBuilder) {
            valueBuilders.add(valueBuilder);
            return this;
        }
    }

    public static class ValueBuilder {

        private final DecimalFormat format = new DecimalFormat("0000");

        private String value = StringUtils.EMPTY;

        private Type type = Type.STRING;

        private String name = StringUtils.EMPTY;

        private String domainName;

        private Statistics statistics;

        public ValueBuilder(String value) {
            this.value = value;
        }

        public static ValueBuilder value(String value) {
            return new ValueBuilder(value);
        }

        public ValueBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public ValueBuilder name(String name) {
            this.name = name;
            return this;
        }

        protected String buildValue() {
            return value;
        }

        protected ColumnMetadata buildColumn(int current) {
            return ColumnMetadata.Builder.column().computedId(format.format(current)).statistics(statistics).type(type).name(name)
                    .domain(domainName).build();
        }

        public ValueBuilder domain(String name) {
            domainName = name;
            return this;
        }

        public ValueBuilder statistics(InputStream statisticsStream) {
            try {
                final ObjectMapper mapper = new ObjectMapper();
                statistics = mapper.readValue(statisticsStream, Statistics.class);
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
