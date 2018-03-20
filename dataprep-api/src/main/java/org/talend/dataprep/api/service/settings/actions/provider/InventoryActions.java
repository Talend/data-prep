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

package org.talend.dataprep.api.service.settings.actions.provider;

import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_METHOD_KEY;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.builder;

import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;

/**
 * Common actions on folders/preparations/datasets settings
 */
// @formatter:off
public interface InventoryActions {
    ActionSettings INVENTORY_EDIT = builder()
            .id("inventory:edit")
            .name("inventory.edit")
            .icon("talend-pencil")
            .type("@@inventory/EDIT")
            .payload(PAYLOAD_METHOD_KEY, "enableInventoryEdit")
            .build();

    ActionSettings INVENTORY_CANCEL_EDIT = builder()
            .id("inventory:cancel-edit")
            .name("inventory.canceledit")
            .type("@@inventory/CANCEL_EDIT")
            .payload(PAYLOAD_METHOD_KEY, "disableInventoryEdit")
            .build();
}
// @formatter:on
