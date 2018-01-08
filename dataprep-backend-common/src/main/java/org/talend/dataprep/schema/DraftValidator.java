// ============================================================================
//
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

package org.talend.dataprep.schema;

import org.talend.dataprep.api.dataset.DataSetMetadata;

/**
 * class responsible to validate if the {@link DataSetMetadata} is still a draft TODO return more reasons (i.e field
 * etc..)
 */
public interface DraftValidator {

    Result validate(DataSetMetadata dataSetMetadata);

    class Result {

        private final boolean draft;

        public Result(boolean draft) {
            this.draft = draft;
        }

        public boolean isDraft() {
            return draft;
        }

    }

}
