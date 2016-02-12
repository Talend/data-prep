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

package org.talend.dataprep.transformation.api.action.metadata.date;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all date related unit tests.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BaseDateTests.class)
@Configuration
@ComponentScan(basePackages = "org.talend.dataprep")
public abstract class BaseDateTests {



    /**
     * @param statisticsFileName the statistics file name to use.
     * @return a row with default settings for the tests.
     */
    protected DataSetRow getDefaultRow( String statisticsFileName) throws IOException
    {

        List<ColumnMetadata> columns = new ArrayList<>( 3);
        columns.add(ColumnMetadata.Builder.column().name("recipe").type( Type.STRING).build());
        ObjectMapper mapper = new ObjectMapper();
        final Statistics statistics = mapper.readerFor( Statistics.class)
            .readValue(CompareDatesTest.class.getResourceAsStream(statisticsFileName));
        columns.add(ColumnMetadata.Builder.column().name("last update").type(Type.DATE).statistics(statistics).build());
        columns.add(ColumnMetadata.Builder.column().name("steps").type(Type.STRING).build());

        RowMetadata metadata = new RowMetadata();
        metadata.setColumns(columns);

        Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "01/01/2010");
        values.put("0002", "Bacon");

        return new DataSetRow(metadata, values);
    }

    protected ColumnMetadata createMetadata(String id, String name, Type type, String statisticsFileName) throws IOException {
        ColumnMetadata column = createMetadata(id, name, type);
        ObjectMapper mapper = new ObjectMapper();
        final Statistics statistics = mapper.readerFor(Statistics.class)
            .readValue(CompareDatesTest.class.getResourceAsStream(statisticsFileName));
        column.setStatistics(statistics);
        return column;
    }

    protected ColumnMetadata createMetadata(String id, String name) {
        return createMetadata(id, name, Type.STRING);
    }

    protected ColumnMetadata createMetadata(String id, String name, Type type) {
        return ColumnMetadata.Builder.column().computedId(id).name(name).type(type).build();
    }

}
