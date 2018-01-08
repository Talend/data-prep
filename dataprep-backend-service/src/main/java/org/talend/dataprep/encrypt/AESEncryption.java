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

package org.talend.dataprep.encrypt;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
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
     * Decrypts the password embedded in the supplied URI and returns the URI with its password decrypted.
     *
     * @param rawUri The URI that may or may not contain an encrypted password in its user info part.
     * @return the URI with a decrypted password.
     * @see URI#getUserInfo()
     */
    public static String decryptUriPassword(final String rawUri) {
        URI uri;
        try {
            uri = new URI(rawUri);
        } catch (URISyntaxException e) {
            LOGGER.info("Invalid URI {}", rawUri, e);
            return rawUri;
        }
        UserInfo userInfo = extractCredentials(uri);
        if (userInfo != null && userInfo.password != null) {
            try {
                userInfo.password = decrypt(userInfo.password);
                return setCredentials(uri, userInfo).toString();
            } catch (Exception e) {
                LOGGER.info("Could not decrypt URI password.");
                return rawUri;
            }
        } else {
            return rawUri;
        }
    }

    /**
     * Encrypt the password part of the URI if any and returns it.
     *
     * @param rawUri the URI that may contain a password in its user info part.
     * @return the URI with its password part, if any, encrypted.
     * @throws Exception if rawUri is not a valid URI or encryption fails.
     */
    public static String encryptUriPassword(final String rawUri) throws Exception {
        URI uri = new URI(rawUri);
        UserInfo userInfo = extractCredentials(uri);
        if (userInfo != null && userInfo.password != null) {
            userInfo.password = encrypt(userInfo.password);
            return setCredentials(uri, userInfo).toString();
        } else {
            return rawUri;
        }
    }

    private static UserInfo extractCredentials(URI uri) {
        String rawUserInfo = uri.getUserInfo();
        if (StringUtils.isNotBlank(rawUserInfo)) {
            String[] parts = rawUserInfo.split(":");
            if (parts.length > 1) {
                String part0 = parts[0];
                return new UserInfo(part0, rawUserInfo.substring(part0.length() + 1));
            }
        }
        return null;
    }

    private static URI setCredentials(URI uri, UserInfo userInfo) throws URISyntaxException {
        return new URIBuilder(uri).setUserInfo(userInfo.userName, userInfo.password).build();
    }

    private static class UserInfo {
        String userName;

        String password;

        public UserInfo(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
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
