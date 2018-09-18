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

package org.talend.dataprep.i18n;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

public class DocumentationLinkGeneratorTest {

    private String currentDocUrl = "https://help.talend.com/";

    @Test
    public void generateDocumentationLinkWithAFSParameter() throws Exception {
        final String finalDocUrl = DocumentationLinkGenerator
                .builder() //
                .url(currentDocUrl) //
                .locale(Locale.FRANCE) //
                .addAfsLanguageParameter(true)
                .build();

        assertEquals("https://help.talend.com/?afs%3Alang=fr", finalDocUrl);
    }

    @Test
    public void generateDocumentationLinkWithContentLangParameter() throws Exception {
        final String finalDocUrl = DocumentationLinkGenerator
                .builder() //
                .url(currentDocUrl) //
                .locale(Locale.FRANCE) //
                .addContentLangParameter(true)
                .build();

        assertEquals("https://help.talend.com/?content-lang=fr", finalDocUrl);
    }

}
