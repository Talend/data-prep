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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataquality.semantic.broadcast.TdqCategories;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;

public class DictionaryResource implements FunctionResource {

    private final TdqCategories tdqCategories;

    public DictionaryResource(TdqCategories tdqCategories) {
        this.tdqCategories = tdqCategories;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryResource.class);

    @Override
    public void register() {
        // Init Dictionaries
        if (tdqCategories != null) {
            LOGGER.info("registering dictionary resources into AnalyzerService...");
            CategoryRecognizerBuilder builder = CategoryRecognizerBuilder.newBuilder().lucene();
            if (tdqCategories.getDictionary() != null) {
                builder = builder.ddDirectory(tdqCategories.getDictionary().asDirectory());
            }
            if (tdqCategories.getCustomDictionary() != null) {
                builder = builder.ddCustomDirectory(tdqCategories.getCustomDictionary().asDirectory());
            }
            if (tdqCategories.getKeyword() != null) {
                builder = builder.kwDirectory(tdqCategories.getKeyword().asDirectory());
            }
            if (tdqCategories.getRegex() != null) {
                builder = builder.regexClassifier(tdqCategories.getRegex().getRegexClassifier());
            }
            if (tdqCategories.getCategoryMetadata() != null){
                builder = builder.metadata(tdqCategories.getCategoryMetadata().getMetadata());
            }
            Providers.get(AnalyzerService.class).setCategoryRecognizerBuilder(builder);
        }
    }
}
