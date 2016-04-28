/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

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
export default function FolderService(state, StateService, FolderRestService, StorageService) {
    'ngInject';

    return {
        init: init,
        children: FolderRestService.children,
        create: FolderRestService.create,
        rename: FolderRestService.rename,
        remove: FolderRestService.remove,
        search: FolderRestService.search,
        refreshContent: refreshContent,
    };

    /**
     * @ngdoc method
     * @name init
     * @methodOf data-prep.services.folder.service:FolderService
     * @param {string} path The folder to init
     * @description Init the sort parameters and folder content
     */
    function init(path) {
        _refreshPreparationsSort();
        _refreshPreparationsOrder();
        return refreshContent(path);
    }

    /**
     * @ngdoc method
     * @name refreshContent
     * @methodOf data-prep.services.folder.service:FolderService
     * @param {string} path The folder to init
     * @returns {Promise} The GET promise
     */
    function refreshContent(path) {
        const sort = state.inventory.preparationsSort.id;
        const order = state.inventory.preparationsOrder.id;
        return FolderRestService.getContent(path, sort, order)
            .then((content) => StateService.setFolder(path, content));
    }

    /**
     * @ngdoc method
     * @methodOf data-prep.services.folder.service:FolderService
     * @name _refreshPreparationsSort
     * @description Refresh the actual sort parameter
     * */
    function _refreshPreparationsSort() {
        const savedSort = StorageService.getPreparationsSort();
        if (savedSort) {
            StateService.setPreparationsSort(_.find(state.inventory.sortList, {id: savedSort}));
        }
    }

    /**
     * @ngdoc method
     * @methodOf data-prep.services.folder.service:FolderService
     * @name _refreshPreparationsOrder
     * @description Refresh the actual order parameter
     */
    function _refreshPreparationsOrder() {
        const savedSortOrder = StorageService.getPreparationsOrder();
        if (savedSortOrder) {
            StateService.setPreparationsOrder(_.find(state.inventory.orderList, {id: savedSortOrder}));
        }
    }
}