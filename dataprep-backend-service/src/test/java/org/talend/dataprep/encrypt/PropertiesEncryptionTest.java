// ============================================================================
//
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

package org.talend.dataprep.encrypt;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

public class PropertiesEncryptionTest {

    @Test
    public void shouldEncryptAndSaveAllOccurrences() throws Exception {
        // given
        String propertyKey = "admin.password";
        String propertyValue = "5ecr3t";
        String propertyEncodedValue = "JP6lC6hVeu3wRZA1Tzigyg==";


        String mongoPropName = "mongodb.uri";
        String mongoPropValue = "mongodb://userName:5ecr3t@dataprep-server.org:27017/dataprep?toto=tata";
        String mongoPropValueEncrypted = "mongodb://userName:JP6lC6hVeu3wRZA1Tzigyg==@dataprep-server.org:27017/dataprep?toto=tata";

        Path tempFile = Files.createTempFile("dataprep-PropertiesEncryptionTest.", ".properties");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(tempFile, Charsets.UTF_8)) {
            for (int i = 0; i < 5; i++) {
                bufferedWriter.write(propertyKey + "=" + propertyValue);
                bufferedWriter.newLine();
            }
            bufferedWriter.write(mongoPropName + "=" + mongoPropValue);
        }

        // when
        new PropertiesEncryption().encryptAndSave(tempFile.toString(), Sets.newHashSet(propertyKey, mongoPropName));

        // then
        Properties properties = new Properties();
        properties.load(new FileInputStream(tempFile.toFile()));
        assertEquals(propertyEncodedValue, properties.getProperty(propertyKey));
        assertEquals(mongoPropValueEncrypted, properties.getProperty(mongoPropName));
    }

    @Test
    public void shouldNotEncryptCommentedProperties() throws Exception {
        // given
        String commentedLines = "# admin.password = administrator password";

        Path tempFile = Files.createTempFile("dataprep-PropertiesEncryptionTest.", ".properties");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(tempFile, Charsets.UTF_8)) {
            bufferedWriter.write("# file.password = the file password");
            bufferedWriter.newLine();
            bufferedWriter.write("file = /tmp");
            bufferedWriter.newLine();
        }

        // when
        new PropertiesEncryption().encryptAndSave(tempFile.toString(), Sets.newHashSet("file.password"));

        // then
        try (BufferedReader reader = Files.newBufferedReader(tempFile)) {
            String line = reader.readLine();
            assertEquals("# file.password = the file password", line);
            line = reader.readLine();
            assertEquals("file = /tmp", line);
        }
    }

    @Test
    public void decryptAndSave() throws Exception {
        // given
        String propertyKey = "admin.password";
        String propertyEncodedValue = "JP6lC6hVeu3wRZA1Tzigyg==";
        String propertyValue = "5ecr3t";
        Path tempFile = Files.createTempFile("dataprep-PropertiesEncryptionTest.", ".properties");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(tempFile, Charsets.UTF_8)) {
            for (int i = 0; i < 5; i++) {
                bufferedWriter.write(propertyKey + "=" + propertyEncodedValue);
                bufferedWriter.newLine();
            }
        }

        // when
        new PropertiesEncryption().decryptAndSave(tempFile.toString(), Sets.newHashSet(propertyKey));

        // then
        final String expectedLine = propertyKey + "=" + propertyValue;
        verifyContent(expectedLine, tempFile);
    }

    @Test
    public void encryptAndSave_doesNotBreakLayout() throws Exception {
        Path tempFile = Files.createTempFile("dataprep-PropertiesEncryptionTest.", ".properties");
        try (InputStream refInStream = getClass().getResourceAsStream("keep-layout-test-in.properties")) {
            Files.copy(refInStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        new PropertiesEncryption().encryptAndSave(tempFile.toString(), Sets.newHashSet("admin.password"));

        assertEquals(Resources.toString(getClass().getResource("keep-layout-test-expected.properties"), Charsets.UTF_8),
                com.google.common.io.Files.toString(tempFile.toFile(), Charsets.UTF_8));

    }

    @Test
    public void shouldEncryptAllKeysButMissingOnes() throws Exception {
        // given
        String propertyKey = "admin.password";
        String missingPropertyKeyOne = "missingOne";
        String missingPropertyKeyTwo = "missingTwo";
        String propertyValue = "5ecr3t";
        String propertyEncodedValue = "JP6lC6hVeu3wRZA1Tzigyg==";
        Path tempFile = Files.createTempFile("dataprep-PropertiesEncryptionTest.", ".properties");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(tempFile, Charsets.UTF_8)) {
            for (int i = 0; i < 5; i++) {
                bufferedWriter.write(propertyKey + "=" + propertyValue);
                bufferedWriter.newLine();
            }
        }

        // when
        new PropertiesEncryption().encryptAndSave(tempFile.toString(),
                Sets.newHashSet(propertyKey, missingPropertyKeyOne, missingPropertyKeyTwo));

        // then
        final String expectedLine = propertyKey + "=" + propertyEncodedValue;
        verifyContent(expectedLine, tempFile);
    }

    private void verifyContent(String expectedLine, Path tempFile) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(tempFile)) {
            String line = reader.readLine();
            while (line != null) {
                assertEquals(expectedLine, line);
                line = reader.readLine();
            }
        }
    }
}
