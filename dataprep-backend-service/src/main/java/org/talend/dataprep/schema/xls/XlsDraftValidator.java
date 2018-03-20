//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.schema.xls;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.DraftValidator;

@Service("draftValidator#xls")
public class XlsDraftValidator implements DraftValidator {

    @Override
    public Result validate(DataSetMetadata dataSetMetadata) {
        if (StringUtils.isEmpty(dataSetMetadata.getSheetName())) {
            return new DraftValidator.Result(true);
        }
        return new DraftValidator.Result(false);
    }
}
