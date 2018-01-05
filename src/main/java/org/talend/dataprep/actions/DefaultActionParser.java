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

package org.talend.dataprep.actions;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.function.Function;

import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.actions.resources.DictionaryFunctionResourceProvider;
import org.talend.dataprep.actions.resources.FunctionResourceProvider;
import org.talend.dataprep.actions.resources.LookupFunctionResourceProvider;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

public class DefaultActionParser implements ActionParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultActionParser.class);

    private static final ActionRegistry actionRegistry = new ClassPathActionRegistry(
            "org.talend.dataprep.transformation.actions");

    private final String apiUrl;

    private final String login;

    private final String password;

    private final FunctionResourceProvider[] providers;

    private boolean allowNonDistributedActions = false;

    public DefaultActionParser(String apiUrl, String login, String password) {
        this.apiUrl = apiUrl;
        this.login = login;
        this.password = password;
        this.providers = new FunctionResourceProvider[] { new LookupFunctionResourceProvider(apiUrl, login, password), //
                new DictionaryFunctionResourceProvider(actionRegistry, apiUrl, login, password) //
        };
    }

    /**
     * Indicate if parser should skip non distributed actions (actions that can't run in distributed contexts).
     *
     * @param allowNonDistributedActions <code>true</code> to allow those actions, <code>false</code> otherwise. Defaults
     * to <code>false</code> (non distributed actions are <b>not</b> allowed).
     */
    public void setAllowNonDistributedActions(boolean allowNonDistributedActions) {
        this.allowNonDistributedActions = allowNonDistributedActions;
    }

    /**
     * Return the preparation as json string that match the preparation id and optional version id.
     *
     * @param clientFormLogin the client that can retrieve the preparation.
     * @param preparationId the preparation id.
     * @param versionId the version id.
     * @return the preparation as json string that match the preparation id and optional version id.
     */
    private String getPreparation(RemoteResourceGetter clientFormLogin, String preparationId, String versionId) {
        LOGGER.debug("Retrieving preparation '{}' version '{}'", preparationId, versionId == null ? "head" : versionId);
        return clientFormLogin.retrievePreparation(apiUrl, login, password, preparationId, versionId);
    }

    private Function<IndexedRecord, IndexedRecord> internalParse(InputStream preparation) {
        final StandalonePreparationFactory transformer = new StandalonePreparationFactory();
        transformer.setAllowNonDistributedActions(allowNonDistributedActions);
        return transformer.create(preparation, providers);
    }

    @Override
    public Function<IndexedRecord, IndexedRecord> parse(String preparationId) {
        return parse(preparationId, null);
    }

    @Override
    public Function<IndexedRecord, IndexedRecord> parse(String preparationId, String versionId) {
        final RemoteResourceGetter clientFormLogin = new RemoteResourceGetter();
        String preparation = getPreparation(clientFormLogin, preparationId, versionId);
        return internalParse(IOUtils.toInputStream(preparation, UTF_8));
    }

}
