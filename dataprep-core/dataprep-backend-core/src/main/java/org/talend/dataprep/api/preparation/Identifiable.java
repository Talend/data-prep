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

package org.talend.dataprep.api.preparation;

public abstract class Identifiable {

    // Only there for MongoDB serialization purposes
    protected String id;

    public abstract String id();

    // Only there for MongoDB serialization purposes
    public abstract String getId();

    // Only there for MongoDB serialization purposes
    public abstract void setId(String id);
}
