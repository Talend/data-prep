package org.talend.dataprep.transformation.api.action.metadata;

import static org.talend.dataprep.api.preparation.Action.Builder.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.parameters.Item;
import org.talend.dataprep.transformation.api.action.parameters.Item.Value;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

/**
 * Split a cell value on a separator.
 */
@Component(Split.ACTION_BEAN_PREFIX + Split.SPLIT_ACTION_NAME)
public class Split extends SingleColumnAction {

    /** The action name. */
    public static final String SPLIT_ACTION_NAME = "split"; //$NON-NLS-1$

    /** The split column appendix. */
    public static final String SPLIT_APPENDIX = "_split"; //$NON-NLS-1$

    /**
     * The separator shown to the user as a list. An item in this list is the value 'other', which allow the user to
     * manually enter its separator.
     */
    private static final String SEPARATOR_PARAMETER = "separator"; //$NON-NLS-1$

    /** The separator manually specified by the user. Should be used only if SEPARATOR_PARAMETER value is 'other'. */
    private static final String MANUAL_SEPARATOR_PARAMETER = "manual_separator"; //$NON-NLS-1$

    /** Number of items produces by the split */
    private static final String LIMIT = "limit"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return SPLIT_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.QUICKFIX.getDisplayName();
    }

    @Override
    @Nonnull
    public Parameter[] getParameters() {
        return new Parameter[] { COLUMN_ID_PARAMETER, COLUMN_NAME_PARAMETER, new Parameter(LIMIT, Type.INTEGER.getName(), "2") };
    }

    /**
     * @see ActionMetadata#getItems()@return
     */
    @Override
    @Nonnull
    public Item[] getItems() {
        Value[] values = new Value[] { //
        new Value(":", true), //
                new Value("@"), //
                new Value(" "), //
                new Value("other", new Parameter(MANUAL_SEPARATOR_PARAMETER, Type.STRING.getName(), StringUtils.EMPTY)) };
        return new Item[] { new Item(SEPARATOR_PARAMETER, "categ", values) };
    }

    /**
     * @see ActionMetadata#accept(ColumnMetadata)
     */
    @Override
    public boolean accept(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    /**
     * @param parameters the action parameters.
     * @return the searator to use according to the given parameters.
     */
    private String getSeparator(Map<String, String> parameters) {
        return ("other").equals(parameters.get(SEPARATOR_PARAMETER)) ? parameters.get(MANUAL_SEPARATOR_PARAMETER) : parameters
                .get(SEPARATOR_PARAMETER);
    }

    /**
     * Split the column for each row.
     *
     * @see ActionMetadata#create(Map)
     */
    @Override
    public Action create(Map<String, String> parameters) {
        return builder().withRow((row, context) -> {
            String columnId = parameters.get(COLUMN_ID);
            String realSeparator = getSeparator(parameters);
            int limit = Integer.parseInt(parameters.get(LIMIT));

            // defensive programming
                if (StringUtils.isEmpty(realSeparator)) {
                    return;
                }
                String originalValue = row.get(columnId);
                if (originalValue != null) {
                    String[] split = originalValue.split(realSeparator, limit);
                    for (int i = 1; i <= limit; i++) {
                        String newValue = i <= split.length ? split[i - 1] : StringUtils.EMPTY;
                        List<String> rowIds = row.values().keySet().stream().collect(Collectors.toList());
                        Integer nextSplitIndex = getNextAvailableSplitIndex(rowIds, columnId);
                        row.set(columnId + SPLIT_APPENDIX + "_" + nextSplitIndex, newValue);
                    }
                }
            }).withMetadata((rowMetadata, context) -> {
                String columnId = parameters.get(COLUMN_ID);
                int limit = Integer.parseInt(parameters.get(LIMIT));
                // defensive programming
                if (StringUtils.isEmpty(getSeparator(parameters))) {
                    return;
                }
                // go through the columns to be able to 'insert' the new columns just after the one needed.
                for (int i = 0; i < rowMetadata.getColumns().size(); i++) {
                    ColumnMetadata column = rowMetadata.getColumns().get(i);
                    if (!StringUtils.equals(column.getId(), columnId)) {
                        continue;
                    }
                    for (int j = 1; j <= limit; j++) {
                        // get the new column id
                        List<String> columnIds = new ArrayList<>(rowMetadata.size());
                        rowMetadata.getColumns().forEach(columnMetadata -> columnIds.add(columnMetadata.getId()));

                        Integer nextAvailableSplitIndex = getNextAvailableSplitIndex(columnIds, column.getId());
                        if (nextAvailableSplitIndex == null) {
                            // if this happen, let's not break anything
                            break;
                        }
                        // create the new column
                        ColumnMetadata newColumnMetadata = ColumnMetadata.Builder //
                                .column() //
                                .computedId(column.getId() + SPLIT_APPENDIX + '_' + nextAvailableSplitIndex) //
                                .name(column.getName() + SPLIT_APPENDIX + '_' + nextAvailableSplitIndex) //
                                .type(Type.get(column.getType())) //
                                .empty(column.getQuality().getEmpty()) //
                                .invalid(column.getQuality().getInvalid()) //
                                .valid(column.getQuality().getValid()) //
                                .headerSize(column.getHeaderSize()) //
                                .build();
                        // add the new column after the current one
                        rowMetadata.getColumns().add(i + j, newColumnMetadata);
                    }
                }
            }).build();
    }


    /**
     * Return the next available split index for the given column within the given ids.
     *
     * This is useful in case a column is split several times.
     *
     * @param columnsId the columns id.
     * @param columnId the column id to split.
     * @return the next available split index or null if not found.
     */
    private Integer getNextAvailableSplitIndex(List<String> columnsId, String columnId) {

        for (int i = 1; i < 1000; i++) {
            String temp = columnId + SPLIT_APPENDIX + '_' + i;
            if (!columnsId.contains(temp)) {
                return i;
            }
        }

        return null;
    }

}
