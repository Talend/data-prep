package org.talend.dataprep.schema;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FormatFamilyFactory {

    @Autowired
    private Map<String, FormatFamily> formatFamilyMap;

    public FormatFamily getFormatFamily(String formatFamilyId) {
        return formatFamilyMap.get(formatFamilyId);
    }

    public boolean hasFormatFamily(String formatFamilyId) {
        return formatFamilyMap.containsKey(formatFamilyId);
    }

}
