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

package org.talend.dataprep.preparation;

import org.talend.dataprep.api.preparation.Step;

public class FixedIdStep extends Step {

    private final String fixedId;

    public FixedIdStep(String fixedId) {
        super(null, null, null);
        this.fixedId = fixedId;
    }

    @Override
    public String id() {
        return fixedId;
    }

    @Override
    public String getId() {
        return fixedId;
    }
}
