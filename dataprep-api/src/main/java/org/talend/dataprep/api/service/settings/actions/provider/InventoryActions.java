//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.settings.actions.provider;

import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;

import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_METHOD_KEY;

public interface InventoryActions {
    ActionSettings INVENTORY_EDIT = ActionSettings.builder()
            .id("inventory:edit")
            .name("Edit name")
            .icon("talend-pencil")
            .type("@@inventory/EDIT")
            .payload(PAYLOAD_METHOD_KEY, "enableInventoryEdit")
            .build();

    ActionSettings INVENTORY_CANCEL_EDIT = ActionSettings.builder()
            .id("inventory:cancel-edit")
            .name("Cancel name edition")
            .type("@@inventory/CANCEL_EDIT")
            .payload(PAYLOAD_METHOD_KEY, "disableInventoryEdit")
            .build();
}
