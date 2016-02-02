//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

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
    public static SameJSONAs<String> sameJSONAsFile(InputStream stream) throws IOException {
        return (SameJSONAs<String>) sameJSONAs(IOUtils.toString(stream)).allowingExtraUnexpectedFields()
                .allowingAnyArrayOrdering();
    }
}
