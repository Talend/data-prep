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

package org.talend.dataprep.command.preparation;

import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.PostConstruct;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNABLE_TO_DELETE_PREPARATION_CACHE;

/**
 * Command used to evict cache content related to the preparation.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class PreparationCacheDelete extends GenericCommand<Void> {

    /**
     * The preparation id.
     */
    private final String id;

    public PreparationCacheDelete(String id) {
        super(TRANSFORM_GROUP);
        this.id = id;
    }

    @PostConstruct
    public void executeRequest() throws URISyntaxException {
        URI uri = new URIBuilder(transformationServiceUrl + "/preparation/" + id + "/cache").build();

        execute(() -> new HttpDelete(uri));
        on(HttpStatus.OK).then(asNull());
        onError(e -> new TDPException(UNABLE_TO_DELETE_PREPARATION_CACHE, e, build().put("id", this.id)));
    }
}
