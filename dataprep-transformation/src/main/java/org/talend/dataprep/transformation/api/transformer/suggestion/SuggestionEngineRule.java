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

package org.talend.dataprep.transformation.api.transformer.suggestion;

import java.util.function.BiFunction;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

public interface SuggestionEngineRule extends BiFunction<ActionMetadata, ColumnMetadata, Integer> {

    int HOP = 10;

    int LOW = HOP;

    int MEDIUM = HOP * 2;

    int HIGH = HOP * 3;

    int EMPTY_MGT = HOP * 4;

    int INVALID_MGT = HOP * 5;

    int NEGATIVE = -1 * HOP;

    int NON_APPLICABLE = 0;

}
