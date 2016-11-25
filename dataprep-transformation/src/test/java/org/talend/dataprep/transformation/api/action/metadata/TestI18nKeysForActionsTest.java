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
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.parameters.Item;
import org.talend.dataprep.parameters.LocalizedItem;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.TransformationBaseTest;

/**
 * Test that a translation exists for i18n keys label/desc for each action and each params/item.
 */
public class TestI18nKeysForActionsTest extends TransformationBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestI18nKeysForActionsTest.class);

    @Autowired
    private ActionDefinition[] allActions;

    @Test
    public void allActionShouldHaveTranslations() {
        for (ActionDefinition actionMetadata : allActions) {
            final String name = actionMetadata.getName();
            assertNotNull(name);
            assertNotEquals("", name);

            actionMetadata.getLabel();
            actionMetadata.getDescription();

            String toString = actionMetadata.getName() + "," + actionMetadata.getCategory() + "," + actionMetadata.getLabel()
                    + "," + actionMetadata.getDescription();
            LOGGER.info(toString);

            for (Parameter param : actionMetadata.getParameters()) {
                assertParameter(param);
            }
        }
    }

    private void assertParameter(Parameter param) {
        String parameterName = param.getName();
        assertNotNull(parameterName);
        assertNotEquals("", parameterName);

        String parameterLabel = param.getLabel();
        assertNotNull(parameterLabel);
        assertNotEquals("", parameterLabel);

        String parameterDescription = param.getDescription();
        assertNotNull(parameterDescription);
        assertNotEquals("", parameterDescription);

        LOGGER.trace("  - " + parameterName + " | " + parameterLabel + " | " + parameterDescription);
        if (param instanceof SelectParameter) {
            List<Item> values = ((SelectParameter) param).getItems();
            for (Item value : values) {
                assertItem(value);
            }
        }
    }

    private void assertItem(Item value) {
        LOGGER.trace("    - " + value);

        if (value instanceof LocalizedItem) {
            try {
                LOGGER.trace("    - " + value.getLabel());
            } catch (Exception e) {
                fail("missing key <" + value.getValue() + ">");
            }
        }

        for (Parameter inlineParam : value.getParameters()) {
            String name = inlineParam.getName();
            String label = inlineParam.getLabel();
            String desc = inlineParam.getDescription();
            LOGGER.trace("      - " + name + " | " + label + " | " + desc);
        }
    }

}
