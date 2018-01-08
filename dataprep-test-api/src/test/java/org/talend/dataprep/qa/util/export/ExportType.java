package org.talend.dataprep.qa.util.export;

import java.util.Arrays;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum ExportType {
    CSV("CSV"), //
    EXCEL("EXCEL"), //
    TABLEAU("TABLEAU"), //
    XLSX("XLSX"), //
    HDFS("HDFS"); //

    private String name;

    ExportType(String pName) {
        name = pName;
    }

    /**
     * Get a corresponding {@link ExportType} from a {@link String}.
     *
     * @param pName the {@link ExportType#name}.
     * @return the corresponding {@link ExportType} or <code>null</code> if there isn't.
     */
    @Nullable
    public static ExportType getExportType(@NotNull String pName) {
        return Arrays.stream(ExportType.values()) //
                .filter(e -> e.name.equalsIgnoreCase(pName)) //
                .findFirst() //
                .orElse(null);
    }

    public String getName() {
        return name;
    }
}
