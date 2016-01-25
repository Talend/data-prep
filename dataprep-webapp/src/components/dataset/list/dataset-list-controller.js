(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.dataset-list.controller:DatasetListCtrl
     * @description Dataset list controller.
     On creation, it fetch dataset list from backend and load playground if 'datasetid' query param is provided
     * @requires data-prep.services.state.service:StateService
     * @requires data-prep.services.dataset.service:DatasetService
     * @requires data-prep.services.dataset.service:DatasetListSortService
     * @requires data-prep.services.folder.service:FolderService
     * @requires data-prep.services.playground.service:PlaygroundService
     * @requires data-prep.services.uploadWorkflowService.service:UploadWorkflowService
     * @requires data-prep.services.datasetWorkflowService.service:UpdateWorkflowService
     * @requires data-prep.services.utils.service:MessageService
     * @requires talend.widget.service:TalendConfirmService
     */
    function DatasetListCtrl ($timeout, $translate, $stateParams, StateService, DatasetService, DatasetListSortService, PlaygroundService,
                              TalendConfirmService, MessageService, UploadWorkflowService, UpdateWorkflowService, FolderService, state) {
        var vm = this;

        vm.datasetService = DatasetService;
        vm.uploadWorkflowService = UploadWorkflowService;
        vm.state = state;

        vm.isCloningDs = false;
        vm.isMovingDs = false;

        /**
         * @ngdoc property
         * @name folderName
         * @propertyOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description The folder name
         * @type {String}
         */
        vm.folderName = '';

        /**
         * @ngdoc property
         * @name sortList
         * @propertyOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description The sort list
         * @type {array}
         */
        vm.sortList = DatasetListSortService.getSortList();

        /**
         * @ngdoc property
         * @name orderList
         * @propertyOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description The sort order list
         * @type {string}
         */
        vm.orderList = DatasetListSortService.getOrderList();

        /**
         * @ngdoc property
         * @name sortSelected
         * @propertyOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description Selected sort.
         * @type {object}
         */
        vm.sortSelected = DatasetListSortService.getSortItem();

        /**
         * @ngdoc property
         * @name sortOrderSelected
         * @propertyOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description Selected sort order.
         * @type {object}
         */
        vm.sortOrderSelected = DatasetListSortService.getOrderItem();

        /**
         * @type {Array} folder found after a search
         */
        vm.foldersFound = [];

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
            if (vm.sortSelected === sortType) {
                return;
            }

            var oldSort = vm.sortSelected;
            vm.sortSelected = sortType;
            DatasetListSortService.setSort(sortType.id);

            FolderService.getContent(state.folder.currentFolder)
                .catch(function () {
                    vm.sortSelected = oldSort;
                    DatasetListSortService.setSort(oldSort.id);
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
            if (vm.sortOrderSelected === order) {
                return;
            }

            var oldSort = vm.sortOrderSelected;
            vm.sortOrderSelected = order;
            DatasetListSortService.setOrder(order.id);

            FolderService.getContent(state.folder.currentFolder)
                .catch(function () {
                    vm.sortOrderSelected = oldSort;
                    DatasetListSortService.setOrder(oldSort.id);
                });
        };

        /**
         * @ngdoc method
         * @name open
         * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description [PRIVATE] Initiate a new preparation from dataset
         * @param {object} dataset The dataset to open
         */
        function open (dataset) {
            PlaygroundService.initPlayground(dataset)
                .then(function () {
                    $timeout(StateService.showPlayground);
                });
        }

        /**
         * @ngdoc method
         * @name uploadUpdatedDatasetFile
         * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description [PRIVATE] updates the existing dataset with the uploadd one
         */
        vm.uploadUpdatedDatasetFile = function uploadUpdatedDatasetFile (dataset) {
            UpdateWorkflowService.updateDataset(vm.updateDatasetFile[0], dataset);
        };

        /**
         * @ngdoc method
         * @name remove
         * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description Delete a dataset
         * @param {object} dataset The dataset to delete
         */
        vm.remove = function remove (dataset) {
            TalendConfirmService.confirm({disableEnter: true}, ['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'], {
                type: 'dataset',
                name: dataset.name
            })
                .then(function () {
                    return DatasetService.delete(dataset);
                })
                .then(function () {
                    FolderService.getContent(state.folder.currentFolder);
                    MessageService.success('REMOVE_SUCCESS_TITLE', 'REMOVE_SUCCESS', {
                        type: 'dataset',
                        name: dataset.name
                    });
                });
        };

        /**
         * @ngdoc method
         * @name clone
         * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description perform the dataset cloning to the folder destination
         */
        vm.clone = function () {
            vm.isCloningDs = true;
            vm.cloneNameForm.$commitViewValue();

            DatasetService.clone(vm.datasetToClone, vm.folderDestination, vm.cloneName)
                .then(function () {
                    MessageService.success('COPY_SUCCESS_TITLE', 'COPY_SUCCESS');

                    // force going to current folder to refresh the content
                    FolderService.getContent(state.folder.currentFolder);
                    // reset some values to initial values
                    vm.folderDestinationModal = false;
                    vm.datasetToClone = null;
                    vm.folderDestination = null;
                    vm.foldersFound = [];
                    vm.cloneName = '';
                    vm.isCloningDs = false;

                }, function () {
                    vm.isCloningDs = false;
                    setTimeout(vm.focusOnNameInput, 1100);
                });
        };

        /**
         * @ngdoc method
         * @name move
         * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description perform the dataset moving to the folder destination
         */
        vm.move = function () {
            vm.isMovingDs = true;
            vm.cloneNameForm.$commitViewValue();

            DatasetService.move(vm.datasetToClone, state.folder.currentFolder, vm.folderDestination, vm.cloneName)
                .then(function () {
                    MessageService.success('MOVE_SUCCESS_TITLE', 'MOVE_SUCCESS');

                    // force going to current folder to refresh the content
                    FolderService.getContent(state.folder.currentFolder);

                    // reset some values to initial values
                    vm.folderDestinationModal = false;
                    vm.datasetToClone = null;
                    vm.folderDestination = null;
                    vm.foldersFound = [];
                    vm.cloneName = '';
                    vm.isMovingDs = false;

                }, function () {
                    vm.isMovingDs = false;
                    setTimeout(vm.focusOnNameInput, 1100);
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
        vm.rename = function rename (dataset, name) {
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
         * @name loadUrlSelectedDataset
         * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description [PRIVATE] Load playground with provided dataset id, if present in route param
         * @param {object[]} datasets List of all user's datasets
         */
        var loadUrlSelectedDataset = function loadUrlSelectedDataset (datasets) {
            if ($stateParams.datasetid) {
                var selectedDataset = _.find(datasets, function (dataset) {
                    return dataset.id === $stateParams.datasetid;
                });

                if (selectedDataset) {
                    open(selectedDataset);
                }
                else {
                    MessageService.error('PLAYGROUND_FILE_NOT_FOUND_TITLE', 'PLAYGROUND_FILE_NOT_FOUND', {type: 'dataset'});
                }
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
                .then(FolderService.getContent.bind(null, state.folder.currentFolder));
        };

        //-------------------------------
        // Folder
        //-------------------------------

        vm.goToFolder = FolderService.getContent;

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
        vm.addFolder = function addFolder () {
            vm.folderNameForm.$commitViewValue();

            var pathToCreate = (state.folder.currentFolder.id ? state.folder.currentFolder.id : '') + '/' + vm.folderName;
            FolderService.create(pathToCreate)
                .then(function () {
                    FolderService.getContent(state.folder.currentFolder);
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
        vm.renameFolder = function renameFolder (folder, newName) {
            var path = folder.id;
            var lastSlashIndex = path.lastIndexOf('/');
            var newPath = path.substring(0, lastSlashIndex) + '/' + newName;
            FolderService.rename(path, newPath)
                .then(function () {
                    FolderService.getContent(state.folder.currentFolder);
                });
        };

        /**
         * @ngdoc method
         * @name removeFolder
         * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description Remove a folder
         * @param {object} folder The folder to remove
         */
        vm.removeFolder = function removeFolder (folder) {
            FolderService.remove(folder.id)
                .then(function () {
                    FolderService.getContent(state.folder.currentFolder);
                });
        };


        /**
         * @ngdoc method
         * @name openFolderChoice
         * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description Display folder destination choice modal
         * @param {object} dataset - the dataset to clone or move
         */
        vm.openFolderChoice = function openFolderChoice (dataset) {
            vm.datasetToClone = dataset;
            vm.foldersFound = [];
            vm.searchFolderQuery = '';
            vm.cloneName = dataset.name;

            var toggleToCurrentFolder = state.folder && state.folder.currentFolder && state.folder.currentFolder.id;

            if (toggleToCurrentFolder) {
                var pathParts = state.folder.currentFolder.id.split('/');
                var currentPath = pathParts[0];
            }

            var rootFolder = {id: '', path: '', collapsed: false, name: $translate.instant('HOME_FOLDER')};

            FolderService.children()
                .then(function (res) {
                    rootFolder.nodes = res.data;
                    vm.chooseFolder(rootFolder);

                    vm.folders = [rootFolder];
                    _.forEach(vm.folders[0].nodes, function (folder) {
                        folder.collapsed = true;
                        // recursive toggle until we reach the current folder
                        if (toggleToCurrentFolder && folder.id === currentPath) {
                            vm.toggle(folder, pathParts.length > 0 ? _.slice(pathParts, 1) : null, currentPath);
                            vm.chooseFolder(folder);
                        }
                    });
                    vm.folderDestinationModal = true;
                });
        };

        /**
         * @ngdoc method
         * @name toggle
         * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description load folder children
         * @param {object} folder The folder to display children
         * @param {array} pathParts All path parts
         * @param {string} currentPath The current path for recursive call
         */
        vm.toggle = function toggle (folder, pathParts, currentPath) {
            if (!folder.collapsed) {
                folder.collapsed = true;
            } else {
                if (!folder.nodes) {
                    FolderService.children(folder.id)
                        .then(function (res) {
                            folder.nodes = res.data ? res.data : [];
                            vm.collapseNodes(folder);
                            if (pathParts && pathParts[0]) {
                                currentPath += currentPath ? '/' + pathParts[0] : pathParts[0];
                                _.forEach(folder.nodes, function (folder) {
                                    if (folder.id === currentPath) {
                                        vm.toggle(folder, pathParts.length > 0 ? _.slice(pathParts, 1) : null, currentPath);
                                        vm.chooseFolder(folder);
                                    }
                                });
                            }
                        });

                } else {
                    vm.collapseNodes(folder);
                }
            }
        };


        /**
         * @ngdoc method
         * @name chooseFolder
         * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description Set folder destination choice
         * @param {object} folder - the folder to use for cloning the dataset
         */
        vm.chooseFolder = function (folder) {
            var previousSelected = vm.folderDestination;
            if (previousSelected) {
                previousSelected.selected = false;
            }
            vm.folderDestination = folder;
            folder.selected = true;
        };

        /**
         * @ngdoc method
         * @name collapseNodes
         * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description utility function to collapse nodes
         * @param {object} node - parent node of childs to collapse
         */
        vm.collapseNodes = function (node) {
            _.forEach(node.nodes, function (folder) {
                folder.collapsed = true;
            });
            if (node.nodes.length > 0) {
                node.collapsed = false;
            } else {
                node.collapsed = !node.collapsed;
            }
        };


        /**
         * @ngdoc method
         * @name searchFolders
         * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
         * @description Search folders
         */
        vm.searchFolders = function searchFolders () {

            vm.foldersFound = [];
            if (vm.searchFolderQuery) {
                //Add the root folder if it matches the filter
                var n = $translate.instant('HOME_FOLDER').indexOf(vm.searchFolderQuery);

                FolderService.search(vm.searchFolderQuery)
                    .then(function (response) {
                        if (n > -1) {
                            var rootFolder = {id: '', path: '', name: $translate.instant('HOME_FOLDER')};
                            vm.foldersFound.push(rootFolder);
                            vm.foldersFound = vm.foldersFound.concat(response.data);
                        } else {
                            vm.foldersFound = response.data;
                        }
                        if (vm.foldersFound.length > 0) {
                            vm.chooseFolder(vm.foldersFound[0]); //Select by default first folder
                        }
                    });
            } else {
                vm.chooseFolder(vm.folders[0]);  //Select by default first folder
            }

        };

        // load the datasets
        DatasetService
            .getDatasets()
            .then(loadUrlSelectedDataset);
    }

    /**
     * @ngdoc property
     * @name datasets
     * @propertyOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description The dataset list.
     * This list is bound to {@link data-prep.services.dataset.service:DatasetService DatasetService}.datasetsList()
     */
    Object.defineProperty(DatasetListCtrl.prototype,
        'datasets', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.datasetService.datasetsList();
            }
        });

    /**
     * @ngdoc property
     * @name currentFolderContent
     * @propertyOf data-prep.folder.controller:FolderCtrl
     * @description The folder content list.
     * This list is bound to {@link data-prep.services.state.service:FolderStateService}.state.currentFolderContent
     */
    Object.defineProperty(DatasetListCtrl.prototype,
        'currentFolderContent', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.state.folder.currentFolderContent;
            }
        });

    angular.module('data-prep.dataset-list')
        .controller('DatasetListCtrl', DatasetListCtrl);
})();
