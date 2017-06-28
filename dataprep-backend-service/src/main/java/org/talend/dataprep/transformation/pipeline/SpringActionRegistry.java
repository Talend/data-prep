package org.talend.dataprep.transformation.pipeline;

import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.help.Help;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.CellAction;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.DataSetAction;
import org.talend.dataprep.transformation.actions.common.RowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class SpringActionRegistry implements ActionRegistry { // NOSONAR

    @Autowired(required = false)
    private List<ActionDefinition> actions;

    @Autowired(required = false)
    private Help help;

    @Override
    public ActionDefinition get(String name) {
        for (ActionDefinition action : actions) {
            if (action.getName().equals(name)) {
                return help != null ? new ActionWithHelpLinkDefinition(action) : action;
            }
        }
        return null;
    }

    /**
     * ActionDefinition decorator in order construct help links (depending of TDP version)
     */
    class ActionWithHelpLinkDefinition implements ActionDefinition, CellAction, RowAction, ColumnAction, DataSetAction {
        private final ActionDefinition actionDefinition;

        ActionWithHelpLinkDefinition(ActionDefinition actionDefinition) {
            this.actionDefinition = actionDefinition;
        }

        public Class<? extends ActionDefinition> getActionDefinitionClassName() {
            return actionDefinition.getClass();
        }

        public ActionDefinition getActionDefinition() {
            return actionDefinition;
        }

        @Override
        public String getName() {
            return actionDefinition.getName();
        }

        @Override
        public String getCategory() {
            return actionDefinition.getCategory();
        }

        @Override
        public String getLabel() {
            return actionDefinition.getLabel();
        }

        @Override
        public String getDescription() {
            return actionDefinition.getDescription();
        }

        @Override
        public String getDocUrl() {
            return help.getExactUrl() + actionDefinition.getDocUrl();
        }

        @Override
        public List<Parameter> getParameters() {
            return actionDefinition.getParameters();
        }

        @Override
        public Set<Behavior> getBehavior() {
            return actionDefinition.getBehavior();
        }

        @Override
        public Function<GenericRecord, GenericRecord> action(List<Parameter> parameters) {
            return actionDefinition.action(parameters);
        }

        @Override
        public boolean acceptScope(ScopeCategory scope) {
            return actionDefinition.acceptScope(scope);
        }

        @Override
        public ActionDefinition adapt(ColumnMetadata column) {
            return actionDefinition.adapt(column);
        }

        @Override
        public ActionDefinition adapt(ScopeCategory scope) {
            return actionDefinition.adapt(scope);
        }

        @Override
        public boolean acceptField(ColumnMetadata column) {
            return actionDefinition.acceptField(column);
        }

        @Override
        public void compile(ActionContext actionContext) {
            actionDefinition.compile(actionContext);
        }

        @Override
        public boolean implicitFilter() {
            return actionDefinition.implicitFilter();
        }

        @Override
        public void applyOnLine(DataSetRow row, ActionContext context) {
            if (actionDefinition instanceof RowAction) {
                ((RowAction) actionDefinition).applyOnLine(row, context);
            }
        }

        @Override
        public void applyOnCell(DataSetRow row, ActionContext context) {
            if (actionDefinition instanceof CellAction) {
                ((CellAction) actionDefinition).applyOnCell(row, context);
            }
        }

        @Override
        public void applyOnColumn(DataSetRow row, ActionContext context) {
            if (actionDefinition instanceof ColumnAction) {
                ((ColumnAction) actionDefinition).applyOnColumn(row, context);
            }
        }

        @Override
        public void applyOnDataSet(DataSetRow row, ActionContext context) {
            if (actionDefinition instanceof DataSetAction) {
                ((DataSetAction) actionDefinition).applyOnDataSet(row, context);
            }
        }
    }

    @Override
    public Stream<Class<? extends ActionDefinition>> getAll() {
        return actions.stream().map(ActionDefinition::getClass);
    }

    @Override
    public Stream<ActionDefinition> findAll() {
        return actions.stream();
    }
}
