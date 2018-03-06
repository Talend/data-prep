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

package org.talend.dataprep.api.service.settings.actions.api;



/**
 * A dropdown element can be part of a action dropdown or an action split dropdown.
 */
public interface SettingElement {
    /**
     * Returns the action id.
     *
     * @return A {@link String} representing the action id.
     */
    String getId();

    boolean isEnabled();

    SettingElement translate();
}
