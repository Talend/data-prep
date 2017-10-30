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

package org.talend.dataprep.api.service.settings.views.provider;

import org.talend.dataprep.api.service.settings.views.api.ViewSettings;
import org.talend.dataprep.api.service.settings.views.api.actionsbar.ActionsBarSettings;
import org.talend.dataprep.api.service.settings.views.api.list.*;

import static org.talend.dataprep.api.service.settings.actions.provider.DatasetActions.*;
import static org.talend.dataprep.api.service.settings.actions.provider.InventoryActions.INVENTORY_CANCEL_EDIT;
import static org.talend.dataprep.api.service.settings.actions.provider.MenuActions.MENU_FOLDERS;
import static org.talend.dataprep.api.service.settings.actions.provider.MenuActions.MENU_PLAYGROUND_PREPARATION;
import static org.talend.dataprep.api.service.settings.actions.provider.PreparationActions.*;
import static org.talend.dataprep.api.service.settings.views.api.actionsbar.ActionsBarSettings.ActionsPosition.LEFT;

/**
 * Lists configuration
 */
// @formatter:off
public interface ListViews {
    ViewSettings FOLDERS_LIST = ListSettings.builder()
            .id("listview:folders")
            .list(
                    ListDetailsSettings.builder()
                            .titleProps(
                                    ListTitleSettings.builder()
                                            .onClick(MENU_FOLDERS.getId())
                                            .build()
                            )
                            .build()
            )
            .build();

    ViewSettings PREPARATIONS_LIST = ListSettings.builder()
            .id("listview:preparations")
            .didMountActionCreator(PREPARATION_FOLDER_FETCH.getId())
            .list(
                    ListDetailsSettings.builder()
                            .column("name", "list.column.name")
                            .column("author", "list.column.author")
                            .column("creationDate", "list.column.created")
                            .column("lastModificationDate", "list.column.modified")
                            .column("datasetName", "list.column.dataset")
                            .column("nbSteps", "list.column.steps")
                            .itemProps(ListItemsSettings.builder().classNameKey("className").build())
                            .sort(ListSortSettings.builder().onChange(PREPARATION_SORT.getId()).build())
                            .titleProps(
                                    ListTitleSettings.builder()
                                            .displayModeKey("displayMode")
                                            .iconKey("icon")
                                            .key("name")
                                            .onClick(MENU_PLAYGROUND_PREPARATION.getId())
                                            .onEditCancel(INVENTORY_CANCEL_EDIT.getId())
                                            .onEditSubmit(PREPARATION_SUBMIT_EDIT.getId())
                                            .build()
                            )
                            .build()
            )
            .toolbar(
                    ToolbarDetailsSettings.builder()
                            .actionBar(
                                    ActionsBarSettings.builder()
                                            .action(LEFT, PREPARATION_CREATE.getId())
                                            .action(LEFT, PREPARATION_FOLDER_CREATE.getId())
                                            .build()
                            )
                            .build()
            )
            .build();

    ViewSettings DATASETS_LIST = ListSettings.builder()
            .id("listview:datasets")
            .didMountActionCreator(DATASET_FETCH.getId())
            .list(
                    ListDetailsSettings.builder()
                            .column("name", "list.column.name")
                            .column("statusActions", "list.column.actions", Boolean.TRUE, "actions")
                            .column("author", "list.column.author")
                            .column("creationDate", "list.column.created")
                            .column("nbRecords", "list.column.rows")
                            .itemProps(ListItemsSettings.builder().classNameKey("className").build())
                            .sort(ListSortSettings.builder().onChange(DATASET_SORT.getId()).build())
                            .titleProps(
                                    ListTitleSettings.builder()
                                            .displayModeKey("displayMode")
                                            .iconKey("icon")
                                            .key("name")
                                            .onClick(DATASET_OPEN.getId())
                                            .onEditCancel(INVENTORY_CANCEL_EDIT.getId())
                                            .onEditSubmit(DATASET_SUBMIT_EDIT.getId())
                                            .build()
                            )
                            .build()
            )
            .toolbar(
                    ToolbarDetailsSettings.builder()
                            .actionBar(
                                    ActionsBarSettings.builder()
                                            .action(LEFT, DATASET_CREATE.getId())
                                            .build()
                            )
                            .build()
            )
            .build();
}
// @formatter:on
