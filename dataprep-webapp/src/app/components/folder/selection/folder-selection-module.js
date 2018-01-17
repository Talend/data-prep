/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_FOLDER_MODULE from '../../../services/folder/folder-module';

import FolderSelection from './folder-selection-component';
import FolderTreeComponent from './folder-tree/folder-tree-component';
import FolderTreeNodeComponent from './folder-tree-node/folder-tree-node-component';

const MODULE_NAME = 'data-prep.folder-selection';

/**
 * @ngdoc object
 * @name data-prep.folder-selection
 * @description This module contains the controller and directives to select a folder
 * @requires data-prep.services.state
 * @requires data-prep.services.folder
 */
angular.module(MODULE_NAME, [SERVICES_FOLDER_MODULE])
    .component('folderTreeNode', FolderTreeNodeComponent)
    .component('folderTree', FolderTreeComponent)
    .component('folderSelection', FolderSelection);

export default MODULE_NAME;
