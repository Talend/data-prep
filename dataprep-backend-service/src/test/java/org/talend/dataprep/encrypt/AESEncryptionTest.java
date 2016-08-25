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

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.IllegalBlockSizeException;

public class AESEncryptionTest {

    @Test
    public void should_get_the_same_string_after_encrypt_then_decrypt() throws Exception {
        // given
        String src = "Dataprep";

        // when
        String encrypted = AESEncryption.encrypt(src);
        String decrypted = AESEncryption.decrypt(encrypted);

        // then
        Assert.assertEquals(src, decrypted);
    }

    @Test(expected = IllegalBlockSizeException.class)
    public void should_throw_an_exception_when_trying_to_decrypt_a_non_encrypted_value() throws Exception {
        // given
        String src = "Dataprep";

        // when
        String encrypted = AESEncryption.decrypt(src);

        // then
        Assert.fail("should not reach this point");
    }

}