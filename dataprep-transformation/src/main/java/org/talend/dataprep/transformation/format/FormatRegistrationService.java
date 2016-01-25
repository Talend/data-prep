package org.talend.dataprep.transformation.format;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private static final List<String> INTERNAL_TYPES = Arrays.asList(new String[] { JsonFormat.JSON });

    /** List of available format types. */
    @Autowired
    private List<ExportFormat> types;

    /**
     * Return external formats.
     */
    public List<ExportFormat> getExternalFormats() {
        return types.stream().filter(format -> !INTERNAL_TYPES.contains(format.getName())).collect(Collectors.toList());
    }

    /**
     * Return a registered format from its name or null if none found.
     * 
     * @param formatName the wanted format name.
     * @return the wanted export format or null if not found.
     */
    public ExportFormat getByName(String formatName) {
        final Optional<ExportFormat> format = types.stream().filter(f -> StringUtils.equals(formatName, f.getName())).findFirst();
        return format.isPresent() ? format.get() : null;
    }
}
