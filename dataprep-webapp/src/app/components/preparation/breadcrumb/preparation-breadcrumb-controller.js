/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class PreparationBreadcrumbController {
	constructor($state, state, FolderService) {
		'ngInject';
		this.$state = $state;
		this.state = state;
		this.FolderService = FolderService;
	}

    /**
     * @ngdoc method
     * @name go
     * @methodOf data-prep.preparation-header.controller:PreparationBreadcrumbController
     * @description go to a folder
     * @param {object} folder The folder to go
     */
	go(folder) {
		this.$state.go('nav.index.preparations', { folderId: folder.id });
	}

    /**
     * @ngdoc method
     * @name fetchChildren
     * @methodOf data-prep.preparation-header.controller:PreparationBreadcrumbController
     * @description fetch the content of a folder
     * @param {object} folder The folder to fetch into its children
     */
	fetchChildren(folder) {
		this.FolderService.refreshBreadcrumbChildren(folder.id);
	}
}
