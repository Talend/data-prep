package org.talend.dataprep.transformation.api.action.metadata;

import static org.talend.dataprep.api.preparation.Action.Builder.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;

@Component(ComputeLength.ACTION_BEAN_PREFIX + ComputeLength.LENGTH_ACTION_NAME)
public class ComputeLength extends SingleColumnAction {

    /**
     * The action name.
     */
    public static final String LENGTH_ACTION_NAME = "compute_length"; //$NON-NLS-1$

    /**
     * The column appendix.
     */
    public static final String APPENDIX = "_length"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return LENGTH_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.QUICKFIX.getDisplayName();
    }

    /**
     * @see ActionMetadata#create(Map)
     */
    @Override
    public Action create(Map<String, String> parameters) {
        String columnId = parameters.get(COLUMN_ID);


        return builder().withRow((row, context) -> {
            String value = row.get(columnId);

            if (value != null) {
                String newValue = value.length() + "";

                row.set(columnId + APPENDIX, newValue);
            }
        }).withMetadata((rowMetadata, context) -> {
            List<String> columnIds = new ArrayList<>(rowMetadata.size());
            rowMetadata.getColumns().forEach(columnMetadata -> columnIds.add(columnMetadata.getId()));
            if (!columnIds.contains(columnId + APPENDIX)) {

                // go through the columns to be able to 'insert' the new columns just after the one needed.
                for (int i = 0; i < rowMetadata.getColumns().size(); i++) {
                    ColumnMetadata column = rowMetadata.getColumns().get(i);
                    if (!StringUtils.equals(column.getId(), columnId)) {
                        continue;
                    }

                    // create the new column
                    ColumnMetadata newColumnMetadata = ColumnMetadata.Builder //
                            .column() //
                            .computedId(column.getId() + APPENDIX) //
                            .name(column.getName() + APPENDIX) //
                            .type(Type.INTEGER) //
                            .empty(column.getQuality().getEmpty()) //
                            .invalid(column.getQuality().getInvalid()) //
                            .valid(column.getQuality().getValid()) //
                            .headerSize(column.getHeaderSize()) //
                            .build();
                    // add the new column after the current one
                    rowMetadata.getColumns().add(i + 1, newColumnMetadata);
                }
            }
        }).build();
    }

    /**
     * @see ActionMetadata#accept(ColumnMetadata)
     */
    @Override
    public boolean accept(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }
}
