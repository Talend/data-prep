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

package org.talend.dataprep.transformation.actions.date;

import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;

import java.io.InputStream;

import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;

/**
 * Base class for all date related unit tests.
 */
public abstract class BaseDateTest<T extends  AbstractActionMetadata> extends AbstractMetadataBaseTest<T> {

    protected BaseDateTest(T action) {
        super(action);
    }

    /**
     * @param statisticsFileName the statistics file name to use.
     * @return a row with default settings for the tests.
     */
    protected DataSetRow getDefaultRow(String statisticsFileName) {
        return builder() //
                .with(value("lorem bacon").type(Type.STRING).name("recipe")) //
                .with(value("01/01/2010").type(Type.DATE).name("last update").statistics(getDateTestJsonAsStream(statisticsFileName))) //
                .with(value("Bacon").type(Type.STRING).name("steps")) //
                .build();
    }

    protected InputStream getDateTestJsonAsStream(String testFileName) {
        return BaseDateTest.class.getResourceAsStream(testFileName);
    }

}
