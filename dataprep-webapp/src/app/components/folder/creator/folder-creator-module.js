/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import FolderCreatorContainer from './folder-creator-container';
import FolderCreatorFormContainer from './form/folder-creator-form-component';

const MODULE_NAME = 'data-prep.folder-creator';

/**
 * @ngdoc object
 * @name data-prep.folder-creator
 * @description This module manage folder creation
 */
angular.module(MODULE_NAME,
	[])
    .component('folderCreatorForm', FolderCreatorFormContainer)
    .component('folderCreator', FolderCreatorContainer);

export default MODULE_NAME;
