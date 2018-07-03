package org.talend.dataprep.transformation.actions.common;

import java.math.BigInteger;
import java.util.Map;

public abstract class AbstractGenerateSequenceAction extends AbstractActionMetadata implements ColumnAction {
    /** The starting value of sequence */
    public static final String START_VALUE = "start_value";

    /** The step value of sequence */
    public static final String STEP_VALUE = "step_value";

    /** The next value of sequence to calculate */
    protected static final String SEQUENCE = "sequence";

    /** this class is used to calculate the sequence next step */
    public static class CalcSequence {

        BigInteger nextValue;

        BigInteger step;

        public CalcSequence(Map<String, String> parameters) {
            this.nextValue = new BigInteger(parameters.get(START_VALUE));
            this.step = new BigInteger(parameters.get(STEP_VALUE));
        }

        public String getNextValue() {
            String toReturn = nextValue.toString();
            nextValue = nextValue.add(step);
            return toReturn;
        }

    }
}
