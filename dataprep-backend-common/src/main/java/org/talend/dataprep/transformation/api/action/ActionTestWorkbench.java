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

package org.talend.dataprep.transformation.api.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.transformation.BaseTransformer;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;

public class ActionTestWorkbench {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionTestWorkbench.class);

    public static void test(RowMetadata rowMetadata, DataSetRowAction... actions) {
        test(new DataSetRow(rowMetadata), actions);
    }

    public static void test(DataSetRow input, DataSetRowAction... actions) {
        test(Collections.singletonList(input), actions);
    }

    public static void test(Collection<DataSetRow> input, DataSetRowAction... actions) {
        TransformationContext context = new TransformationContext();
        final List<DataSetRowAction> allActions = new ArrayList<>();
        Collections.addAll(allActions, actions);
        BaseTransformer.baseTransform(input.stream(), allActions, context).forEach(r -> LOGGER.debug(r.toString()));
    }
}
