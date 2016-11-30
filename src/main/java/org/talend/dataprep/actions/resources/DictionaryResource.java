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

package org.talend.dataprep.actions.resources;

import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.service.Dictionaries;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;

class DictionaryResource implements FunctionResource {

    private final Dictionaries dictionaries;

    DictionaryResource(Dictionaries dictionaries) {
        this.dictionaries = dictionaries;
    }

    @Override
    public void register() {
        // Init Dictionaries
        if (dictionaries != null) {
            CategoryRecognizerBuilder builder = CategoryRecognizerBuilder.newBuilder().lucene();
            if (dictionaries.getDictionary() != null) {
                builder = builder.ddDirectory(dictionaries.getDictionary().get());
            }
            if (dictionaries.getKeyword() != null) {
                builder = builder.kwDirectory(dictionaries.getKeyword().get());
            }
            Providers.get(AnalyzerService.class, builder);
        }
    }
}
