// ============================================================================
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

package org.talend.dataprep.schema;

import java.io.IOException;
import java.util.List;

import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is used as a facade for all the format detection. It is an aggregation of all detectors.
 */
@Component
public class CompositeFormatDetector implements Detector {

    /** The fallback guess if the input is not CSV compliant. */
    @Autowired
    private UnsupportedFormatFamily unsupportedFormatFamily;

    /**
     * The TIKA detector used to discover format
     */
    @Autowired
    private List<Detector> detectors;

    /**
     * Reads an input stream and detects its format.
     *
     * Note that the stream will not close the specified stream before returning. It is to the responsibility of the
     * caller to close it.
     *
     * @param metadata the specified TIKA {@link Metadata}
     * @param inputStream the specified input stream
     * @return either null or the detected format
     * @throws IOException
     */
    @Override
    public Format detect(Metadata metadata, TikaInputStream inputStream) throws IOException {

        Format result = null;
        for (Detector detector : detectors) {
            result = detector.detect(metadata, inputStream);
            if (result != null)
                break;
        }
        if (result == null) {
            result = new Format(unsupportedFormatFamily, FormatUtils.DEFAULT_ENCODING);
        }
        return result;
    }

}
