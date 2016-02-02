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

package org.talend.dataprep.api.preparation.json;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationDetails;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Component
public class PreparationMetadataModule extends SimpleModule {

    @Autowired
    PreparationDetailsJsonSerializer preparationDetailsJsonSerializer;

    public PreparationMetadataModule() {
        super(Preparation.class.getName(), new Version(1, 0, 0, null, null, null));
    }

    @PostConstruct
    public void init() {
        addSerializer(PreparationDetails.class, preparationDetailsJsonSerializer);
    }

}
