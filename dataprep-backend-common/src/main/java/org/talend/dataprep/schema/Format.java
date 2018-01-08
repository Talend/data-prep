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

/**
 * Represents an accurate format which is a two-part identifier composed of a family format (xls, csv, html, ...) and an
 * encoding. This format may or may not be supported by Data prep. Notice that this class is immutable.
 */
public class Format {

    /**
     * The family of the format e.g. CSV, XLS, HTML or unsupported family
     */
    private final FormatFamily formatFamily;

    /**
     * The encoding of the format, e.g. UTF-8, windows-1252, ...
     */
    private final String encoding;

    /**
     * Constructs a format with given arguments
     * 
     * @param formatFamily the format family
     * @param encoding the encoding
     */
    public Format(FormatFamily formatFamily, String encoding) {
        this.formatFamily = formatFamily;
        this.encoding = encoding;
    }

    /**
     *
     * @return the format family
     */
    public FormatFamily getFormatFamily() {
        return formatFamily;
    }

    /**
     *
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }
}
