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

package org.talend.dataprep.transformation.service;

import java.io.Serializable;

import org.talend.dataquality.semantic.broadcast.BroadcastIndexObject;

/**
 * A container object for DQ dictionaries (keyword and dictionary).
 */
public class Dictionaries implements Serializable {

    private final BroadcastIndexObject dictionary;

    private final BroadcastIndexObject keyword;

    public Dictionaries(BroadcastIndexObject dictionary, BroadcastIndexObject keyword) {
        this.dictionary = dictionary;
        this.keyword = keyword;
    }

    public BroadcastIndexObject getDictionary() {
        return dictionary;
    }

    public BroadcastIndexObject getKeyword() {
        return keyword;
    }
}
