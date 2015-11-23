package org.talend.dataprep.schema;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnsupportedFormatGuesser implements FormatGuesser {

    @Autowired
    private UnsupportedFormatGuess noOpFormatGuess;

    @Override
    public FormatGuesser.Result guess(SchemaParser.Request request, String encoding) {
        return new FormatGuesser.Result(noOpFormatGuess, "UTF-8", Collections.emptyMap());
    }

}
