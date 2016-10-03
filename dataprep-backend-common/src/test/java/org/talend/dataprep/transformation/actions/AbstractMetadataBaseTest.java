// ============================================================================
//
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

package org.talend.dataprep.transformation.actions;

import java.io.IOException;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.test.MockTestApplication;
import org.talend.dataprep.transformation.actions.common.ActionFactory;
import org.talend.dataprep.transformation.actions.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for all related unit tests that deal with metadata
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MockTestApplication.class)
public abstract class AbstractMetadataBaseTest {

    @Autowired
    protected ActionRegistry actionRegistry;

    /** The dataprep ready jackson builder. */
    @Autowired
    public ObjectMapper mapper;

    @Autowired
    public ActionFactory factory;

    @Autowired
    protected AnalyzerService analyzerService;

    protected String generateJson(String token, String operator) {
        ReplaceOnValueHelper r = new ReplaceOnValueHelper(token, operator);
        try {
            return mapper.writeValueAsString(r);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    protected ColumnMetadata createMetadata(String id, String name, Type type, String statisticsFileName) throws IOException {
        ColumnMetadata column = createMetadata(id, name, type);
        ObjectMapper mapper = new ObjectMapper();
        final Statistics statistics = mapper.readerFor(Statistics.class)
                .readValue(getClass().getResourceAsStream("/org/talend/dataprep/transformation/actions/date/" + statisticsFileName));
        column.setStatistics(statistics);
        return column;
    }

    protected ColumnMetadata createMetadata(String id, String name) {
        return createMetadata(id, name, Type.STRING);
    }

    protected ColumnMetadata createMetadata(String id, String name, Type type) {
        return columnBaseBuilder().computedId(id).name(name).type(type).build();
    }

    protected ColumnMetadata.Builder columnBaseBuilder() {
        return ColumnMetadata.Builder.column();
    }
}
