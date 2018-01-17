/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.folder.service:FolderService
 * @description Folder service. This service provide the entry point to the Folder service.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.folder.service:FolderRestService
 * @requires data-prep.services.utils.service:StorageService
 */
export default function FolderService($q, $state, $timeout, state, StateService, FolderRestService, OnboardingService, StorageService) {
	'ngInject';

	return {
		adaptFolders,
		adaptPreparations,
		changeSort,
		getFolderActions,
		getPreparationActions,
		init,
		refresh,
		refreshBreadcrumbChildren,
		refreshCurrentFolder,
		children,
		create,
		rename,
		remove,
		tree: FolderRestService.tree,
	};

	/**
	 * @ngdoc method
	 * @name startOnboarding
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description launch onboarding
	 */
	function startOnboarding() {
		const tourId = 'preparation';
		if (OnboardingService.shouldStartTour(tourId)) {
			OnboardingService.startTour(tourId);
		}
	}

	/**
	 * @ngdoc method
	 * @name init
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @param {string} id The folder id to init
	 * @description Init the sort parameters and folder content
	 */
	function init(id) {
		refreshPreparationsSort();
		return this.refresh(id).then(startOnboarding);
	}

	/**
	 * @ngdoc method
	 * @name refresh
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description Refresh the actual folder
	 * @param {string} id The folder to init
	 * @returns {Promise} The GET promise
	 */
	function refresh(id) {
		const folderId = id || state.inventory.homeFolder.id;

		const sort = state.inventory.folder.sort.field;
		const order = state.inventory.folder.sort.isDescending ? 'desc' : 'asc';

		const breadcrumbPromise = FolderRestService.getById(folderId);
		const contentPromise = FolderRestService
			.getContent(folderId, sort, order)
			.then(content => ({
				folders: this.adaptFolders(content.folders),
				preparations: this.adaptPreparations(content.preparations),
			}));
		return $q.all([breadcrumbPromise, contentPromise])
			.then(([breadcrumb, content]) => {
				const currentFolder = breadcrumb.folder;
				const fullBreadcrumb = breadcrumb.hierarchy.concat(currentFolder);
				StateService.setFolder(currentFolder, content);
				StateService.setBreadcrumb(fullBreadcrumb);
			});
	}

	/**
	 * @ngdoc method
	 * @name adaptPreparations
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description Adapt preparation for UI components
	 * @param {object[]} preparations The preparation
	 * @returns {object[]} The adapted preparations
	 */
	function adaptPreparations(preparations) {
		return preparations.map(item => ({
			id: item.id,
			type: 'preparation',
			name: item.name,
			author: item.owner && item.owner.displayName,
			creationDate: moment(item.creationDate).fromNow(),
			lastModificationDate: moment(item.lastModificationDate).fromNow(),
			datasetName: item.dataset.dataSetName,
			nbSteps: item.steps.length - 1, // remove root step
			icon: 'talend-dataprep',
			displayMode: 'text',
			className: 'list-item-preparation',
			actions: this.getPreparationActions(item),
			model: item,
		}));
	}

	/**
	 * @ngdoc method
	 * @name adaptFolders
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description Adapt folders for UI components
	 * @param {object[]} folders The folders
	 * @returns {object[]} The adapted folders
	 */
	function adaptFolders(folders) {
		return folders.map(item => ({
			id: item.id,
			type: 'folder',
			name: item.name,
			author: item.owner && item.owner.displayName,
			creationDate: moment(item.creationDate).fromNow(),
			lastModificationDate: moment(item.lastModificationDate).fromNow(),
			icon: 'talend-folder',
			displayMode: 'text',
			className: 'list-item-folder',
			actions: this.getFolderActions(item),
			model: item,
		}));
	}

	/**
	 * @ngdoc method
	 * @name getPreparationActions
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description Get the preparations' available actions
	 * @returns {string[]} The array of actions available on the preparation
	 */
	function getPreparationActions() {
		return ['inventory:edit', 'preparation:copy-move', 'preparation:remove'];
	}

	/**
	 * @ngdoc method
	 * @name getFolderActions
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description Get the folders' available actions
	 * @returns {string[]} The array of actions available on the folder
	 */
	function getFolderActions() {
		return ['inventory:edit', 'preparation:folder:remove'];
	}

	/**
	 * @ngdoc method
	 * @name refreshBreadcrumbChildren
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @param {string} id The folder to refresh
	 * @returns {Promise} The process promise
	 */
	function refreshBreadcrumbChildren(id) {
		return FolderRestService.children(id)
			.then((children) => {
				StateService.setBreadcrumbChildren(id, children);
			});
	}

	/**
	 * @ngdoc method
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @name refreshPreparationsSort
	 * @description Refresh the actual sort parameter
	 * */
	function refreshPreparationsSort() {
		const savedSort = StorageService.getPreparationsSort();
		if (savedSort) {
			StateService.setPreparationsSort(savedSort.field, savedSort.isDescending);
		}
	}

	/**
	 * @ngdoc method
	 * @name children
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description Get a folder's children
	 * @param {string} parentId The parent id
	 * @returns {Promise} The GET promise
	 */
	function children(parentId = state.inventory.homeFolder.id) {
		return FolderRestService.children(parentId);
	}

	/**
	 * @ngdoc method
	 * @name create
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description Create a folder
	 * @param {string} parentId The parent id
	 * @param {string} path The relative path to create (from parent)
	 * @returns {Promise} The PUT promise
	 */
	function create(parentId = state.inventory.homeFolder.id, path) {
		return FolderRestService.create(parentId, path);
	}

	/**
	 * @ngdoc method
	 * @name rename
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description Rename a folder
	 * @param {string} folderId The folder id to rename
	 * @param {string} newName The new name
	 * @returns {Promise} The PUT promise
	 */
	function rename(folderId = state.inventory.homeFolder.id, newName) {
		return FolderRestService.rename(folderId, newName);
	}

	/**
	 * @ngdoc method
	 * @name changeSort
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description Change the folder sort
	 * @param {string} field The sort field
	 * @param {string} isDescending True if sort is descending
	 */
	function changeSort({ field, isDescending }) {
		const oldSort = state.inventory.folder.sort;
		const oldField = oldSort.field;
		const oldIsDescending = oldSort.isDescending;

		StateService.setPreparationsSort(field, isDescending);

		return this.refreshCurrentFolder()
			.then(() => StorageService.setPreparationsSort(field, isDescending))
			.catch(() => StateService.setPreparationsSort(oldField, oldIsDescending));
	}

	/**
	 * @ngdoc method
	 * @name refreshCurrentFolder
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description Refresh current folder
	 */
	function refreshCurrentFolder() {
		return this.refresh(state.inventory.folder.metadata.id);
	}

	/**
	 * @ngdoc method
	 * @name remove
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description Remove The folder
	 */
	function remove({ id }) {
		return FolderRestService.remove(id)
			.then(() => this.refreshCurrentFolder());
	}
}
