//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.test;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import uk.co.datumedge.hamcrest.json.SameJSONAs;

public class SameJSONFile {

    private SameJSONFile() {
    }

    /**
     * Utilities method to assert that an expected json contained in a file matches a result.
     *
     * @param stream A stream that contains the expected json.
     * @return a SameJSONAs to use like in assertThat(contentAsString, sameJSONAsFile("t-shirt_100.csv.expected.json"));
     */
    public static SameJSONAs<? super String> sameJSONAsFile(InputStream stream) throws IOException {
        return sameJSONAs(IOUtils.toString(stream, UTF_8));
    }

    /**
     * Utilities method to assert that an expected json contained in a String matches a result.
     *
     * @param expected A string that contains the expected json.
     * @return a SameJSONAs to use like in assertThat(contentAsString, sameJSONAsFile("t-shirt_100.csv.expected.json"));
     */
    public static SameJSONAs<? super String> sameJSONAs(String expected) throws IOException {
        return SameJSONAs.sameJSONAs(expected).allowingExtraUnexpectedFields().allowingAnyArrayOrdering();
    }

}
