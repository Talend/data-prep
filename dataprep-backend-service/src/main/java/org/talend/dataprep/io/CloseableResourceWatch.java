package org.talend.dataprep.io;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.scheduling.annotation.Scheduled;

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

    @Around("within(org.talend..*) && execution(public java.io.Closeable+ *(..))")
    public Object closeableWatch(ProceedingJoinPoint pjp) throws Throwable {
        final Object proceed = pjp.proceed();
        try {
            if (proceed instanceof InputStream) {
                final CloseableHandler handler = new InputStreamHandler((InputStream) proceed);
                entries.add(handler);
                return handler;
            } else if (proceed instanceof OutputStream) {
                final CloseableHandler handler = new OutputStreamHandler((OutputStream) proceed);
                entries.add(handler);
                return handler;
            } else {
                LOGGER.warn("No watch for '{}'.", proceed.getClass());
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

    private boolean remove(CloseableHandler handler) {
        return entries.remove(handler);
    }

    /**
     * A clean up process that starts a minute after the previous ended.
     */
    @Scheduled(fixedDelay = 30000)
    public void log() {
        synchronized (entries) {
            LOGGER.info("Logging closeable resources...");
            for (CloseableHandler entry : entries) {
                LOGGER.info("{}", entry);
            }
            LOGGER.info("Done logging closeable resources.");
        }
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return LOGGER.isDebugEnabled();
    }

    interface CloseableHandler {

        RuntimeException getCaller();

        long getCreation();

        Closeable getCloseable();

        default String format() {
            StringWriter writer = new StringWriter();
            writer.append('\n').append("------------").append('\n');
            writer.append("Closeable: ").append(getCloseable().toString()).append('\n');
            writer.append("Age: ").append(String.valueOf(System.currentTimeMillis() - getCreation())).append('\n');
            getCaller().printStackTrace(new PrintWriter(writer)); // NOSONAR
            writer.append("------------").append('\n');
            return writer.toString();
        }
    }

    private class InputStreamHandler extends InputStream implements CloseableHandler {

        private final InputStream delegate;

        private final RuntimeException caller = new RuntimeException(); // NOSONAR

        private final long creation = System.currentTimeMillis();

        private InputStreamHandler(InputStream delegate) {
            this.delegate = delegate;
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

        private final long creation = System.currentTimeMillis();

        public OutputStreamHandler(OutputStream delegate) {
            this.delegate = delegate;
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
}
