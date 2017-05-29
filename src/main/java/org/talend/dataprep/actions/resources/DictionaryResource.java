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
import org.talend.dataquality.semantic.broadcast.TdqCategories;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;

public class DictionaryResource implements FunctionResource {

    private final TdqCategories tdqCategories;

    public DictionaryResource(TdqCategories tdqCategories) {
        this.tdqCategories = tdqCategories;
    }

    @Override
    public void register() {
        // Init Dictionaries
        if (tdqCategories != null) {
            CategoryRecognizerBuilder builder = CategoryRecognizerBuilder.newBuilder().lucene();
            if (tdqCategories.getDictionary() != null) {
                builder = builder.ddDirectory(tdqCategories.getDictionary().get());
            }
            if (tdqCategories.getKeyword() != null) {
                builder = builder.kwDirectory(tdqCategories.getKeyword().get());
            }
            if (tdqCategories.getRegex() != null) {
                builder = builder.regexClassifier(tdqCategories.getRegex().get());
            }
            if (tdqCategories.getCategoryMetadata() != null){
                builder = builder.metadata(tdqCategories.getCategoryMetadata().get());
            }
            Providers.get(AnalyzerService.class, builder);
        }
    }
}
