// ============================================================================
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

package org.talend.dataprep.io;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class CloseableInputStreamComponent {

    public InputStream getInput() {
        return new NullInputStream(0);
    }

    public OutputStream getOutput() {
        return new NullOutputStream();
    }

    public Closeable getNull() {
        return null;
    }

    public Closeable getException() {
        throw new RuntimeException("On purpose thrown unchecked exception.");
    }

    public Closeable getUnknownCloseable() {
        return () -> {
            // Nothing to do
        };
    }
}
