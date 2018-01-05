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

package org.talend.dataprep.actions.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataquality.semantic.broadcast.TdqCategories;
import org.talend.dataquality.semantic.snapshot.BroadcastDictionarySnapshotProvider;

public class DictionaryResource implements FunctionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryResource.class);

    private final TdqCategories tdqCategories;

    public DictionaryResource(TdqCategories tdqCategories) {
        this.tdqCategories = tdqCategories;
    }

    @Override
    public void register() {
        // Init Dictionaries
        if (tdqCategories != null) {
            LOGGER.info("registering dictionary resources into AnalyzerService...");
            final AnalyzerService analyzerService = Providers.get(AnalyzerService.class);
            analyzerService
                    .setDictionarySnapshotProvider(new BroadcastDictionarySnapshotProvider(tdqCategories.asDictionarySnapshot()));
        }
    }
}
