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

package org.talend.dataprep.transformation.aggregation.api.json;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.talend.dataprep.transformation.aggregation.api.AggregationResult;
import org.talend.dataprep.transformation.aggregation.api.WorkingContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Serialize Aggregation in json.
 */
public class AggregationResultSerializer extends JsonSerializer<AggregationResult> {

    /**
     * @see JsonSerializer#serialize(Object, JsonGenerator, SerializerProvider)
     */
    @Override
    public void serialize(AggregationResult result, JsonGenerator gen, SerializerProvider provider) throws IOException {

        // sort the results first
        Comparator<Map.Entry<String, WorkingContext>> comparator = (e1, e2) -> Double.compare(e2.getValue().getValue(),
                e1.getValue().getValue());
        final List<Map.Entry<String, WorkingContext>> entries = result.entries().stream().sorted(comparator)
                .collect(Collectors.toList());

        // then write it
        gen.writeStartArray();
        for (Map.Entry<String, WorkingContext> entry : entries) {
            String key = entry.getKey();
            gen.writeStartObject();
            gen.writeStringField("data", key);
            gen.writeNumberField(result.getOperator().name(), entry.getValue().getValue());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

}
