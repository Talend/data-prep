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

package org.talend.dataprep.schema.html;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.tika.io.IOUtils;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.html.HtmlEncodingDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.talend.dataprep.schema.Detector;
import org.talend.dataprep.schema.Format;
import org.talend.dataprep.schema.FormatUtils;

/**
 * This class is used as a detector for XLS format. It is an adaptor for the TIKA {@link HtmlEncodingDetector}.
 */
@Component
@Order(value = 2)
public class HtmlDetector implements Detector {

    /**
     * The composed TIKA {@link HtmlEncodingDetector}
     */
    private HtmlEncodingDetector htmlEncodingDetector = new HtmlEncodingDetector();

    /** The html/salsesforce format family. */
    @Autowired
    private HtmlFormatFamily htmlFormatFamily;

    /**
     * Reads an input stream and checks if it has a HTML format.
     * 
     * The general contract of a detector is to not close the specified stream before returning. It is to the
     * responsibility of the caller to close it. The detector should leverage the mark/reset feature of the specified
     * {@see TikaInputStream} in order to let the stream always return the same bytes.
     * 
     * 
     * @param metadata the specified TIKA {@link Metadata}
     * @param inputStream the specified input stream
     * @return either null or an HTML format
     * @throws IOException
     */
    @Override
    public Format detect(Metadata metadata, TikaInputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        } else {
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
                Charset charset = htmlEncodingDetector.detect(stream, metadata);

                if (charset != null) {
                    return new Format(htmlFormatFamily, charset.name());
                }
            }
            return null;
        }

    }

}
