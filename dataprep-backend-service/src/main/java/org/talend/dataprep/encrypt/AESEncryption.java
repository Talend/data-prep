// ============================================================================
//
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

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a helper class to encrypt and decrypt a given string with the
 * <a href="https://en.wikipedia.org/wiki/Advanced_Encryption_Standard">AES (Advanced Encryption Standard)</a> and
 * chosen secret key.
 */
public class AESEncryption {

    private static final Logger LOGGER = LoggerFactory.getLogger(AESEncryption.class);

    private static final String ALGO = "AES";

    private static final String ENCODING = "UTF-8";

    private static Key secretKey;

    static {
        byte[] defaultValue;
        try {
            defaultValue = "DataPrepIsSoCool".getBytes(ENCODING);
        } catch (UnsupportedEncodingException e) {
            defaultValue = "DataPrepIsSoCool".getBytes();
            LOGGER.debug("Unable to find Encoding {}", ENCODING, e);
        }
        try {
            secretKey = generateKey(defaultValue);
        } catch (Exception e) {
            LOGGER.warn("Unable to generate the key used for AES", e);
        }
    }

    /**
     * Private default constructor.
     */
    private AESEncryption() {
        // private constructor to ensure the utility style of this class
    }

    /**
     * Encrypts the specified string and returns its encrypted value.
     * 
     * @param src the specified {@link String}
     * @return the encrypted value of the specified {@link String}
     * @throws Exception
     */
    public static String encrypt(final String src) throws Exception {
        final Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, secretKey);
        final byte[] encVal = c.doFinal(src.getBytes());
        return new String(Base64.getEncoder().encode(encVal));
    }

    /**
     * Decrypts the specified string (which is supposed to be encrypted) and returns its original value.
     * 
     * @param src the specified {@link String}
     * @return the decrypted value of the specified {@link String}
     * @throws Exception
     */
    public static String decrypt(final String src) throws Exception {
        final Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, secretKey);
        final byte[] decodedValue = Base64.getDecoder().decode(src);
        final byte[] decValue = c.doFinal(decodedValue);
        return new String(decValue, ENCODING);
    }

    /**
     * Return the decrypted string or the original value if needed.
     * 
     * @param name the string name to decrypt (useful for debugging purpose)
     * @param src the string to decrypt.
     * @return the decrypted string or the original value if needed.
     */
    public static String decrypt(final String name, final String src) {
        try {
            return decrypt(src);
        } catch (Exception e) {
            LOGGER.debug("could not decrypt {}, return it as it is", name);
            return src;
        }

    }

    /**
     * Generates the key used to encrypt and decrypt.
     *
     * @return the key used to encrypt and decrypt
     * @throws Exception
     */
    private static Key generateKey(byte[] defaultValue) throws Exception {
        return new SecretKeySpec(defaultValue, ALGO);
    }
}
