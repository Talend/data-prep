package org.talend.dataprep.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ZippedContentCacheTest {

    @Mock
    private ContentCache delegate;

    @Test
    public void shouldReturnZippedOutputStream() {
        // given
        when(delegate.put(any(), any())).thenReturn(mock(OutputStream.class));
        ContentCache contentCache = new ZippedContentCache(delegate);

        // when
        final OutputStream stream = contentCache.put(() -> StringUtils.EMPTY, ContentCache.TimeToLive.DEFAULT);

        // then
        assertEquals(GZIPOutputStream.class, stream.getClass());
    }

    @Test
    public void shouldReturnNullOutputStream() {
        // given
        when(delegate.put(any(), any())).thenReturn(null);
        ContentCache contentCache = new ZippedContentCache(delegate);

        // when
        final OutputStream stream = contentCache.put(() -> StringUtils.EMPTY, ContentCache.TimeToLive.DEFAULT);

        // then
        assertNull(stream);
    }

    @Test
    public void shouldReturnZippedInputStream() throws IOException {
        // given
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream sampleContent = new GZIPOutputStream(outputStream)) {
            sampleContent.write('a');
        }
        when(delegate.get(any())).thenReturn(new ByteArrayInputStream(outputStream.toByteArray()));
        ContentCache contentCache = new ZippedContentCache(delegate);

        // when
        final InputStream stream = contentCache.get(() -> StringUtils.EMPTY);

        // then
        assertEquals(GZIPInputStream.class, stream.getClass());
    }

    @Test
    public void shouldReturnNullInputStream() {
        // given
        when(delegate.get(any())).thenReturn(null);
        ContentCache contentCache = new ZippedContentCache(delegate);

        // when
        final InputStream stream = contentCache.get(() -> StringUtils.EMPTY);

        // then
        assertNull(stream);
    }
}