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

package org.talend.dataprep.dataset.store.content.file;

import static org.talend.daikon.exception.ExceptionContext.build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.talend.daikon.content.ContentServiceEnabled;
import org.talend.daikon.content.DeletableResource;
import org.talend.daikon.content.ResourceResolver;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.store.content.DataSetContentStore;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;

/**
 * Local dataset content that stores content in files.
 */
@Component("ContentStore#local")
@ConditionalOnBean(ContentServiceEnabled.class)
public class LocalFileContentStore extends DataSetContentStore {

    private static final String ROOT = "/store/datasets/content/dataset/";

    @Autowired
    private ResourceResolver resolver;

    private DeletableResource getResource(DataSetMetadata dataSetMetadata) {
        return resolver.getResource(ROOT + dataSetMetadata.getId());
    }

    @Override
    public void storeAsRaw(DataSetMetadata dataSetMetadata, InputStream dataSetContent) {
        final DeletableResource resource = getResource(dataSetMetadata);
        try (OutputStream outputStream = resource.getOutputStream()) {
            IOUtils.copy(dataSetContent, outputStream);
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_STORE_DATASET_CONTENT, e,
                    build().put("id", dataSetMetadata.getId()));
        }
    }

    @Override
    public InputStream getAsRaw(DataSetMetadata dataSetMetadata, long limit) {
        final DeletableResource resource = getResource(dataSetMetadata);
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_DATASET_CONTENT, e);
        }
    }

    @Override
    public void delete(DataSetMetadata dataSetMetadata) {
        final DeletableResource resource = getResource(dataSetMetadata);
        try {
            resource.delete();
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_DELETE_DATASET, e,
                    build().put("dataSetId", dataSetMetadata.getId()));
        }
    }

    @Override
    public void clear() {
        try {
            resolver.clear(ROOT + "/**");
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_CLEAR_DATASETS, e);
        }
    }

}
