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

package org.talend.dataprep.configuration;

import static java.nio.charset.StandardCharsets.*;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Bean that list certified and supported charsets.
 */
public class EncodingSupport {

    /** All supported charsets. */
    private static Set<Charset> SUPPORTED_CHARSETS;

    static {
        Set<Charset> charsetsInBuild = new LinkedHashSet<>();
        // preferred charsets we always want at the top of UI lists:
        charsetsInBuild.add(UTF_8);
        charsetsInBuild.add(UTF_16);
        charsetsInBuild.add(UTF_16LE);
        charsetsInBuild.add(Charset.forName("windows-1252"));
        charsetsInBuild.add(ISO_8859_1);
        charsetsInBuild.add(Charset.forName("x-MacRoman"));

        // and the other (and many) charsets nobody ever uses
        charsetsInBuild.addAll((Charset.availableCharsets().values()));
        SUPPORTED_CHARSETS = Collections.unmodifiableSet(charsetsInBuild);
    }

    /**
     * The list of supported charsets with promoted charsets first.
     *
     * @return The list of encodings in data prep may use but are without scope of extensive tests (supported, but not
     * certified).
     */
    public static Set<Charset> getSupportedCharsets() {
        return SUPPORTED_CHARSETS;
    }

}
