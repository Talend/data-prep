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

import java.io.Serializable;

import org.apache.avro.generic.IndexedRecord;

/**
 * A resource to be integrated in a {@link org.talend.dataprep.actions.SerializableFunction}. This resource is included
 * in function and {@link #register()} will be called on first function execution.
 *
 * @see org.talend.dataprep.actions.SerializableFunction#apply(IndexedRecord)
 */
public interface FunctionResource extends Serializable {

    /**
     * Resources in implementation are registered in static classes (no need to pass parameters for registration at the
     * time this interface was created).
     */
    void register();

}
