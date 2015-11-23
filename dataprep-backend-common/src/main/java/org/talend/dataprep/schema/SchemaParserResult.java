package org.talend.dataprep.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.talend.dataprep.api.dataset.ColumnMetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SchemaParserResult implements Serializable {

    @JsonProperty("draft")
    private boolean draft;

    @JsonProperty("sheetContents")
    private List<SheetContent> sheetContents;

    @JsonProperty("sheetName")
    private String sheetName;

    private SchemaParserResult(boolean draft, List<SheetContent> sheetContents, String sheetName) {
        this.draft = draft;
        this.sheetContents = sheetContents;
        this.sheetName = sheetName;
    }

    private SchemaParserResult() {
        //
    }

    public boolean draft() {
        return draft;
    }

    public List<SheetContent> getSheetContents()
    {
        return sheetContents;
    }

    public String getSheetName() {
        return sheetName;
    }

    /**
     * Xls sheet content.
     */
    public static class SheetContent implements Serializable {

        /** The sheet name. */
        @JsonProperty("name")
        private String name;

        /** List of column metadata. */
        @JsonProperty("columnMetadatas")
        private List<ColumnMetadata> columnMetadatas;

        /**
         * Default empty constructor.
         */
        public SheetContent() {
            // no op
        }

        /**
         * Constructor.
         *
         * @param name the sheet name.
         * @param columnMetadatas the list of column metadata.
         */
        public SheetContent(String name, List<ColumnMetadata> columnMetadatas) {
            this.name = name;
            this.columnMetadatas = columnMetadatas;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<ColumnMetadata> getColumnMetadatas() {
            return columnMetadatas;
        }

        public void setColumnMetadatas(List<ColumnMetadata> columnMetadatas) {
            this.columnMetadatas = columnMetadatas;
        }

        @Override
        public SheetContent clone() {
            List<ColumnMetadata> columns = null;
            if (this.columnMetadatas != null) {
                columns = new ArrayList<>();
                for (ColumnMetadata column : this.columnMetadatas) {
                    columns.add(ColumnMetadata.Builder.column().copy(column).build());
                }
            }
            return new SheetContent(this.name, columns);
        }

        @Override
        public String toString() {
            return "SheetContent{" + "name='" + name + '\'' + ", columnMetadatas=" + columnMetadatas + '}';
        }
    }

    @Override
    public String toString()
    {
        return "SchemaParserResult{" +
            "draft=" + draft +
            ", sheetContents=" + sheetContents +
            ", sheetName='" + sheetName + '\'' +
            '}';
    }

    public static class Builder {

        private boolean draft;

        private List<SheetContent> sheetContents;

        private String sheetName;

        public static SchemaParserResult.Builder parserResult() {
            return new Builder();
        }

        public Builder draft(boolean draft) {
            this.draft = draft;
            return this;
        }

        public Builder sheetContents(List<SheetContent> sheetContents) {
            if (sheetContents == null) {
                return this;
            }
            this.sheetContents = new ArrayList<>();
            for (SheetContent content : sheetContents) {
                this.sheetContents.add(content.clone());
            }
            return this;
        }

        public Builder sheetName(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        public Builder copy(SchemaParserResult original) {
            this.draft = original.draft();
            this.sheetContents = original.getSheetContents();
            this.sheetName = original.getSheetName();
            return this;
        }

        public SchemaParserResult build() {
            return new SchemaParserResult(this.draft, this.sheetContents, this.sheetName);
        }
    }
}
