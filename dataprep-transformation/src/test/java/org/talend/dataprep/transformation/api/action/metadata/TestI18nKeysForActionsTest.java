//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.metadata;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.transformation.FailedAction;
import org.talend.dataprep.transformation.TransformationBaseTest;
import org.talend.dataprep.transformation.TransformationFailureAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;

/**
 * Test that a translation exists for i18n keys label/desc for each action and each params/item.
 */
public class TestI18nKeysForActionsTest extends TransformationBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestI18nKeysForActionsTest.class);

    @Autowired
    private ActionMetadata[] allActions;

    private void assertI18nKeyExists(final String label) {
        if(label.startsWith("action.")
                && !label.contains(FailedAction.FAILED_ACTION)
                && !label.contains(TransformationFailureAction.TRANSFORMATION_FAILURE_ACTION)) {
            fail("missing key <" + label + ">");
        }
    }

    @Test
    public void test() {
        for (ActionMetadata actionMetadata : allActions) {
            final String name = actionMetadata.getName();
            assertNotNull(name);
            assertNotEquals("", name);

            String label = actionMetadata.getLabel();
            assertNotNull(label);
            assertNotEquals("", label);
            assertI18nKeyExists(label);

            String desc = actionMetadata.getDescription();
            assertNotNull(desc);
            assertNotEquals("", desc);
            assertI18nKeyExists(desc);

            String toString = actionMetadata.getName() + "," + actionMetadata.getCategory() + "," + actionMetadata.getLabel()
                    + "," + actionMetadata.getDescription();
            LOGGER.info(toString);

            for (Parameter param : actionMetadata.getParameters()) {
                String pname = param.getName();
                assertNotNull(pname);
                assertNotEquals("", pname);

                String plabel = param.getLabel();
                assertNotNull(plabel);
                assertNotEquals("", plabel);
                assertI18nKeyExists("parameter." + pname + ".label");

                String pdesc = param.getDescription();
                assertNotNull(pdesc);
                assertNotEquals("", pdesc);
                assertI18nKeyExists("parameter." + pname + ".desc");

                LOGGER.trace("  - " + pname + " | " + plabel + " | " + pdesc);

                if (param instanceof SelectParameter) {

                    List<SelectParameter.Item> values = (List<SelectParameter.Item>) param.getConfiguration().get("values");

                    for (SelectParameter.Item value : values) {
                        LOGGER.trace("    - " + value);


                        for (Parameter inlineParam : value.getInlineParameters()) {

                            String oname = inlineParam.getName();
                            assertNotNull(oname);
                            assertNotEquals("", oname);

                            String olabel = inlineParam.getLabel();
                            assertNotNull(olabel);
                            assertNotEquals("", olabel);
                            assertI18nKeyExists("parameter." + oname + ".label");

                            String odesc = inlineParam.getDescription();
                            assertNotNull(odesc);
                            assertNotEquals("", odesc);
                            assertI18nKeyExists("parameter." + oname + ".desc");

                            LOGGER.trace("      - " + oname + " | " + olabel + " | " + odesc);
                        }

                    }

                }
            }
            LOGGER.debug("");
        }
    }

}
