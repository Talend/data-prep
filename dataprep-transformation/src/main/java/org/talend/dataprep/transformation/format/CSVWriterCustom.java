// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.transformation.format;

import java.io.PrintWriter;
import java.io.Writer;

import org.talend.dataprep.api.type.Type;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVWriterCustom extends CSVWriter {

    private PrintWriter pw;

    private char separator;

    private char quotechar;

    private char escapechar;

    private String lineEnd;

    public CSVWriterCustom(Writer writer, char separator, char quotechar, char escapechar) {
        super(writer, separator, quotechar, escapechar);
        this.pw = new PrintWriter(writer);
        this.separator = separator;
        this.quotechar = quotechar;
        this.escapechar = escapechar;
        this.lineEnd = DEFAULT_LINE_END;
    }

    public void writeNext(String[] nextLine, String[] valuesTypes) {
        if (nextLine != null && valuesTypes != null && nextLine.length <= valuesTypes.length) {
            StringBuilder sb = new StringBuilder(128);

            for (int i = 0; i < nextLine.length; ++i) {
                if (i != 0) {
                    sb.append(this.separator);
                }

                String nextElement = nextLine[i];

                String nextElementType = valuesTypes[i];
                Boolean applyEnclosure = nextElementType.equals(Type.STRING.getName());
                if (nextElement != null) {
                    if (this.quotechar != 0 && applyEnclosure) {
                        sb.append(this.quotechar);
                    }

                    sb.append(this.stringContainsSpecialCharacters(nextElement) ? this.processLine(nextElement) : nextElement);
                    if (this.quotechar != 0 && applyEnclosure) {
                        sb.append(this.quotechar);
                    }
                }
            }

            sb.append(this.lineEnd);
            this.pw.write(sb.toString());
        }
    }

    private boolean stringContainsSpecialCharacters(String line) {
        return line.indexOf(this.quotechar) != -1 || line.indexOf(this.escapechar) != -1;
    }
}
