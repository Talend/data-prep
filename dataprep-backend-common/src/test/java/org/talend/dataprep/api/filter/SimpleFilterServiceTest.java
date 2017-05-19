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

package org.talend.dataprep.api.filter;

import static java.time.Month.JANUARY;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.date.DateParser;

public class SimpleFilterServiceTest {

    private final SimpleFilterService service = new SimpleFilterService();

    private DataSetRow datasetRowFromValues;

    private DataSetRow row;

    private RowMetadata rowMetadata;

    @Before
    public void init() {
        datasetRowFromValues = new DataSetRow(new HashMap<>());

        final ColumnMetadata firstColumn = new ColumnMetadata();
        firstColumn.setId("0001");
        final ColumnMetadata secondColumn = new ColumnMetadata();
        secondColumn.setId("0002");
        rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Arrays.asList(firstColumn, secondColumn));
        row = new DataSetRow(rowMetadata);
    }

    @Test
    public void should_create_TRUE_predicate_on_empty_filter() throws Exception {
        //given
        final String filtersDefinition = "";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        assertThat(filter.test(datasetRowFromValues), is(true));
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_throw_exception_on_empty_object_definition() throws Exception {
        //given
        final String filtersDefinition = "{}";

        //when
        service.build(filtersDefinition, rowMetadata);

        //then
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_throw_exception_on_invalid_definition() throws Exception {
        //given
        final String filtersDefinition = "}";

        //when
        service.build(filtersDefinition, rowMetadata);

        //then
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_create_unknown_filter() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"bouh\": {" +
                "       \"field\": \"0001\"," +
                "       \"value\": \"toto\"" +
                "   }" +
                "}";

        //when
        service.build(filtersDefinition, rowMetadata);

        //then
    }

    @Test
    public void should_create_EQ_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"eq\": {" +
                "       \"field\": \"0001\"," +
                "       \"value\": \"toto\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", "toto");
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "Toto"); //different case
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "tatatoto"); //contains but different
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", ""); //empty
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", null); //null
        assertThat(filter.test(datasetRowFromValues), is(false));
    }

    @Test
    public void should_create_EQ_predicate_more_number_format_integer_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"eq\": {" +
                "       \"field\": \"0001\"," +
                "       \"value\": \"5\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", "5.0"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "5,00"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "05.0"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "0 005"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));

        datasetRowFromValues.set("0001", "4.5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "4,5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ",5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ".5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "1.000,5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "1 000.5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
    }

    @Test
    public void should_create_EQ_predicate_more_number_format_decimal_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"eq\": {" +
                "       \"field\": \"0001\"," +
                "       \"value\": \"5,35\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", "5.35"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "5,35"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "05.35"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "0 005.35"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));

        datasetRowFromValues.set("0001", "4.5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "4,5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ",5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ".5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "1.000,5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "1 000.5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
    }

    @Test
    public void should_create_EQ_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"eq\": {" +
                "       \"value\": \"toto\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "toto");
        row.set("0002", "toto");
        assertThat(filter.test(row), is(true));
        row.set("0001", "Toto"); //different case on 0001
        assertThat(filter.test(row), is(true)); // "0002" still contains "toto"
        row.set("0002", "Toto"); //different case on 0002
        assertThat(filter.test(row), is(false));
    }

    @Test
    public void should_create_GT_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"gt\": {" +
                "       \"field\": \"0001\"," +
                "       \"value\": 5" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", "6"); //gt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "5"); //eq
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "4"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", "toto"); //nan
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ""); //nan
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", null); //null
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", "4.5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "4,5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ",5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ".5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", "5.0"); //eq
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "5,00"); //eq
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "05.0"); //eq
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "0 005"); //eq
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", "5.5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "5,5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "1.000,5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "1 000.5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(true));
    }

    @Test
    public void should_create_GT_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"gt\": {" +
                "       \"value\": 5" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "6"); //gt
        row.set("0002", "7"); //gt
        assertThat(filter.test(row), is(true));
        row.set("0001", "4"); // lt
        assertThat(filter.test(row), is(true));
        row.set("0002", "4"); // lt
        assertThat(filter.test(row), is(false));
    }

    @Test
    public void should_create_GTE_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"gte\": {" +
                "       \"field\": \"0001\"," +
                "       \"value\": 5" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", "6"); //gt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "5"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "4"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", "toto"); //nan
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ""); //nan
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", null); //null
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", "4.5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "4,5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ",5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ".5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", "5.0"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "5,00"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "05.0"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "0 005"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));

        datasetRowFromValues.set("0001", "5.5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "5,5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "1.000,5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "1 000.5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(true));
    }

    @Test
    public void should_create_GTE_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"gte\": {" +
                "       \"value\": 5" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "5"); //gt
        row.set("0002", "6"); //gt
        assertThat(filter.test(row), is(true));
        row.set("0001", "4"); // lt
        assertThat(filter.test(row), is(true));
        row.set("0002", "4"); //lt
        assertThat(filter.test(row), is(false));
    }

    @Test
    public void should_create_LT_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"lt\": {" +
                "       \"field\": \"0001\"," +
                "       \"value\": 5" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", "6"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "5"); //eq
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "4"); //lt
        assertThat(filter.test(datasetRowFromValues), is(true));

        datasetRowFromValues.set("0001", "toto"); //nan
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ""); //nan
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", null); //null
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", "4.5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "4,5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", ",5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", ".5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(true));

        datasetRowFromValues.set("0001", "5.0"); //eq
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "5,00"); //eq
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "05.0"); //eq
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "0 005"); //eq
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", "5.5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "5,5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "1.000,5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "1 000.5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
    }

    @Test
    public void should_create_LT_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"lt\": {" +
                "       \"value\": 5" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "6"); //gt
        row.set("0002", "6"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "4"); // lt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0002", "4"); //lt
        assertThat(filter.test(datasetRowFromValues), is(true));
    }


    @Test
    public void should_create_LTE_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"lte\": {" +
                "       \"field\": \"0001\"," +
                "       \"value\": 5" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", "6"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "5"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "4"); //lt
        assertThat(filter.test(datasetRowFromValues), is(true));

        datasetRowFromValues.set("0001", "toto"); //nan
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ""); //nan
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", null); //null
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", "4.5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "4,5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", ",5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", ".5"); //lt
        assertThat(filter.test(datasetRowFromValues), is(true));

        datasetRowFromValues.set("0001", "5.0"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "5,00"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "05.0"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "0 005"); //eq
        assertThat(filter.test(datasetRowFromValues), is(true));

        datasetRowFromValues.set("0001", "5.5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "5,5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "1.000,5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "1 000.5"); //gt
        assertThat(filter.test(datasetRowFromValues), is(false));
    }

    @Test
    public void should_create_LTE_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"lte\": {" +
                "       \"value\": 5" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "6"); //gt
        row.set("0002", "6"); //gt
        assertThat(filter.test(row), is(false));
        row.set("0001", "5"); //eq
        assertThat(filter.test(row), is(true));
        row.set("0002", "5"); //lt
        assertThat(filter.test(row), is(true));
    }

    @Test
    public void should_create_CONTAINS_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"contains\": {" +
                "       \"field\": \"0001\"," +
                "       \"value\": \"toto\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", "toto"); //equals
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "Toto"); //different case
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "tatatoto"); //contains but different
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "tagada"); // not contains
        assertThat(filter.test(datasetRowFromValues), is(false));
    }

    @Test
    public void should_create_CONTAINS_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"contains\": {" +
                "       \"value\": \"toto\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "toto"); //equals
        row.set("0002", "toto"); //equals
        assertThat(filter.test(row), is(true));
        row.set("0001", "Toto"); //different case
        assertThat(filter.test(row), is(true));
        row.set("0001", "tatatoto"); //contains but different
        assertThat(filter.test(row), is(true));
        row.set("0001", "tagada"); // not contains
        assertThat(filter.test(row), is(true));
        row.set("0002", "tagada"); // not contains
        assertThat(filter.test(row), is(false));
    }

    @Test
    public void should_create_MATCHES_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"matches\": {" +
                "       \"field\": \"0001\"," +
                "       \"value\": \"Aa9-\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", "toto"); // different pattern
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", "To5-"); // same pattern
        assertThat(filter.test(datasetRowFromValues), is(true));

        datasetRowFromValues.set("0001", "To5--"); // different length
        assertThat(filter.test(datasetRowFromValues), is(false));

        datasetRowFromValues.set("0001", ""); // empty value
        assertThat(filter.test(datasetRowFromValues), is(false));
    }

    @Test
    public void should_create_MATCHES_predicate_empty_pattern() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"matches\": {" +
                "       \"field\": \"0001\"," +
                "       \"value\": \"\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", ""); // empty value
        assertThat(filter.test(datasetRowFromValues), is(true));

        datasetRowFromValues.set("0001", "tagada"); // not empty value
        assertThat(filter.test(datasetRowFromValues), is(false));
    }

    @Test
    public void should_create_MATCHES_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"matches\": {" +
                "       \"value\": \"Aa9-\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "toto"); // different pattern
        row.set("0002", "toto"); // different pattern
        assertThat(filter.test(row), is(false));

        row.set("0001", "To5-"); // same pattern
        assertThat(filter.test(row), is(true));

        row.set("0002", "To5-"); // different length
        assertThat(filter.test(row), is(true));
    }


    @Test
    public void should_create_INVALID_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"invalid\": {" +
                "       \"field\": \"0001\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.setInvalid("0001"); //value in invalid array in column metadata
        assertThat(filter.test(row), is(true));
        row.unsetInvalid("0001");
        assertThat(filter.test(row), is(false));
        assertThat(filter.test(row), is(false));
    }

    @Test
    public void should_create_INVALID_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"invalid\": {" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.setInvalid("0001"); // value in invalid array in column metadata
        row.setInvalid("0002"); // value in invalid array in column metadata
        assertThat(filter.test(row), is(true));
        row.unsetInvalid("0002");
        assertThat(filter.test(row), is(true));
        assertThat(filter.test(row), is(true));
    }

    @Test
    public void should_create_VALID_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"valid\": {" +
                "       \"field\": \"0001\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.setInvalid("0001"); // value is marked as invalid
        assertThat(filter.test(row), is(false));

        row.unsetInvalid("0001"); // value is marked as valid
        row.set("0001", ""); //empty
        assertThat(filter.test(row), is(false));

        row.unsetInvalid("0001"); // value is marked as valid
        row.set("0001", "toto"); // correct value
        assertThat(filter.test(row), is(true));
    }

    @Test
    public void should_create_VALID_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"valid\": {" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "toto");
        row.set("0002", "toto");

        row.setInvalid("0001"); // value is marked as invalid
        assertThat(filter.test(row), is(true));

        row.setInvalid("0002"); // value is marked as invalid
        assertThat(filter.test(row), is(false));
    }

    @Test
    public void should_create_EMPTY_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"empty\": {" +
                "       \"field\": \"0001\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", ""); //empty
        assertThat(filter.test(row), is(true));
        row.set("0001", "toto"); //not empty value
        assertThat(filter.test(row), is(false));
    }

    @Test
    public void should_create_EMPTY_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"empty\": {" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "toto"); // not empty
        row.set("0002", "toto"); // not empty
        assertThat(filter.test(row), is(false));
        row.set("0001", ""); //not empty value
        assertThat(filter.test(row), is(true));
    }

    @Test
    public void should_create_number_RANGE_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"range\": {" +
                "       \"field\": \"0001\"," +
                "       \"start\": \"5\"," +
                "       \"end\": \"10\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.getRowMetadata().getById("0001").setType("integer");
        row.set("0001", "a"); //invalid number
        assertThat(filter.test(row), is(false));
        row.set("0001", "4"); //lt min
        assertThat(filter.test(row), is(false));
        row.set("0001", "5"); //eq min
        assertThat(filter.test(row), is(true));
        row.set("0001", "8"); //in range
        assertThat(filter.test(row), is(true));
        row.set("0001", "10"); //eq max
        assertThat(filter.test(row), is(false));
        row.set("0001", "20"); //gt max
        assertThat(filter.test(row), is(false));

        row.set("0001", "toto"); //nan
        assertThat(filter.test(row), is(false));
        row.set("0001", ""); //nan
        assertThat(filter.test(row), is(false));
        row.set("0001", null); //null
        assertThat(filter.test(row), is(false));

        row.set("0001", "4.5"); //lt
        assertThat(filter.test(row), is(false));
        row.set("0001", "4,5"); //lt
        assertThat(filter.test(row), is(false));
        row.set("0001", ",5"); //lt
        assertThat(filter.test(row), is(false));
        row.set("0001", ".5"); //lt
        assertThat(filter.test(row), is(false));

        row.set("0001", "5.0"); //eq
        assertThat(filter.test(row), is(true));
        row.set("0001", "5,00"); //eq
        assertThat(filter.test(row), is(true));
        row.set("0001", "05.0"); //eq
        assertThat(filter.test(row), is(true));
        row.set("0001", "0 005"); //eq
        assertThat(filter.test(row), is(true));

        row.set("0001", "5.5"); //gt
        assertThat(filter.test(row), is(true));
        row.set("0001", "5,5"); //gt
        assertThat(filter.test(row), is(true));
        row.set("0001", "1.000,5"); //gt
        assertThat(filter.test(row), is(false));
        row.set("0001", "1 000.5"); //gt
        assertThat(filter.test(row), is(false));
    }

    @Test
    public void should_create_number_RANGE_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"range\": {" +
                "       \"start\": \"5\"," +
                "       \"end\": \"10\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "4");
        row.set("0002", "3");
        assertThat(filter.test(row), is(false));

        row.set("0001", "6"); //lt min
        assertThat(filter.test(row), is(true));
    }

    @Test
    public void should_create_date_RANGE_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"range\": {" +
                "       \"field\": \"0001\"," +
                "       \"start\": 0," + //1970-01-01 UTC timezone
                "       \"end\": " + (LocalDateTime.of(1990, JANUARY, 1, 0, 0).toEpochSecond(UTC) * 1000) + //1990-01-01 UTC timezone
                "   }" +
                "}";

        final ColumnMetadata column = row.getRowMetadata().getById("0001");
        column.setType("date");
        final DateParser dateParser = Mockito.mock(DateParser.class);
        when(dateParser.parse("a", column)).thenThrow(new DateTimeException(""));
        when(dateParser.parse("1960-01-01", column)).thenReturn(LocalDateTime.of(1960, JANUARY, 1, 0, 0));
        when(dateParser.parse("1970-01-01", column)).thenReturn(LocalDateTime.of(1970, JANUARY, 1, 0, 0));
        when(dateParser.parse("1980-01-01", column)).thenReturn(LocalDateTime.of(1980, JANUARY, 1, 0, 0));
        when(dateParser.parse("1990-01-01", column)).thenReturn(LocalDateTime.of(1990, JANUARY, 1, 0, 0));
        when(dateParser.parse("2000-01-01", column)).thenReturn(LocalDateTime.of(2000, JANUARY, 1, 0, 0));
        service.setDateParser(dateParser);

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "a"); //invalid number
        assertThat(filter.test(row), is(false));
        row.set("0001", "1960-01-01"); //lt min
        assertThat(filter.test(row), is(false));
        row.set("0001", "1970-01-01"); //eq min
        assertThat(filter.test(row), is(true));
        row.set("0001", "1980-01-01"); //in range
        assertThat(filter.test(row), is(true));
        row.set("0001", "1990-01-01"); //eq max
        assertThat(filter.test(row), is(false));
        row.set("0001", "2000-01-01"); //gt max
        assertThat(filter.test(row), is(false));
    }

    @Test
    public void should_create_AND_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"and\": [" +
                "       {" +
                "           \"empty\": {" +
                "               \"field\": \"0001\"" +
                "           }" +
                "       }," +
                "       {" +
                "           \"eq\": {" +
                "               \"field\": \"0002\"," +
                "               \"value\": \"toto\"" +
                "           }" +
                "       }" +
                "   ]" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", ""); //empty
        datasetRowFromValues.set("0002", "toto"); //eq value
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "tata"); //not empty
        datasetRowFromValues.set("0002", "toto"); //eq value
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", ""); //empty
        datasetRowFromValues.set("0002", "tata"); //neq value
        assertThat(filter.test(datasetRowFromValues), is(false));
    }

    @Test
    public void should_create_OR_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"or\": [" +
                "       {" +
                "           \"empty\": {" +
                "               \"field\": \"0001\"" +
                "           }" +
                "       }," +
                "       {" +
                "           \"eq\": {" +
                "               \"field\": \"0002\"," +
                "               \"value\": \"toto\"" +
                "           }" +
                "       }" +
                "   ]" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", ""); //empty
        datasetRowFromValues.set("0002", "toto"); //eq value
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "tata"); //not empty
        datasetRowFromValues.set("0002", "toto"); //eq value
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", ""); //empty
        datasetRowFromValues.set("0002", "tata"); //neq value
        assertThat(filter.test(datasetRowFromValues), is(true));
        datasetRowFromValues.set("0001", "tata"); //not empty
        datasetRowFromValues.set("0002", "tata"); //neq value
        assertThat(filter.test(datasetRowFromValues), is(false));
    }

    @Test
    public void should_create_NOT_predicate() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"not\":" +
                "       {" +
                "           \"empty\": {" +
                "               \"field\": \"0001\"" +
                "           }" +
                "       }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        //then
        datasetRowFromValues.set("0001", ""); //empty
        assertThat(filter.test(datasetRowFromValues), is(false));
        datasetRowFromValues.set("0001", "toto"); //not empty
        assertThat(filter.test(datasetRowFromValues), is(true));
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_create_NOT_predicate_invalid1() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"not\": [" +
                "       {" +
                "           \"empty\": {" +
                "               \"field\": \"0001\"" +
                "           }" +
                "       }," +
                "       {" +
                "           \"eq\": {" +
                "               \"field\": \"0002\"," +
                "               \"value\": \"toto\"" +
                "           }" +
                "       }" +
                "   ]" +
                "}";

        //when
        service.build(filtersDefinition, rowMetadata);

        //then
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_create_NOT_predicate_invalid2() throws Exception {
        //given
        final String filtersDefinition = "{" +
                "   \"not\":" +
                "       {" +
                "       }" +
                "}";

        //when
        service.build(filtersDefinition, rowMetadata);

        //then
    }

    /**
     * Make preparation sent to stream work when no filter is sent
     * <a>https://jira.talendforge.org/browse/TDP-3518</a>
     *
     */
    @Test
    public void TDP_3518_should_create_TRUE_predicate_on_double_quote() throws Exception {
        // given
        final String filtersDefinition = null;

        // when
        final Predicate<DataSetRow> filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThat(filter.test(datasetRowFromValues), is(true));
    }

}
