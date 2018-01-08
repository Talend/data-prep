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

package org.talend.dataprep.transformation.actions;

import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.CREATE_NEW_COLUMN;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.test.LocalizationRule;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionFactory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for all related unit tests that deal with metadata
 */
public abstract class AbstractMetadataBaseTest<T extends AbstractActionMetadata> {

    /** The dataprep ready jackson builder. */
    protected final ObjectMapper mapper = new ObjectMapper();

    protected final ActionFactory factory = new ActionFactory();

    protected final ActionRegistry actionRegistry = new ClassPathActionRegistry("org.talend.dataprep.transformation.actions");

    protected final AnalyzerService analyzerService = new AnalyzerService();

    @Rule
    public LocalizationRule rule = new LocalizationRule(Locale.US);

    /* The action to be tested: */
    protected T action;

    public AbstractMetadataBaseTest(T action){
        this.action = action;
    }

    /**
     * For TDP-3798, add a checkbox for most actions to allow the user to choose if action is applied in place or if it
     * creates a new column.
     * This enum declares the possible policy for an action.
     * This is used to ensure that every action test classes declare their policy, and that they are tested.
     */
    public enum CreateNewColumnPolicy {
        VISIBLE_DISABLED, // checkbox param is visible, default to 'false' (like 'Negate')
        VISIBLE_ENABLED, // checkbox param is visible, default to 'true' (like 'Compare dates')
        INVISIBLE_DISABLED, // no checkbox, always in-place (like 'Mask data')
        INVISIBLE_ENABLED, // no checkbox, always creates new column (like 'Extract email parts')
        NA // Not applicable for this action (like 'Delete row')
    }

    /**
     * Each action test class must implement this method, to assure a minimum level of test.
     * At least that the 'toggle create new column' feature is take into account.
     */
    protected abstract CreateNewColumnPolicy getCreateNewColumnPolicy();

    @Test
    public void testCreateNewColumnPolicy() {
        switch (getCreateNewColumnPolicy()) {
        case VISIBLE_DISABLED:
            allowCreateColumn_disabledByDefault();
            break;
        case VISIBLE_ENABLED:
            allowCreateColumn_enabledByDefault();
            break;
        case INVISIBLE_DISABLED:
        case NA:
            forbidCreateColumn_alwaysDisabled();
            break;
        case INVISIBLE_ENABLED:
            forbidCreateColumn_alwaysEnabled();
            break;
        }
    }

    private void allowCreateColumn_enabledByDefault() {
        // test that 'create_new_column' parameter is present and set to 'true' by default:
        final List<Parameter> parameters = action.getParameters(Locale.getDefault());

        boolean found = false;
        for (Parameter parameter : parameters) {
            if (parameter.getName().equals(CREATE_NEW_COLUMN)) {
                found = true;
                assertEquals("Create new column", parameter.getLabel());
                assertTrue(Boolean.parseBoolean(parameter.getDefault()));
            }
        }
        if (!found) {
            fail("'Create new column' not found");
        }
    }

    private void allowCreateColumn_disabledByDefault() {
        // test that 'create_new_column' parameter is present and set to 'false' by default:
        final List<Parameter> parameters = action.getParameters(Locale.getDefault());

        boolean found = false;
        for (Parameter parameter : parameters) {
            if (parameter.getName().equals(CREATE_NEW_COLUMN)) {
                found = true;
                assertEquals("Create new column", parameter.getLabel());
                assertFalse(Boolean.parseBoolean(parameter.getDefault()));
            }
        }
        if (!found) {
            fail("'Create new column' not found");
        }
    }

    private void forbidCreateColumn_alwaysEnabled() {
        // test that 'create_new_column' parameter is not present:
        final List<Parameter> parameters = action.getParameters(Locale.getDefault());

        for (Parameter parameter : parameters) {
            if (parameter.getName().equals(CREATE_NEW_COLUMN)) {
                fail("'Create new column' found, while it should not");
            }
        }

        // test that this action will create a new column:
        assertTrue(ActionsUtils.doesCreateNewColumn(Collections.emptyMap(), true));
    }

    private void forbidCreateColumn_alwaysDisabled() {
        // test that 'create_new_column' parameter is not present:
        final List<Parameter> parameters = action.getParameters(Locale.getDefault());

        for (Parameter parameter : parameters) {
            if (parameter.getName().equals(CREATE_NEW_COLUMN)) {
                fail("'Create new column' found, while it should not");
            }
        }

        // test that this action will not create a new column:
        assertFalse(ActionsUtils.doesCreateNewColumn(Collections.<String, String>emptyMap(), false));
    }

    @Test
    public void test_apply_inplace() throws Exception {
        switch (getCreateNewColumnPolicy()) {
        case NA:
            break;
        default:
            fail("Not implemented");
        }
    }

    @Test
    public void test_apply_in_newcolumn() throws Exception {
        switch (getCreateNewColumnPolicy()) {
        case NA:
            break;
        default:
            fail("Not implemented");
        }
    }

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

        final Map<ColumnMetadata, String> values = new LinkedHashMap<>();

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

        String buildValue() {
            return value;
        }

        ColumnMetadata buildColumn(int current) {
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
