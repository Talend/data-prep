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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.talend.dataprep.api.type.Type.STRING;

/**
 * Split a cell value on a separator.
 */
@Action(ExtractUrlTokens.EXTRACT_URL_TOKENS_ACTION_NAME)
public class ExtractUrlTokens extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String EXTRACT_URL_TOKENS_ACTION_NAME = "extract_url_tokens"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractUrlTokens.class);

    @Override
    public String getName() {
        return EXTRACT_URL_TOKENS_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.SPLIT.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return STRING.equals(Type.get(column.getType())) && StringUtils.equalsIgnoreCase("url", column.getDomain());
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), true)) {
            final List<ActionsUtils.AdditionalColumn> additionalColumns = new ArrayList<>();
            for (UrlTokenExtractor urlTokenExtractor : UrlTokenExtractors.URL_TOKEN_EXTRACTORS) {
                additionalColumns.add(ActionsUtils
                        .additionalColumn()
                        .withKey(urlTokenExtractor.getTokenName())
                        .withName(context.getColumnName() + urlTokenExtractor.getTokenName())
                        .withType(urlTokenExtractor.getType()));
            }
            ActionsUtils.createNewColumn(context, additionalColumns);
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String originalValue = row.get(columnId);
        URI uri = null;
        try {
            uri = new URI(originalValue);
        } catch (URISyntaxException | NullPointerException e) {
            // Nothing to do, silently skip this row, leave uri null, will be treated just below
            LOGGER.debug("Unable to parse value {}.", originalValue, e);
        }
        // if uri is null, we still loop on urlTokenExtractors in order to create the column metadata for all rows, even
        // invalid ones.
        final Map<String, String> newColumns = ActionsUtils.getTargetColumnIds(context);
        for (UrlTokenExtractor urlTokenExtractor : UrlTokenExtractors.URL_TOKEN_EXTRACTORS) {
            String cellValue;
            if (uri != null) {
                String token = urlTokenExtractor.extractToken(uri);
                if (token == null) {
                    cellValue = EMPTY;
                } else {
                    cellValue = token;
                }
            } else {
                cellValue = EMPTY;
            }
            row.set(newColumns.get(urlTokenExtractor.getTokenName()), cellValue);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }
}
