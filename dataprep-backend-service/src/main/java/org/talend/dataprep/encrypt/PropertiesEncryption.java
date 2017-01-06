// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * See also the properties file <a href="https://docs.oracle.com/cd/E23095_01/Platform.93/ATGProgGuide/html/s0204propertiesfileformat01.html">
 * specification</a>.
 * </p>
 * <p>
 * Based on the apache {@link PropertiesConfiguration} for properties parsing and saving. This method tends to modify
 * the properties file layout.
 * </p>
 */
public class PropertiesEncryption {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesEncryption.class);

    /**
     * Reads the specified property file and then encrypts, if not already encrypted, the specified set of properties
     * and overwrites the specified property file.
     *
     * @param input the property file
     * @param mustBeEncrypted the set of properties that must be encrypted
     */
    public void encryptAndSave(String input, Set<String> mustBeEncrypted) {
        modifyAndSave(input, mustBeEncrypted, this::encryptIfNot);
    }

    /**
     * Reads the specified property file and then decrypts, if not already decrypted, the specified set of properties
     * and overwrites the specified property file.
     *
     * @param input the property file path
     * @param mustBeDecrypted the set of properties that must be encrypted
     */
    public void decryptAndSave(String input, Set<String> mustBeDecrypted) {
        modifyAndSave(input, mustBeDecrypted, this::decryptIfNot);
    }

    /**
     * Applies the specified function to the specified set of parameters contained in the input file.
     *
     * @param input The specified name of file to encrypt
     * @param mustBeModified the specified set of parameters
     * @param function the specified function to apply to the set of specified parameters
     */
    private void modifyAndSave(String input, Set<String> mustBeModified, Function<String, String> function) {
        Path inputFilePath = Paths.get(input);
        if (Files.exists(inputFilePath) && Files.isRegularFile(inputFilePath) && Files.isReadable(inputFilePath)) {
            try {
                Parameters params = new Parameters();
                FileBasedConfigurationBuilder<PropertiesConfiguration> builder = //
                        new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class) //
                                .configure(params.fileBased() //
                                        .setFile(inputFilePath.toFile())); //
                PropertiesConfiguration config = builder.getConfiguration();
                for (String key : mustBeModified) {
                    config.setProperty(key, function.apply(config.getString(key)));
                }
                builder.save();
            } catch (ConfigurationException e) {
                LOGGER.error("unable to read {} {}", input, e);
            }
        } else {
            LOGGER.debug("No readable file at {}", input);
        }
    }

    /**
     * Encrypts the specified string if it is not already encrypted and returns the encrypted string.
     *
     * @param input the specified input to be encrypted
     * @return the encrypted string
     */
    private String encryptIfNot(String input) {
        try {
            AESEncryption.decrypt(input);
            // If no exception is thrown it must be that it was already encrypted.
            return input;
        } catch (Exception e) {
            try {
                return AESEncryption.encrypt(input);
            } catch (Exception e1) {
                LOGGER.debug("Error encrypting value.", e1);
            }
        }
        return "";
    }

    /**
     * Decrypts the specified string if it is not already decrypted and returns the decrypted string.
     *
     * @param input the specified input to be decrypted
     * @return the decrypted string
     */
    private String decryptIfNot(String input) {
        try {
            return AESEncryption.decrypt(input);
        } catch (Exception e) {
            // Property was already decrypted
            LOGGER.debug("Trying to decrypt a non encrypted property.", e);
            return input;
        }
    }
}
