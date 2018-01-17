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

package org.talend.dataprep.schema.csv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.io.IOUtils;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.txt.UniversalEncodingDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.talend.dataprep.schema.Detector;
import org.talend.dataprep.schema.Format;
import org.talend.dataprep.schema.FormatUtils;

/**
 * This class is used as a detector for CSV class. It is an adaptor for the TIKA MimeTypes {@link MimeTypes} and
 * {@link UniversalEncodingDetector}.
 *
 *
 *
 */
@Component
@Order(value = 3)
public class CSVDetector implements Detector {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVDetector.class);

    /**
     * The media type returned by TIKA when a CSV is detected.
     */
    private static final String TEXT_PLAIN = "text/plain";

    /**
     * The TIKA MimeTypes {@link MimeTypes}
     */
    private final MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();

    /**
     * The TIKA MimeTypes {@link UniversalEncodingDetector}
     */
    private final UniversalEncodingDetector encodingDetector = new UniversalEncodingDetector();

    /** The csv format family. */
    @Autowired
    private CSVFormatFamily csvFormatFamily;

    /**
     * Reads an input stream and checks if it has a CSV format.
     *
     * The general contract of a detector is to not close the specified stream before returning. It is to the
     * responsibility of the caller to close it. The detector should leverage the mark/reset feature of the specified
     * {@see TikaInputStream} in order to let the stream always return the same bytes.
     *
     * @param metadata the specified TIKA {@link Metadata}
     * @param inputStream the specified input stream
     * @return either null or an CSV format
     * @throws IOException
     */
    @Override
    public Format detect(Metadata metadata, TikaInputStream inputStream) throws IOException {

        Format result = detectText(metadata, inputStream);

        if (result == null) {
            inputStream.mark(FormatUtils.META_TAG_BUFFER_SIZE);
            byte[] buffer = new byte[FormatUtils.META_TAG_BUFFER_SIZE];
            int n = 0;

            for (int m = inputStream.read(buffer); m != -1
                    && n < buffer.length; m = inputStream.read(buffer, n, buffer.length - n)) {
                n += m;
            }

            inputStream.reset();
            String head = FormatUtils.readFromBuffer(buffer, 0, n);

            try (InputStream stream = TikaInputStream.get(IOUtils.toInputStream(head))) {
                result = detectText(new Metadata(), stream);
            }
        }
        return result;
    }

    /**
     * A private utility class used to detect format.
     * 
     * @param metadata the specified TIKA {@link Metadata}
     * @param inputStream the specified input stream
     * @return either null or an CSV format
     * @throws IOException
     */
    private Format detectText(Metadata metadata, InputStream inputStream) throws IOException {
        MediaType mediaType = mimeTypes.detect(inputStream, metadata);
        if (mediaType != null) {
            String mediaTypeName = mediaType.toString();

            if (StringUtils.startsWith(mediaTypeName, TEXT_PLAIN)) {
                Charset charset = null;
                try {
                    charset = encodingDetector.detect(inputStream, metadata);
                } catch (IOException e) {
                    LOGGER.debug("Unable to detect the encoding for a data set in CSV format", e);
                }
                if (charset != null) {
                    return new Format(csvFormatFamily, charset.name());
                } else {
                    return new Format(csvFormatFamily, FormatUtils.DEFAULT_ENCODING);
                }
            }
        }
        return null;
    }
}
