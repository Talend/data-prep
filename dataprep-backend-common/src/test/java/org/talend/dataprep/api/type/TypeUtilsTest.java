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

package org.talend.dataprep.api.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.util.Collections;

import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.classifier.custom.UserDefinedCategory;
import org.talend.dataquality.semantic.recognizer.CategoryFrequency;
import org.talend.dataquality.semantic.statistics.SemanticType;
import org.talend.dataquality.statistics.type.DataTypeEnum;


public class TypeUtilsTest {

    @Test
    public void testConvertString() throws Exception {
        ColumnMetadata metadata = column().id(1).type(Type.ANY).build();
        DataTypeEnum[] types = TypeUtils.convert(Collections.singletonList(metadata));
        assertThat(types[0], is(DataTypeEnum.STRING));

        metadata = column().id(2).type(Type.STRING).build();
        types = TypeUtils.convert(Collections.singletonList(metadata));
        assertThat(types[0], is(DataTypeEnum.STRING));
    }

    @Test
    public void testConvertInteger() throws Exception {
        ColumnMetadata metadata = column().id(1).type(Type.NUMERIC).build();
        DataTypeEnum[] types = TypeUtils.convert(Collections.singletonList(metadata));
        assertThat(types[0], is(DataTypeEnum.INTEGER));

        metadata = column().id(2).type(Type.INTEGER).build();
        types = TypeUtils.convert(Collections.singletonList(metadata));
        assertThat(types[0], is(DataTypeEnum.INTEGER));
    }

    @Test
    public void testConvertDouble() throws Exception {
        ColumnMetadata metadata = column().id(1).type(Type.DOUBLE).build();
        DataTypeEnum[] types = TypeUtils.convert(Collections.singletonList(metadata));
        assertThat(types[0], is(DataTypeEnum.DOUBLE));

        metadata = column().id(2).type(Type.FLOAT).build();
        types = TypeUtils.convert(Collections.singletonList(metadata));
        assertThat(types[0], is(DataTypeEnum.DOUBLE));
    }

    @Test
    public void testConvertBoolean() throws Exception {
        ColumnMetadata metadata = column().id(1).type(Type.BOOLEAN).build();
        final DataTypeEnum[] types = TypeUtils.convert(Collections.singletonList(metadata));
        assertThat(types[0], is(DataTypeEnum.BOOLEAN));
    }

    @Test
    public void testConvertDate() throws Exception {
        ColumnMetadata metadata = column().id(1).type(Type.DATE).build();
        final DataTypeEnum[] types = TypeUtils.convert(Collections.singletonList(metadata));
        assertThat(types[0], is(DataTypeEnum.DATE));
    }

    @Test
    public void testNullSemanticDomainType() throws Exception {
        assertThat(TypeUtils.getDomainLabel(((SemanticType) null)), is(""));
        assertThat(TypeUtils.getDomainLabel(((String) null)), is(""));
    }

    @Test
    public void testSemanticDomainType() throws Exception {
        final SemanticType semanticType = new SemanticType();
        semanticType.increment(new CategoryFrequency(new UserDefinedCategory(SemanticCategoryEnum.AIRPORT.getId())), 1);
        assertThat(TypeUtils.getDomainLabel(semanticType), is(SemanticCategoryEnum.AIRPORT.getDisplayName()));
        assertThat(TypeUtils.getDomainLabel(SemanticCategoryEnum.AIRPORT.getId()), is(SemanticCategoryEnum.AIRPORT.getDisplayName()));
    }
}
