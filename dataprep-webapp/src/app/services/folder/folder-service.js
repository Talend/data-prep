/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import moment from 'moment';

/**
 * @ngdoc service
 * @name data-prep.services.folder.service:FolderService
 * @description Folder service. This service provide the entry point to the Folder service.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.folder.service:FolderRestService
 * @requires data-prep.services.utils.service:StorageService
 */
export default function FolderService($q, state, StateService, FolderRestService, StorageService) {
	'ngInject';

	return {
		adaptFolders,
		adaptPreparations,
		getFolderActions,
		getPreparationActions,
		init,
		refresh,
		refreshBreadcrumbChildren,
		children,
		create,
		rename,
		remove: FolderRestService.remove,
		tree: FolderRestService.tree,
	};

	/**
	 * @ngdoc method
	 * @name init
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @param {string} id The folder id to init
	 * @description Init the sort parameters and folder content
	 */
	function init(id) {
		refreshPreparationsSort();
		refreshPreparationsOrder();
		return this.refresh(id);
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
		const folderId = id || state.inventory.homeFolderId;

		const sort = state.inventory.preparationsSort.id;
		const order = state.inventory.preparationsOrder.id;

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
			name: item.name,
			author: item.author,
			creationDate: moment(item.creationDate).fromNow(),
			lastModificationDate: moment(item.lastModificationDate).fromNow(),
			dataset: item.dataset.dataSetName,
			nbLines: item.dataset.dataSetNbRow,
			nbSteps: item.steps.length - 1, // remove root step
			icon: 'talend-dataprep',
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
			name: item.name,
			author: item.owner.displayName,
			creationDate: moment(item.creationDate).fromNow(),
			lastModificationDate: moment(item.lastModificationDate).fromNow(),
			icon: 'talend-folder',
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
		return ['preparation:copy-move', 'preparation:remove'];
	}

	/**
	 * @ngdoc method
	 * @name getFolderActions
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @description Get the folders' available actions
	 * @returns {string[]} The array of actions available on the folder
	 */
	function getFolderActions() {
		return ['preparation:remove:folder'];
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
			StateService.setPreparationsSort(_.find(state.inventory.sortList, { id: savedSort }));
		}
	}

	/**
	 * @ngdoc method
	 * @methodOf data-prep.services.folder.service:FolderService
	 * @name refreshPreparationsOrder
	 * @description Refresh the actual order parameter
	 */
	function refreshPreparationsOrder() {
		const savedSortOrder = StorageService.getPreparationsOrder();
		if (savedSortOrder) {
			StateService.setPreparationsOrder(_.find(state.inventory.orderList, { id: savedSortOrder }));
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
	function children(parentId = state.inventory.homeFolderId) {
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
	function create(parentId = state.inventory.homeFolderId, path) {
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
	function rename(folderId = state.inventory.homeFolderId, newName) {
		return FolderRestService.rename(folderId, newName);
	}
}
