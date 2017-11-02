package org.talend.dataprep.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.talend.dataprep.exception.error.CommonErrorCodes;

public class DataprepBundleTest {

    private static Properties messagesProperties;

    @BeforeClass
    public static void setUpClass() throws IOException {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        messagesProperties = new Properties();
        messagesProperties.load(DataprepBundleTest.class.getResourceAsStream("/org/talend/dataprep/error_messages.properties"));
        messagesProperties.load(DataprepBundleTest.class.getResourceAsStream("/org/talend/dataprep/messages.properties"));

    }

    @Test
    public void message() throws Exception {
        String key = "export.CSV.title";
        assertEquals(messagesProperties.getProperty(key), DataprepBundle.message(key));
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
