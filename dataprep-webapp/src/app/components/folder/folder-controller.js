/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.folder.controller:FolderCtrl
 * @description Export controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.folder.service:FolderService
 * @requires data-prep.services.utils.service:StorageService
 */
export default function FolderCtrl(state, StateService, FolderService, StorageService) {
    'ngInject';

    var vm = this;
    vm.state = state;

    /**
     * @ngdoc method
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @name refreshDatasetsSort
     * @description refresh the actual sort parameter
     * */
    function refreshDatasetsSort() {
        var savedSort = StorageService.getDatasetsSort();
        if (savedSort) {
            StateService.setDatasetsSort(_.find(state.inventory.sortList, {id: savedSort}));
        }
    }

    /**
     * @ngdoc method
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @name refreshDatasetsOrder
     * @description refresh the actual order parameter
     */
    function refreshDatasetsOrder() {
        var savedSortOrder = StorageService.getDatasetsOrder();
        if (savedSortOrder) {
            StateService.setDatasetsOrder(_.find(state.inventory.orderList, {id: savedSortOrder}));
        }
    }

    /**
     * @ngdoc method
     * @name goToFolder
     * @methodOf data-prep.folder.controller:FolderCtrl
     * @param {object} folder - the folder to go
     */
    vm.goToFolder = function goToFolder(folder) {
        FolderService.getContent(folder);
    };

    /**
     * @ngdoc method
     * @name initMenuChildren
     * @methodOf data-prep.folder.controller:FolderCtrl
     * @param {object} folder - the folder
     * @description build the children of the folder menu entry as parameter
     */
    vm.initMenuChildren = function initMenuChildren(folder) {
        StateService.setMenuChildren([]);
        FolderService.populateMenuChildren(folder);
    };

    /**
     * Load folders on start
     */
    refreshDatasetsSort();
    refreshDatasetsOrder();

    FolderService.getContent();
}