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
package org.talend.dataprep.transformation.format;

import java.io.PrintWriter;
import java.io.Writer;

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

    public void writeNext(String[] nextLine, Boolean[] isEnclosed) {
        if (nextLine != null && isEnclosed != null && nextLine.length == isEnclosed.length) {
            StringBuilder sb = new StringBuilder(128);

            for (int i = 0; i < nextLine.length; ++i) {
                if (i != 0) {
                    sb.append(this.separator);
                }

                String nextElement = nextLine[i];
                Boolean elementIsEnclosed = isEnclosed[i];
                if (nextElement != null) {
                    // starting enclosure
                    if (this.quotechar != 0 && elementIsEnclosed) {
                        sb.append(this.quotechar);
                    }
                    // escaping values
                    sb.append(this.stringContainsSpecialCharacters(nextElement) ? this.processLine(nextElement) : nextElement);
                    // ending enclosure
                    if (this.quotechar != 0 && elementIsEnclosed) {
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
