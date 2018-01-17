//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.format;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.format.export.ExportFormat;

/**
 * Internal service in charge of format type registration.
 */
@Service
public class FormatRegistrationService {

    /** List of internal format types. */
    private static final List<String> INTERNAL_TYPES = Collections.singletonList(JsonFormat.JSON);

    /** List of available format types. */
    @Autowired
    private List<ExportFormat> types;

    /**
     * Return external formats.
     */
    public Stream<ExportFormat> getExternalFormats() {
        return types.stream().filter(format -> !INTERNAL_TYPES.contains(format.getName()));
    }

    /**
     * Return a registered format from its name or null if none found.
     * 
     * @param formatName the wanted format name.
     * @return the wanted export format or null if not found.
     */
    public ExportFormat getByName(String formatName) {
        final Optional<ExportFormat> format = types.stream().filter(f -> StringUtils.equalsIgnoreCase(formatName, f.getName())).findFirst();
        return format.isPresent() ? format.get() : null;
    }
}
