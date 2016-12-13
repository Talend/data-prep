// ============================================================================
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

package org.talend.dataprep.actions.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.actions.RemoteResourceGetter;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.actions.datablending.Lookup;

public class LookupFunctionResourceProvider implements FunctionResourceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LookupFunctionResourceProvider.class);

    private final String apiUrl;

    private final String login;

    private final String password;

    public LookupFunctionResourceProvider(String apiUrl, String login, String password) {
        this.apiUrl = apiUrl;
        this.login = login;
        this.password = password;
    }

    private Map<String, DataSetRow> getLookupDataset(RemoteResourceGetter clientFormLogin, String dataSetId,
                                                     String joinOnColumn) {
        LOGGER.debug("Retrieving lookup dataset '{}'", dataSetId);
        return clientFormLogin.retrieveLookupDataSet(apiUrl, login, password, dataSetId, joinOnColumn);
    }

    public LightweightExportableDataSet retrieveLookupDataSetFromAction(RemoteResourceGetter clientFormLogin, Action action) {
        final LightweightExportableDataSet result;

        if (StringUtils.equals(action.getName(), Lookup.LOOKUP_ACTION_NAME)) {
            final String dataSetId = action.getParameters().get(Lookup.Parameters.LOOKUP_DS_ID.getKey());
            if (StringUtils.isEmpty(dataSetId)) {
                throw new IllegalArgumentException("A lookup action must have a lookup dataset id: " + action);
            } else {
                final String joinOn = action.getParameters().get(Lookup.Parameters.LOOKUP_JOIN_ON.getKey());
                result = getLookupDataset(clientFormLogin, dataSetId, joinOn);
            }
        } else {
            throw new IllegalArgumentException(
                    "Trying to retrieve a lookup dataset from the following action: " + action.getName());
        }
        return result;
    }

    public Map<String, LightweightExportableDataSet> retrieveLookupDataSets(List<Action> actions) {
        final Map<String, LightweightExportableDataSet> result = new HashMap<>();
        final RemoteResourceGetter clientFormLogin = new RemoteResourceGetter();
        actions.stream() //
                .filter(action -> StringUtils.equals(action.getName(), Lookup.LOOKUP_ACTION_NAME)) //
                .forEach(action -> {
                    final Map<String, DataSetRow> lookup = retrieveLookupDataSetFromAction(clientFormLogin, action);
                    result.put(action.getParameters().get(Lookup.Parameters.LOOKUP_DS_ID.getKey()), lookup);
                });
        return result;
    }


    @Override
    public FunctionResource get(List<RunnableAction> actions) {
        return new LookupResource(retrieveLookupDataSets(actions));
    }
}
