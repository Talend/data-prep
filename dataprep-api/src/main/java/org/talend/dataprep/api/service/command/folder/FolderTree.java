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

package org.talend.dataprep.api.service.command.folder;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;

import java.io.InputStream;

import static org.talend.dataprep.command.Defaults.pipeStream;

/**
 * List all folders in a tree representation
 */
@Component
@Scope("prototype")
public class FolderTree extends GenericCommand<InputStream> {

    /**
     * Get all the folders in a tree representation
     */
    public FolderTree() {
        super(GenericCommand.PREPARATION_GROUP);
        execute(this::onExecute);
        on(HttpStatus.OK).then(pipeStream());
    }

    private HttpRequestBase onExecute() {
        return new HttpGet(preparationServiceUrl + "/folders/tree");
    }

}
