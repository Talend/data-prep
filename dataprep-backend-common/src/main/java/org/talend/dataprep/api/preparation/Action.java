package org.talend.dataprep.api.preparation;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.annotation.Transient;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Class used to wrap DataSetRowAction into json.
 */
@JsonRootName("action")
public class Action implements Serializable {

    /** Default noop action. */
    public static final DataSetRowAction IDLE_ROW_ACTION = (row, context) -> row;

    /** The wrapped row action. */
    @Transient
    private final transient DataSetRowAction rowAction;

    /** Json description of the action. */
    private String action;

    /** Parameters needed for the action. */
    private MixedContentMap parameters = new MixedContentMap();

    /**
     * Default empty constructor.
     */
    public Action() {
        rowAction = IDLE_ROW_ACTION;
    }

    /**
     * Create an Action from the given RowAction.
     * 
     * @param rowAction the row action to build the Action from.
     */
    public Action(DataSetRowAction rowAction) {
        this.rowAction = rowAction;
    }

    /**
     * @return the json description of the action.
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action the json description of the action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return the action parameters.
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the action parameters to set.
     */
    public void setParameters(MixedContentMap parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the row action.
     */
    @JsonIgnore(true)
    @Transient
    public DataSetRowAction getRowAction() {
        return rowAction;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Action action1 = (Action) o;
        return Objects.equals(action, action1.action) && Objects.equals(parameters, action1.parameters);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(rowAction, action, parameters);
    }

    /**
     * Builder used to ease the Action creation.
     */
    public static class Builder {

        /** The default noop action. */
        private DataSetRowAction rowAction = IDLE_ROW_ACTION;

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
        public Action build() {
            return new Action(rowAction);
        }

    }
}
