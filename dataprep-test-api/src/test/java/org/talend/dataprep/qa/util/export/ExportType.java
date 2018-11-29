package org.talend.dataprep.qa.util.export;

import java.util.Arrays;

public enum ExportType {
    CSV("CSV"), //
    EXCEL("EXCEL"), //
    TABLEAU("TABLEAU"), //
    XLSX("XLSX"), //
    HDFS("HDFS"), //
    AMAZON_S3("AmazonS3"), // Export is run on TDP runtime
    AMAZON_S3_DI("AmazonS3DI"); // Export is run on Hadoop Cluster runtime

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
    public static ExportType getExportType(String pName) {
        return Arrays
                .stream(ExportType.values()) //
                .filter(e -> e.name.equalsIgnoreCase(pName)) //
                .findFirst() //
                .orElse(null);
    }

    public String getName() {
        return name;
    }
}
