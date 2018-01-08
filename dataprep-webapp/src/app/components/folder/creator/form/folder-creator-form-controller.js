/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.folder-creator.controller:FolderCreatorFormCtrl
 * @description Folder creator controller.
 * @requires data-prep.services.folder.service:FolderService
 */
export default class FolderCreatorFormCtrl {
	constructor(state, StateService, FolderService) {
		'ngInject';
		this.state = state;
		this.StateService = StateService;
		this.FolderService = FolderService;

		this.name = '';
	}

	/**
	 * @ngdoc method
	 * @name createFolder
	 * @methodOf data-prep.folder-creator.controller:FolderCreatorFormCtrl
	 * @description Create a new folder
	 */
	createFolder() {
		this.folderNameForm.$commitViewValue();
		const currentFolderId = this.state.inventory.folder.metadata.id;
		return this.FolderService.create(currentFolderId, this.name)
			.then(() => this.StateService.toggleFolderCreator())
			.then(() => {
				this.FolderService.refresh(currentFolderId);
			});
	}
}
