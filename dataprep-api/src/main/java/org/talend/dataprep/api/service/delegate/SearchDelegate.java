// ============================================================================
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

package org.talend.dataprep.api.service.delegate;

import java.util.stream.Stream;

/**
 * A search delegate to implement for each possible searchable categories (folder, datasets, preparations...).
 *
 * @param <T> The type of returned search results. Can be anything (objects are serialized to JSON using application's
 * context {@link com.fasterxml.jackson.databind.ObjectMapper}).
 */
public interface SearchDelegate<T> {

    /**
     * The search category is used for both filtering results in search <b>and</b> as base for the JSON field name in
     * the response.
     *
     * @return The search category name, never <code>null</code>.
     */
    String getSearchCategory();

    /**
     * @return A i18n label key for the search result.
     * @see org.talend.dataprep.i18n.MessagesBundle
     */
    String getSearchLabel();

    /**
     * @return The inventory type as requested by UI.
     */
    String getInventoryType();

    /**
     * Search the underlying category for <code>query</code>.
     *
     * @param query The name to be searched in underlying category.
     * @param strict Match strictly <code>query</code> or not.
     * @return A {@link Stream} of search results.
     */
    Stream<T> search(String query, boolean strict);
}
