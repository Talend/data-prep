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

package org.talend.dataprep.configuration;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Bean that list certified and supported charsets.
 */
@Component
@SuppressWarnings("InsufficientBranchCoverage")
public class EncodingSupport {

    /**
     * The list of supported encodings in data prep (could be {@link Charset#availableCharsets()}, but requires
     * extensive tests, so a sub set is returned to ease testing).
     * 
     * @see #getSupportedCharsets()
     */
    @Value("#{'UTF-8,UTF-16,UTF-16LE,windows-1252,ISO-8859-1,x-MacRoman'.split(',')}")
    private String[] certifiedEncoding;

    /** All supported charsets. */
    private Set<Charset> supportedCharsets;

    /**
     * Init the charset lists.
     */
    @PostConstruct
    @SuppressWarnings("unchecked")
    private void initCharsets() {
        final List<Charset> certifiedCharsets = Arrays.stream(certifiedEncoding).map(Charset::forName)
                .collect(Collectors.toList());
        this.supportedCharsets = new LinkedHashSet<>(
                ListUtils.union(certifiedCharsets, new ArrayList<>(Charset.availableCharsets().values())));
    }

    /**
     * @return The list of encodings in data prep may use but are without scope of extensive tests (supported, but not
     * certified).
     * @see #certifiedEncoding
     */
    public Set<Charset> getSupportedCharsets() {
        return supportedCharsets;
    }

}
