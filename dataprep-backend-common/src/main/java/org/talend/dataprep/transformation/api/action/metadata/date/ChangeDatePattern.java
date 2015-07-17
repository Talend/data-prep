package org.talend.dataprep.transformation.api.action.metadata.date;

import static org.talend.dataprep.api.preparation.Action.Builder.builder;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.CommonErrorCodes;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.transformation.api.action.metadata.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.SingleColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Item;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Change the date pattern on a 'date' column.
 */
@Component(ChangeDatePattern.ACTION_BEAN_PREFIX + ChangeDatePattern.ACTION_NAME)
public class ChangeDatePattern extends SingleColumnAction {

    /** Action name. */
    public static final String ACTION_NAME = "change_date_pattern"; //$NON-NLS-1$

    /** Name of the new date pattern parameter. */
    protected static final String NEW_PATTERN = "new_pattern"; //$NON-NLS-1$

    /** The parameter object for the custom new pattern. */
    private static final String CUSTOM_PATTERN = "custom_date_pattern";

    /** The parameter object for the custom new pattern. */
    private static final Parameter CUSTOM_PATTERN_PARAMETER = new Parameter(CUSTOM_PATTERN, Type.STRING.getName(),
            StringUtils.EMPTY);

    /** Name of the old pattern parameter. */
    protected static final String OLD_PATTERN = "old_pattern"; //$NON-NLS-1$

    /**
     * Return the json statistics node.
     *
     * @param mapper jackson object mapper.
     * @param column the column metadata to work on.
     * @return the json statistics node.
     */
    private JsonNode getStatisticsNode(ObjectMapper mapper, ColumnMetadata column) {
        try {
            return mapper.readTree(column.getStatistics());
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

    /**
     * @see ActionMetadata#create(Map)
     */
    @Override
    public Action create(Map<String, String> parameters) {
        String columnId = getColumnIdParameter(parameters);
        SimpleDateFormat newDateFormat = getDateFormat(getNewPattern(parameters));
        return builder().withRow((row, context) -> {
            // sadly unable to do that outside of the closure since the context is not available
                SimpleDateFormat currentDateFormat = getDateFormat((String) context.get(OLD_PATTERN));

                // parse the
                String value = row.get(columnId);
                Date date = null;
                try {
                    date = currentDateFormat.parse(value);
                    row.set(columnId, newDateFormat.format(date));
                } catch (ParseException e) {
                    // cannot parse the date, let's leave it as is
                }
            }).withMetadata((rowMetadata, context) -> {
                // parse and checks the new date pattern
                String newPattern = getDateFormat(getNewPattern(parameters)).toPattern();

                JsonFactory jsonFactory = new JsonFactory();
                ObjectMapper mapper = new ObjectMapper(jsonFactory);
                ColumnMetadata column = rowMetadata.getById(columnId);
                // defensive programming
                if (column == null) {
                    return;
                }

                // store the current pattern in the context
                JsonNode rootNode = getStatisticsNode(mapper, column);

                JsonNode mostUsedPatternNode = rootNode.get("patternFrequencyTable").get(0); //$NON-NLS-1$
                String futureExPattern = mostUsedPatternNode.get("pattern").asText(); //$NON-NLS-1$
                context.put(OLD_PATTERN, futureExPattern);

                // update the pattern in the column
                try {
                    ((ObjectNode) mostUsedPatternNode).put("pattern", newPattern); //$NON-NLS-1$
                    StringWriter temp = new StringWriter(1000);

                    JsonGenerator generator = jsonFactory.createGenerator(temp);
                    mapper.writeTree(generator, rootNode);
                    column.setStatistics(temp.toString());
                } catch (IOException e) {
                    throw new TDPException(CommonErrorCodes.UNABLE_TO_WRITE_JSON, e);
                }
            }).build();
    }

    /**
     * @param pattern the date pattern.
     * @return the simple date format out of the parameters.
     */
    private SimpleDateFormat getDateFormat(String pattern) {
        try {
            if (StringUtils.isEmpty(pattern)) {
                throw new IllegalArgumentException();
            }
            return new SimpleDateFormat(pattern, Locale.ENGLISH);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("pattern '" + pattern + "' is not a valid date pattern", iae);
        }

    }

    private String getNewPattern(Map<String, String> parameters) {
        return ("custom").equals(parameters.get(NEW_PATTERN)) ? parameters.get(CUSTOM_PATTERN) : parameters.get(NEW_PATTERN);
    }

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.DATE.getDisplayName();
    }

    /**
     * @see ActionMetadata#getItems()@return
     */
    @Override
    @Nonnull
    public Item[] getItems() {

        ResourceBundle patterns = ResourceBundle.getBundle(
                "org.talend.dataprep.transformation.api.action.metadata.date.date_patterns", Locale.ENGLISH);
        Enumeration<String> keys = patterns.getKeys();
        List<Item.Value> values = new ArrayList<>();
        while (keys.hasMoreElements()) {
            Item.Value currentValue = new Item.Value(patterns.getString(keys.nextElement()));
            values.add(currentValue);
        }

        values.add(new Item.Value("custom", CUSTOM_PATTERN_PARAMETER));
        values.get(0).setDefault(true);

        return new Item[] { new Item(NEW_PATTERN, "patterns", values.toArray(new Item.Value[] {})) };
    }

    /**
     * Only works on 'date' columns.
     * 
     * @see ActionMetadata#accept(ColumnMetadata)
     */
    @Override
    public boolean accept(ColumnMetadata column) {
        return Type.DATE.equals(Type.get(column.getType()));
    }
}
