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

package org.talend.dataprep.transformation.actions.net;

import java.net.URI;

import org.talend.dataprep.api.type.Type;

/**
 * Interface for all url token extractor.
 */
public interface UrlTokenExtractor {

    /**
     * @return the token name.
     */
    String getTokenName();

    /**
     * @param url the url to extract the token from.
     * @return the extracted token.
     */
    String extractToken(URI url);

    /**
     * @return token type (default is String).
     */
    default Type getType() {
        return Type.STRING;
    }
}
