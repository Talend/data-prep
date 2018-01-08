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
 * @name data-prep.services.folder.service:FolderRestService
 * @description Folder service. This service provide the entry point to the backend folder REST api.<br/>
 * <b style="color: red;">WARNING : do NOT use this service directly.
 * {@link data-prep.services.folder.service:FolderService FolderService} must be the only entry point for folder</b>
 */
export default function FolderRestService($http, RestURLs) {
	'ngInject';

	return {
		children,
		create,
		getContent,
		rename,
		remove,
		tree,
		getById,
	};

    /**
     * @ngdoc method
     * @name children
     * @methodOf data-prep.services.folder.service:FolderRestService
     * @description Get a folder's children
     * @param {string} parentId The parent id
     * @returns {Promise} The GET promise
     */
	function children(parentId) {
		const url = `${RestURLs.folderUrl}?parentId=${encodeURIComponent(parentId)}`;
		return $http.get(url).then(res => res.data);
	}

    /**
     * @ngdoc method
     * @name create
     * @methodOf data-prep.services.folder.service:FolderRestService
     * @description Create a folder
     * @param {string} parentId The parent id
     * @param {string} path The relative path to create (from parent)
     * @returns {Promise} The PUT promise
     */
	function create(parentId, path) {
		return $http.put(`${RestURLs.folderUrl}?parentId=${encodeURIComponent(parentId)}&path=${encodeURIComponent(path)}`);
	}

    /**
     * @ngdoc method
     * @name getContent
     * @methodOf data-prep.services.folder.service:FolderRestService
     * @description List the preparations and folders contained in the specified folder
     * @param {string} folderId The folder id to list
     * @param {string} sortType Sort by specified type
     * @param {string} sortOrder Sort in specified order
     * @returns {Promise} The GET promise
     */
	function getContent(folderId, sortType, sortOrder) {
		let url = `${RestURLs.folderUrl}/${encodeURIComponent(folderId)}/preparations`;

		const options = [];
		if (sortType) {
			options.push(`sort=${sortType}`);
		}

		if (sortOrder) {
			options.push(`order=${sortOrder}`);
		}

		if (options.length) {
			url = `${url}?${options.join('&')}`;
		}

		return $http.get(url).then(result => result.data);
	}

    /**
     * @ngdoc method
     * @name remove
     * @methodOf data-prep.services.folder.service:FolderRestService
     * @description Remove a folder
     * @param {string} folderId the folder id to remove
     * @returns {Promise} The DELETE promise
     */
	function remove(folderId) {
		return $http.delete(`${RestURLs.folderUrl}/${encodeURIComponent(folderId)}`);
	}

    /**
     * @ngdoc method
     * @name rename
     * @methodOf data-prep.services.folder.service:FolderRestService
     * @description Rename a folder
     * @param {string} folderId The folder id to rename
     * @param {string} newName The new name
     * @returns {Promise} The PUT promise
     */
	function rename(folderId, newName) {
		return $http.put(`${RestURLs.folderUrl}/${encodeURIComponent(folderId)}/name`, newName);
	}

    /**
     * @ngdoc method
     * @name tree
     * @methodOf data-prep.services.folder.service:FolderRestService
     * @description Get the whole folder tree
     * @returns {Promise} The GET promise
     */
	function tree() {
		return $http.get(`${RestURLs.folderUrl}/tree`).then(res => res.data);
	}

    /**
     * @ngdoc method
     * @name getById
     * @methodOf data-prep.services.folder.service:FolderRestService
     * @description Get the folder metadata with its hierarchy
     * @param {string} id the folder id
     * @returns {Promise} The GET promise
     */
	function getById(id) {
		return $http.get(`${RestURLs.folderUrl}/${id}`).then(res => res.data);
	}
}
