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

package org.talend.dataprep.schema;

import org.springframework.stereotype.Component;

/**
 * A special implementation of {@link FormatFamily} to serve as fallback and indicates that the provided content is not
 * supported in data prep.
 */
@Component(UnsupportedFormatFamily.BEAN_ID)
public class UnsupportedFormatFamily implements FormatFamily {

    protected static final String BEAN_ID = "formatGuess#any";

    @Override
    public String getMediaType() {
        return "*/*"; //$NON-NLS-1$
    }

    @Override
    public Serializer getSerializer() {
        throw new UnsupportedOperationException();
    }

    public SchemaParser getSchemaGuesser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DraftValidator getDraftValidator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBeanId() {
        return BEAN_ID;
    }
}
