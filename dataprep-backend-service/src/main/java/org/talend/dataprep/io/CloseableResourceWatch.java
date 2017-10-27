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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.scheduling.annotation.Scheduled;
import org.talend.daikon.content.DeletableResource;

/**
 * This class configures an aspect around methods that <b>return</b> a {@link Closeable closeable} implementation.
 * It currently supports:
 * <ul>
 *     <li>{@link InputStream}</li>
 *     <li>{@link OutputStream}</li>
 * </ul>
 * To activate this watcher, logging framework must enable "org.talend.dataprep.io" in debug level.
 */
@Configuration
@Aspect
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Conditional(CloseableResourceWatch.class)
public class CloseableResourceWatch implements Condition {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloseableResourceWatch.class);

    private final Set<CloseableHandler> entries = Collections.synchronizedSet(new HashSet<>());

    @Around("within(org.talend..*) && (execution(public java.io.Closeable+ *(..)) || execution(public org.talend.daikon.content.DeletableResource+ *(..)))")
    public Object closeableWatch(ProceedingJoinPoint pjp) throws Throwable {
        final Object proceed = pjp.proceed();
        if (proceed == null) {
            LOGGER.warn("Unable to watch null closeable.");
            return null;
        }
        try {
            if (proceed instanceof InputStream) {
                final CloseableHandler handler = new InputStreamHandler((InputStream) proceed);
                entries.add(handler);
                return handler;
            } else if (proceed instanceof OutputStream) {
                final CloseableHandler handler = new OutputStreamHandler((OutputStream) proceed);
                entries.add(handler);
                return handler;
            } else if (proceed instanceof DeletableResource) {
                return new DeletableResourceProxy((DeletableResource) proceed);
            } else {
                LOGGER.warn("No watch for '{}'.", proceed);
                return proceed;
            }
        } catch (Exception e) {
            if (!LOGGER.isDebugEnabled()) {
                LOGGER.error("Unable to watch resource '{}'.", proceed);
            } else {
                LOGGER.debug("Unable to watch resource '{}'.", proceed, e);
            }
        }
        return proceed;
    }

    public Set<CloseableHandler> getEntries() {
        return entries;
    }

    private boolean remove(CloseableHandler handler) {
        return entries.remove(handler);
    }

    /**
     * A clean up process that starts a minute after the previous ended.
     */
    @Scheduled(fixedDelay = 30000)
    public void log() {
        synchronized (entries) {
            LOGGER.info("Logging closeable resources ({} opened resources)...", entries.size());
            for (CloseableHandler entry : entries) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("{}", entry.format());
                } else {
                    LOGGER.info("{}", entry);
                }
            }
            LOGGER.info("Done logging closeable resources ({} opened resources).", entries.size());
        }
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return context.getEnvironment().getProperty("dataprep.io.watch", Boolean.class, Boolean.FALSE) || LOGGER.isDebugEnabled();
    }

    public interface CloseableHandler {

        RuntimeException getCaller();

        long getCreation();

        Closeable getCloseable();

        String getId();

        default String format() {
            StringWriter writer = new StringWriter();
            writer.append('\n').append("------------").append('\n');
            writer.append("Closeable: ").append(getCloseable().getClass().getSimpleName()).append('\n');
            writer.append("Id: ").append(getId()).append('\n');
            writer.append("Age: ").append(String.valueOf(System.currentTimeMillis() - getCreation())).append('\n');
            getCaller().printStackTrace(new PrintWriter(writer)); // NOSONAR
            writer.append("------------").append('\n');
            return writer.toString();
        }
    }

    private class InputStreamHandler extends InputStream implements CloseableHandler {

        private final InputStream delegate;

        private final RuntimeException caller = new RuntimeException(); // NOSONAR

        private String id = UUID.randomUUID().toString();

        private final long creation = System.currentTimeMillis();

        private InputStreamHandler(InputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return delegate.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                remove(this);
            }
        }

        @Override
        public String toString() {
            return format();
        }

        @Override
        public RuntimeException getCaller() {
            return caller;
        }

        @Override
        public long getCreation() {
            return creation;
        }

        @Override
        public Closeable getCloseable() {
            return this;
        }
    }

    private class OutputStreamHandler extends OutputStream implements CloseableHandler {

        private final OutputStream delegate;

        private final RuntimeException caller = new RuntimeException(); // NOSONAR

        private String id = UUID.randomUUID().toString();

        private final long creation = System.currentTimeMillis();

        public OutputStreamHandler(OutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                remove(this);
            }
        }

        @Override
        public RuntimeException getCaller() {
            return caller;
        }

        @Override
        public long getCreation() {
            return creation;
        }

        @Override
        public Closeable getCloseable() {
            return this;
        }

        @Override
        public String toString() {
            return format();
        }
    }

    private class DeletableResourceProxy implements DeletableResource {

        private final DeletableResource delegate;

        private DeletableResourceProxy(DeletableResource delegate) {this.delegate = delegate;}

        @Override
        public void delete() throws IOException {
            delegate.delete();
        }

        @Override
        public void move(String s) throws IOException {
            delegate.move(s);
        }

        @Override
        public boolean isWritable() {
            return delegate.isWritable();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            OutputStreamHandler handler = new OutputStreamHandler(delegate.getOutputStream());
            entries.add(handler);
            return handler;
        }

        @Override
        public boolean exists() {
            return delegate.exists();
        }

        @Override
        public boolean isReadable() {
            return delegate.isReadable();
        }

        @Override
        public boolean isOpen() {
            return delegate.isOpen();
        }

        @Override
        public URL getURL() throws IOException {
            return delegate.getURL();
        }

        @Override
        public URI getURI() throws IOException {
            return delegate.getURI();
        }

        @Override
        public File getFile() throws IOException {
            return delegate.getFile();
        }

        @Override
        public long contentLength() throws IOException {
            return delegate.contentLength();
        }

        @Override
        public long lastModified() throws IOException {
            return delegate.lastModified();
        }

        @Override
        public Resource createRelative(String relativePath) throws IOException {
            return delegate.createRelative(relativePath);
        }

        @Override
        public String getFilename() {
            return delegate.getFilename();
        }

        @Override
        public String getDescription() {
            return delegate.getDescription();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            InputStreamHandler handler = new InputStreamHandler(delegate.getInputStream());
            entries.add(handler);
            return handler;
        }
    }
}
