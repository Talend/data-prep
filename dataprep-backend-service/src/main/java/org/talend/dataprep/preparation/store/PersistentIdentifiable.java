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

package org.talend.dataprep.preparation.store;

import org.talend.dataprep.api.preparation.Identifiable;

/**
 * A marker interface for identifiable instances persisted in a storage (even in-memory and of course others such as file
 * or mongodb or yet to be created storages).
 */
abstract class PersistentIdentifiable extends Identifiable {
}
