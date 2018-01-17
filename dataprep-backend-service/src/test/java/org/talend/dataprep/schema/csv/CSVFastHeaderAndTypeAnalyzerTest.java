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

package org.talend.dataprep.schema.csv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.test.LocalizationRule;

public class CSVFastHeaderAndTypeAnalyzerTest {

    @Rule
    public LocalizationRule rule = new LocalizationRule(Locale.US);

    @Test(expected = IllegalArgumentException.class)
    public void should_not_construct_object_with_null_sample(){
        new CSVFastHeaderAndTypeAnalyzer(null, new Separator(';'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_construct_object_with_null_separator(){
        new CSVFastHeaderAndTypeAnalyzer(null, new Separator(';'));
    }

    @Test
    public void should_neither_detect_type_nor_first_line_header_when_sample_is_empty() {
        // given
        String firstRecord = "";
        List<String> list = Collections.singletonList(firstRecord);
        Separator separator = new Separator(';');

        // when
        CSVFastHeaderAndTypeAnalyzer analysis = new CSVFastHeaderAndTypeAnalyzer(list, separator);

        // then
        List<Type> expectedTypes = Collections.emptyList();
        Assert.assertArrayEquals(expectedTypes.toArray(), analysis.getHeaders().stream().map( p -> p.getValue()).toArray());
        Assert.assertFalse(analysis.isFirstLineAHeader());
    }

    @Test
    public void should_detect_type_without_first_line_header_when_sample_has_one_line() {
        // given
        String firstRecord = "0001;Toto; Hello;1,000.02;false;;";
        List<String> list = Collections.singletonList(firstRecord);
        Separator separator = new Separator(';');

        // when
        CSVFastHeaderAndTypeAnalyzer analysis = new CSVFastHeaderAndTypeAnalyzer(list, separator);

        // then
        List<Type> expectedTypes = Arrays.asList(Type.INTEGER, Type.STRING, Type.STRING, Type.DOUBLE, Type.BOOLEAN, Type.STRING);
        Assert.assertFalse(analysis.isFirstLineAHeader());
        Assert.assertArrayEquals(expectedTypes.toArray(), analysis.getHeaders().stream().map( p -> p.getValue()).toArray());

    }

    @Test
    public void should_detect_type_without_first_line_header_when_sample_has_two_lines_without_header() {
        // given
        String firstRecord = "0001;Toto; Hello;1,000.02";
        String secondRecord = "0001;Toto; Hello;1,000.02";
        List<String> list = Arrays.asList(firstRecord, secondRecord);
        Separator separator = new Separator(';');

        // when
        CSVFastHeaderAndTypeAnalyzer analysis = new CSVFastHeaderAndTypeAnalyzer(list, separator);

        // then
        List<Type> expectedTypes = Arrays.asList(Type.INTEGER, Type.STRING, Type.STRING, Type.DOUBLE);
        Assert.assertFalse(analysis.isFirstLineAHeader());
        Assert.assertArrayEquals(expectedTypes.toArray(), analysis.getHeaders().stream().map( p -> p.getValue()).toArray());
        Assert.assertTrue(analysis.isHeaderInfoReliable());

    }

    @Test
    public void should_detect_type_with_first_line_header_when_sample_has_ten_text_lines_without_header() {
        // given
        String firstRecord = "Toto; Hello;";
        String record2 = "Toto; Hello;";
        String record3 = "Toto; Hello;";
        String record4 = "Toto; Hello;";
        String record5 = "Toto; Hello;1;0";
        String record6 = "Toto; 2;1;2";
        String record7 = "Toto; 3;1";
        String record8 = "Toto; 4;1";
        String record9 = "Toto; 2;1";
        String record10 = "Toto;2;2; Hello;";
        List<String> list = Arrays.asList(firstRecord, record2, record3, record4, record5, record6, record7, record8, record9,
                record10);
        Separator separator = new Separator(';');
        for (int i = 0; i < 10; i++) {
            separator.incrementCount(i);
        }

        // when
        CSVFastHeaderAndTypeAnalyzer analysis = new CSVFastHeaderAndTypeAnalyzer(list, separator);

        // then
        // List<Type> expectedTypes = Arrays.asList(Type.STRING, Type.STRING, Type.INTEGER, Type.STRING);
        List<Type> expectedTypes = Arrays.asList(Type.STRING, Type.STRING);
        Assert.assertArrayEquals(expectedTypes.toArray(), analysis.getHeaders().stream().map( p -> p.getValue()).toArray());
        Assert.assertTrue(analysis.isFirstLineAHeader());
    }

    @Test
    public void should_detect_type_without_first_line_header_when_sample_has_a_header_plus_one_line() {
        // given
        String firstRecord = "ID; Name; Welcome message; Salary";
        String secondRecord = "0001;Toto; Hello;1,000.02";
        List<String> list = Arrays.asList(firstRecord, secondRecord);
        Separator separator = new Separator(';');
        separator.incrementCount(1);

        // when
        CSVFastHeaderAndTypeAnalyzer analysis = new CSVFastHeaderAndTypeAnalyzer(list, separator);

        // then
        List<Type> expectedTypes = Arrays.asList(Type.INTEGER, Type.STRING, Type.STRING, Type.DOUBLE);
        Assert.assertArrayEquals(expectedTypes.toArray(), analysis.getHeaders().stream().map( p -> p.getValue()).toArray());
        Assert.assertTrue(analysis.isFirstLineAHeader());
    }

    @Test
    public void TDP_289_first_case() {
        // given
        String firstRecord = "1; Abdelaziz; CA;";
        String secondRecord = "2; Charles; MI;";
        List<String> list = Arrays.asList(firstRecord, secondRecord);
        Separator separator = new Separator(';');
        separator.incrementCount(1);

        // when
        CSVFastHeaderAndTypeAnalyzer analysis = new CSVFastHeaderAndTypeAnalyzer(list, separator);

        // then
        List<String> expectedHeaders = Arrays.asList("COL1", "COL2", "COL3");
        List<Type> expectedTypes = Arrays.asList(Type.INTEGER, Type.STRING, Type.STRING);
        Assert.assertArrayEquals(expectedHeaders.toArray(), analysis.getHeaders().stream().map( p -> p.getKey()).toArray());
        Assert.assertArrayEquals(expectedTypes.toArray(), analysis.getHeaders().stream().map( p -> p.getValue()).toArray());
        Assert.assertFalse(analysis.isFirstLineAHeader());
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-1249
     */
    @Test
    public void should_strip_quotes_in_header() {
        // given
        List<String> list = Arrays.asList("\"user_id\";\"birth\";\"country\";page_visited;first_item",
                "4dc1548af;11/9/1970;France;6.0;22.0");
        Separator separator = new Separator(';');
        separator.incrementCount(1);

        // when
        CSVFastHeaderAndTypeAnalyzer analysis = new CSVFastHeaderAndTypeAnalyzer(list, separator);

        // then
        List<String> expectedHeaders = Arrays.asList("user_id", "birth", "country", "page_visited", "first_item");
        Assert.assertArrayEquals(expectedHeaders.toArray(), analysis.getHeaders().stream().map( p -> p.getKey()).toArray());
    }

    @Test
    public void should_not_detect_first_line_as_column() {
        // given
        List<String> list = Arrays.asList("\"1000800\",\"2.0 PET X8 LIFT SOUR CHERRY\"",
                "\"1001000\",\"1.0 RGB X12 COCA COLA\"", "\"1001300\",\"2.0 PET X6 LILIA\"");
        Separator separator = new Separator(',');
        separator.incrementCount(1);
        separator.incrementCount(2);
        separator.incrementCount(3);

        // when
        CSVFastHeaderAndTypeAnalyzer analysis = new CSVFastHeaderAndTypeAnalyzer(list, separator);

        // then
        List<String> expectedHeaders = Arrays.asList("COL1", "COL2");
        Assert.assertArrayEquals(expectedHeaders.toArray(), analysis.getHeaders().stream().map( p -> p.getKey()).toArray());
    }
}
