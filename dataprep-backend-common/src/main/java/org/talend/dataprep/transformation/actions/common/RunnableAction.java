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

package org.talend.dataprep.transformation.actions.common;

import java.util.Map;

import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;

public class RunnableAction extends Action {

    /** Default noop action. */
    private static final DataSetRowAction IDLE_ROW_ACTION = (row, context) -> row;

    /** The wrapped row action. */
    private final DataSetRowAction rowAction;

    public RunnableAction() {
        this(IDLE_ROW_ACTION);
    }

    public RunnableAction(DataSetRowAction rowAction) {
        this.rowAction = rowAction;
    }

    public DataSetRowAction getRowAction() {
        return rowAction;
    }

    /**
     * Builder used to ease the Action creation.
     */
    public static class Builder {

        /** The default noop action. */
        private DataSetRowAction rowAction = IDLE_ROW_ACTION;

        private DataSetRowAction compile = IDLE_ROW_ACTION;

        private Map<String, String> parameters;

        private String name;

        /**
         * @return the Builder to use.
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * @param rowAction add the given row action to the builder.
         * @return the current builder to carry on building.
         */
        public Builder withRow(DataSetRowAction rowAction) {
            this.rowAction = rowAction;
            return this;
        }

        /**
         * @return the built row action.
         */
        public RunnableAction build() {
            DataSetRowAction newAction = new DataSetRowActionImpl(rowAction, compile);
            final RunnableAction builtAction = new RunnableAction(newAction);
            builtAction.getParameters().putAll(parameters);
            builtAction.setName(name);
            return builtAction;
        }

        public Builder withCompile(DataSetRowAction compile) {
            this.compile = compile;
            return this;
        }

        public Builder withParameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

    }


}
