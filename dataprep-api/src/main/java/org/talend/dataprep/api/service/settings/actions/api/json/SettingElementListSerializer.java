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


package org.talend.dataprep.api.service.settings.actions.api.json;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.talend.dataprep.api.service.settings.actions.api.SettingElement;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SettingElementListSerializer extends JsonSerializer<List<SettingElement>> {

    @Override
    public void serialize(List<SettingElement> elems, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(elems.stream().map(e -> e.getId()).collect(Collectors.toList()).toString());
    }

}
