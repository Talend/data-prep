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

package org.talend.dataprep.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.test.SpringLocalizationRule;

public class DataprepBundleTest {

    private static Properties messagesProperties;

    @Rule
    public SpringLocalizationRule rule = new SpringLocalizationRule(Locale.FRANCE);

    @BeforeClass
    public static void setUpClass() throws IOException {
        messagesProperties = new Properties();
        messagesProperties
                .load(DataprepBundleTest.class.getResourceAsStream("/org/talend/dataprep/error_messages_fr.properties"));
        messagesProperties.load(DataprepBundleTest.class.getResourceAsStream("/org/talend/dataprep/messages_fr.properties"));

    }

    @Test
    public void message() throws Exception {
        String key = "export.CSV.title";
        assertEquals(messagesProperties.getProperty(key), DataprepBundle.message(key));
    }

    @Test
    public void messageDefault() throws Exception {
        Locale.setDefault(Locale.US);
        assertEquals("Export to CSV", DataprepBundle.defaultMessage("export.CSV.title"));
    }

    @Test
    public void errorTitle() throws Exception {
        String producedKey = CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST.name() + ".TITLE";
        assertEquals(messagesProperties.getProperty(producedKey),
                DataprepBundle.errorTitle(CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST));
    }

    @Test
    public void getDataprepBundle() throws Exception {
        assertNotNull(DataprepBundle.getDataprepBundle());
    }
}
