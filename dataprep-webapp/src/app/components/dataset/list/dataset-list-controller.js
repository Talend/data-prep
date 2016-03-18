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
 * @name data-prep.dataset-list.controller:DatasetListCtrl
 * @description Dataset list controller.
 On creation, it fetch dataset list from backend and load playground if 'datasetid' query param is provided
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.dataset.service:DatasetService
 *
 * @requires data-prep.services.datasetWorkflowService.service:UploadWorkflowService
 * @requires data-prep.services.datasetWorkflowService.service:UpdateWorkflowService
 *
 * @requires talend.widget.service:TalendConfirmService
 * @requires data-prep.services.utils.service:MessageService
 * @requires data-prep.services.utils.service:StorageService
 * @requires data-prep.services.folder.service:FolderService
 */
export default function DatasetListCtrl($stateParams, $timeout, $state, $translate, state, StateService, DatasetService,
                                        UploadWorkflowService, UpdateWorkflowService,
                                        TalendConfirmService, MessageService, FolderService, StorageService) {
    'ngInject';
    var vm = this;

    vm.datasetService = DatasetService;
    vm.uploadWorkflowService = UploadWorkflowService;
    vm.state = state;
    vm.stateService = StateService;
    vm.messageService = MessageService;
    vm.folderService = FolderService;

    /**
     * @ngdoc property
     * @name folderName
     * @propertyOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description The folder name
     * @type {String}
     */
    vm.folderName = '';

    /**
     * @type {string} name used for dataset clone
     */
    vm.cloneName = '';

    /**
     * @ngdoc method
     * @name sort
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description sort dataset by sortType by calling refreshDatasets from DatasetService
     * @param {object} sortType Criteria to sort
     */
    vm.updateSortBy = function (sortType) {
        if (state.inventory.sort.id === sortType.id) {
            return;
        }

        var oldSort = state.inventory.sort;

        StateService.setDatasetsSort(sortType);
        StorageService.setDatasetsSort(sortType.id);

        FolderService.getContent(state.inventory.currentFolder)
            .catch(function () {
                StateService.setDatasetsSort(oldSort);
                StorageService.setDatasetsSort(oldSort.id);
            });
    };

    /**
     * @ngdoc method
     * @name sort
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description sort dataset in order (ASC or DESC) by calling refreshDatasets from DatasetService
     * @param {object} order Sort order ASC(ascending) or DESC(descending)
     */
    vm.updateSortOrder = function (order) {
        if (state.inventory.order.id === order.id) {
            return;
        }

        var oldOrder = state.inventory.order;

        StateService.setDatasetsOrder(order);
        StorageService.setDatasetsOrder(order.id);

        FolderService.getContent(state.inventory.currentFolder)
            .catch(function () {
                StateService.setDatasetsOrder(oldOrder);
                StorageService.setDatasetsOrder(oldOrder.id);
            });
    };

    /**
     * @ngdoc method
     * @name openPreparation
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description opens a preparation
     * @param {object} preparation The preparation to open
     */
    vm.openPreparation = function openPreparation(preparation) {
        StateService.setPreviousState('nav.index.datasets');
        StateService.setPreviousStateOptions({folderPath: $stateParams.folderPath});
        $state.go('playground.preparation', {prepid: preparation.id});
    };

    /**
     * @ngdoc method
     * @name uploadUpdatedDatasetFile
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description [PRIVATE] updates the existing dataset with the uploadd one
     */
    vm.uploadUpdatedDatasetFile = function uploadUpdatedDatasetFile(dataset) {
        UpdateWorkflowService.updateDataset(vm.updateDatasetFile[0], dataset);
    };

    /**
     * @ngdoc method
     * @name remove
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description Delete a dataset
     * @param {object} dataset The dataset to delete
     */
    vm.remove = function remove(dataset) {
        TalendConfirmService.confirm({disableEnter: true}, ['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'], {
                type: 'dataset',
                name: dataset.name
            })
            .then(function () {
                return DatasetService.delete(dataset);
            })
            .then(function () {
                FolderService.getContent(state.inventory.currentFolder);
                MessageService.success('REMOVE_SUCCESS_TITLE', 'REMOVE_SUCCESS', {
                    type: 'dataset',
                    name: dataset.name
                });
            });
    };

    /**
     * @ngdoc method
     * @name rename
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @param {object} dataset The dataset to rename
     * @param {string} name The new name
     * @description Rename a dataset
     */
    vm.rename = function rename(dataset, name) {
        var cleanName = name ? name.trim().toLowerCase() : '';
        if (cleanName) {
            if (dataset.renaming) {
                return;
            }

            if (DatasetService.getDatasetByName(cleanName)) {
                MessageService.error('DATASET_NAME_ALREADY_USED_TITLE', 'DATASET_NAME_ALREADY_USED');
                return;
            }

            dataset.renaming = true;
            var oldName = dataset.name;
            dataset.name = name;
            return DatasetService.update(dataset)
                .then(function () {
                    MessageService.success('DATASET_RENAME_SUCCESS_TITLE',
                        'DATASET_RENAME_SUCCESS');

                }).catch(function () {
                    dataset.name = oldName;
                }).finally(function () {
                    dataset.renaming = false;
                });
        }
    };

    /**
     * @ngdoc method
     * @name processCertification
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description [PRIVATE] Ask certification for a dataset
     * @param {object[]} dataset Ask certification for the dataset
     */
    vm.processCertification = function (dataset) {
        vm.datasetService
            .processCertification(dataset)
            .then(FolderService.getContent.bind(null, state.inventory.currentFolder));
    };

    //-------------------------------
    // Folder
    //-------------------------------

    vm.goToFolder = function goToFolder(folder) {
        $state.go('nav.index.datasets', {folderPath: folder.path});
    };

    /**
     * @ngdoc method
     * @name actionsOnAddFolderClick
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description run these action when clicking on Add Folder button
     */
    vm.actionsOnAddFolderClick = function () {
        vm.folderNameModal = true;
        vm.folderName = '';
    };

    /**
     * @ngdoc method
     * @name addFolder
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description Create a new folder
     */
    vm.addFolder = function addFolder() {
        vm.folderNameForm.$commitViewValue();

        var pathToCreate = (state.inventory.currentFolder.path ? state.inventory.currentFolder.path : '') + '/' + vm.folderName;
        FolderService.create(pathToCreate)
            .then(function () {
                FolderService.getContent(state.inventory.currentFolder);
                vm.folderNameModal = false;
            });
    };

    /**
     * @ngdoc method
     * @name renameFolder
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description Rename a folder
     * @param {object} folder the folder to rename
     * @param {string} newName the new last part of the path
     */
    vm.renameFolder = function renameFolder(folder, newName) {
        var path = folder.path;
        var lastSlashIndex = path.lastIndexOf('/');
        var newPath = path.substring(0, lastSlashIndex) + '/' + newName;
        FolderService.rename(path, newPath)
            .then(function () {
                FolderService.getContent(state.inventory.currentFolder);
            });
    };

    /**
     * @ngdoc method
     * @name removeFolder
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description Remove a folder
     * @param {object} folder The folder to remove
     */
    vm.removeFolder = function removeFolder(folder) {
        FolderService.remove(folder.path)
            .then(function () {
                FolderService.getContent(state.inventory.currentFolder);
            });
    };


    /**
     * @ngdoc method
     * @name openFolderSelection
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description Remove a folder
     * @param {object} dataset The dataset to clone or copy
     */
    vm.openFolderSelection = function openFolderSelection(dataset) {
        vm.datasetCopyVisibility = true;
        vm.datasetToCopyMove = dataset;
    };

    /**
     * @ngdoc method
     * @name clone
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description makes a copy of a dataset
     * @param {object} dataset to move
     * @param {object} destinationFolder destination folder
     * @param {string} name of the new dataset
     */
    vm.clone = function clone(dataset, destinationFolder, name) {
        return vm.datasetService.clone(dataset, destinationFolder, name)
            .then(function () {
                FolderService.getContent(state.inventory.currentFolder);
                MessageService.success('COPY_SUCCESS_TITLE', 'COPY_SUCCESS');
                vm.datasetCopyVisibility = false;
            });
    };

    /**
     * @ngdoc method
     * @name move
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description moves a dataset from 1 folder to another
     * @param {object} dataset to move
     * @param {object} destinationFolder destination folder
     * @param {string} name of the new dataset
     **/
    vm.move = function move(dataset, destinationFolder, name) {
        return vm.datasetService.move(dataset, destinationFolder, name)
            .then(function () {
                FolderService.getContent(state.inventory.currentFolder);
                MessageService.success('MOVE_SUCCESS_TITLE', 'MOVE_SUCCESS');
                vm.datasetCopyVisibility = false;
            });
    };

    /**
     * Load folders constent on start
     */
    FolderService.refreshDatasetsSort();
    FolderService.refreshDatasetsOrder();

    if ($stateParams.folderPath) {
        const folderDefinition = {
            path: $stateParams.folderPath,
            name: _.chain($stateParams.folderPath)
                .split('/')
                .filter((part) => part)
                .last()
                .value()
        };
        FolderService
            .getContent(folderDefinition)
            .catch(() => $state.go('nav.index.datasets', {folderPath: ''}));
    }
    else {
        FolderService.getContent();
    }
}
