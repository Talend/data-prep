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

import java.io.*;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.talend.daikon.content.DeletableResource;

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

    public DeletableResource getDeletableResource() {
        return new DeletableResource() {

            private NullOutputStream nullOutputStream = new NullOutputStream();

            private NullInputStream nullInputStream = new NullInputStream(0);

            @Override
            public void delete() throws IOException {

            }

            @Override
            public void move(String s) throws IOException {

            }

            @Override
            public boolean isWritable() {
                return false;
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return nullOutputStream;
            }

            @Override
            public boolean exists() {
                return false;
            }

            @Override
            public boolean isReadable() {
                return false;
            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public URL getURL() throws IOException {
                return null;
            }

            @Override
            public URI getURI() throws IOException {
                return null;
            }

            @Override
            public File getFile() throws IOException {
                return null;
            }

            @Override
            public long contentLength() throws IOException {
                return 0;
            }

            @Override
            public long lastModified() throws IOException {
                return 0;
            }

            @Override
            public Resource createRelative(String relativePath) throws IOException {
                return null;
            }

            @Override
            public String getFilename() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return nullInputStream;
            }
        };
    }
}
