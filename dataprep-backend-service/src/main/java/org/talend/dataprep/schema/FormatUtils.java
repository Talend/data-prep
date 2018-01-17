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

package org.talend.dataprep.schema;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Many constants used during format detection.
 */
public class FormatUtils {

    /**
     * Buffer sizes used to detect CSV and Html formats
     */
    public static final int META_TAG_BUFFER_SIZE = 8192;

    /**
     * ASCII charset used to detect CSV and Html formats
     */
    public static final Charset ASCII = Charset.forName("US-ASCII");

    /**
     * The default encoding, for instance it is used for xls format
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * The default encoding used for unknown media type
     */
    public static final String UNKNOWN_MEDIA_TYPE = "application/octet-stream";

    /**
     * No arg constructor
     */
    private FormatUtils() {
    }

    /**
     * Returns a string read from a buffer of bytes after replacing null character by the empty string.
     * 
     * @param buffer the byte buffer
     * @param start the index from which to start reading
     * @param end the index at which to stop reading
     * @return a string read from a buffer of bytes after replacing null character by the empty string
     */
    public static String readFromBuffer(byte[] buffer, int start, int end) {
        String result = ASCII.decode(ByteBuffer.wrap(buffer, start, end)).toString();
        Character c = Character.MIN_CODE_POINT;
        result = result.replaceAll(c.toString(), "");
        return result;
    }

}
