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

package org.talend.dataprep.schema.unsupported;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.schema.FormatGuesser;
import org.talend.dataprep.schema.SchemaParser;

@Component
public class UnsupportedFormatGuesser implements FormatGuesser {

    @Autowired
    private UnsupportedFormatGuess noOpFormatGuess;

    @Override
    public FormatGuesser.Result guess(SchemaParser.Request request, String encoding) {
        return new FormatGuesser.Result(noOpFormatGuess, "UTF-8", Collections.emptyMap());
    }

}
