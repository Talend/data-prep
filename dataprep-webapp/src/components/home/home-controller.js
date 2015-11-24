(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.home.controller:HomeCtrl
     * @description Home controller.
     * @requires data-prep.services.dataset.service:DatasetService
     * @requires talend.widget.service:TalendConfirmService
     * @requires data-prep.services.datasetWorkflowService.service:UploadWorkflowService
     * @requires data-prep.services.datasetWorkflowService.service:UpdateWorkflowService
     * @requires data-prep.services.state.service:StateService
     * @requires data-prep.services.state.constant:state
     * @requires data-prep.services.folder.service:FolderService
     */
    function HomeCtrl($window, UploadWorkflowService, UpdateWorkflowService, DatasetService, TalendConfirmService, StateService, state, FolderService, $state) {
        var vm = this;
        var DATA_INVENTORY_PANEL_KEY = 'org.talend.dataprep.data_inventory_panel_display';
        vm.$state = $state;

        /**
         * @ngdoc property
         * @name importType
         * @propertyOf data-prep.home.controller:HomeCtrl
         * @description List of supported import type.
         * @type {object[]}
         */
        vm.importTypes = [
            {id: 'local', name: 'Local file'},
            {id: 'http', name: 'from HTTP'},
            {id: 'hdfs', name: 'from HDFS'}
        ];

        /**
         * @ngdoc property
         * @name showRightPanel
         * @propertyOf data-prep.home.controller:HomeCtrl
         * @description Flag that control the right panel display
         * @type {boolean}
         */
        vm.showRightPanel = getRightPanelState();

        /**
         * @ngdoc property
         * @name showRightPanelIcon
         * @propertyOf data-prep.home.controller:HomeCtrl
         * @description data icon of the state of the right panel
         * @type {string}
         */
        vm.showRightPanelIcon = 'u';
        updateRightPanelIcon();

        /**
         * @ngdoc property
         * @name uploadingDatasets
         * @propertyOf data-prep.home.controller:HomeCtrl
         * @description The current uploading datasets
         * @type {object[]}
         */
        vm.uploadingDatasets = state.dataset.uploadingDatasets;

        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------Right panel------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name toggleRightPanel
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Toggle the right panel containing inventory data
         */
        vm.toggleRightPanel = function () {
            vm.showRightPanel = !vm.showRightPanel;

            saveRightPanelState();
            updateRightPanelIcon();
        };

        /**
         * @ngdoc method
         * @name updateRightPanelIcon
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Update the displayed icon to toggle right panel
         */
        function updateRightPanelIcon() {
            vm.showRightPanelIcon = vm.showRightPanel ? 't' : 'u';
        }

        /**
         * @ngdoc method
         * @name getRightPanelState
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Get the data inventory panel parameters from localStorage
         */
        function getRightPanelState() {
            var params = $window.localStorage.getItem(DATA_INVENTORY_PANEL_KEY);
            return params ? JSON.parse(params) : false;
        }

        /**
         * @ngdoc method
         * @name saveRightPanelState
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Save the data inventory panel parameters in localStorage
         */
        function saveRightPanelState() {
            $window.localStorage.setItem(DATA_INVENTORY_PANEL_KEY, JSON.stringify(vm.showRightPanel));
        }

        //--------------------------------------------------------------------------------------------------------------
        //-----------------------------------------------------Import---------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name startDefaultImport
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Start the default import process of a dataset.
         */
        vm.startDefaultImport = function () {
            var defaultExportType = _.find(vm.importTypes, 'id', 'local');
            vm.startImport(defaultExportType);
        };

        /**
         * @ngdoc method
         * @name startImport
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Start the import process of a dataset. Route the call to the right import method
         * (local or remote) depending on the import type user choice.
         */
        vm.startImport = function (importType) {
            switch (importType.id) {
                case 'local':
                    document.getElementById('datasetFile').click();
                    break;
                case 'http':
                    // show http dataset form
                    vm.datasetHttpModal = true;
                    break;
                case 'hdfs':
                    // show hdfs dataset form
                    vm.datasetHdfsModal = true;
                    break;
                default:
            }
        };

        /**
         * @ngdoc method
         * @name importHttpDataSet
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Import a remote http dataset.
         */
        vm.importHttpDataSet = function () {
            var importParameters = {
                type: 'http',
                name: vm.datasetName,
                url: vm.datasetUrl
            };

            var dataset = DatasetService.createDatasetInfo(null, importParameters.name);
            StateService.startUploadingDataset(dataset);

            DatasetService.import(importParameters)
                .then(function (event) {
                    DatasetService.getDatasetById(event.data).then(UploadWorkflowService.openDataset);
                })
                .catch(function () {
                    dataset.error = true;
                })
                .finally(function () {
                    StateService.finishUploadingDataset(dataset);
                });
        };

        /**
         * @ngdoc method
         * @name importHdfsDataSet
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Import a remote hdfs dataset.
         */
        vm.importHdfsDataSet = function () {
            var importParameters = {
                type: 'hdfs',
                name: vm.datasetName,
                url: vm.datasetUrl
            };

            var dataset = DatasetService.createDatasetInfo(null, importParameters.name);
            StateService.startUploadingDataset(dataset);

            DatasetService.import(importParameters)
                .then(function (event) {
                    DatasetService.getDatasetById(event.data).then(UploadWorkflowService.openDataset);
                })
                .catch(function () {
                    dataset.error = true;
                })
                .finally(function () {
                    StateService.finishUploadingDataset(dataset);
                });
        };

        /**
         * @ngdoc method
         * @name uploadDatasetFile
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Upload dataset : Step 1 - file selected. It takes the file name, and crop the extension.
         * If this new name already exists, we display the dataset name change modal.
         * Otherwise, we create the dataset with this name.
         */
        vm.uploadDatasetFile = function uploadDatasetFile() {
            var file = vm.datasetFile[0];

            // remove file extension and ask final name
            var name = file.name.replace(/\.[^/.]+$/, '');
            vm.datasetName = name;

            // show dataset name popup when name already exists
            if(DatasetService.getDatasetByNameAndFolder(name, state.folder.currentFolder)) {
                vm.datasetNameModal = true;
            }
            // create dataset with calculated name if it is unique
            else {
                vm.uploadDatasetName();
            }
        };

        /**
         * @ngdoc method
         * @name uploadDatasetName
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Upload dataset : Step 2 - name entered. It ask for override if a dataset with the same name
         * exists, and trigger the upload
         */
        vm.uploadDatasetName = function uploadDatasetName() {
            var file = vm.datasetFile[0];
            var name = vm.datasetName;

            // if the name exists, ask for update or creation
            vm.existingDatasetFromName = DatasetService.getDatasetByNameAndFolder(name, state.folder.currentFolder);
            console.log('uploadDatasetName:'+vm.existingDatasetFromName);
            if (vm.existingDatasetFromName) {
                TalendConfirmService.confirm(null, ['UPDATE_EXISTING_DATASET'], {dataset: vm.datasetName})
                    .then(
                    function () {
                        vm.updateExistingDataset();
                    },
                    function (cause) {
                        if (cause !== 'dismiss') {
                            vm.createDatasetFromExistingName();
                        }
                    }
                );
            }
            // create with requested name
            else {
                createDataset(file, name);
            }
        };

        /**
         * @ngdoc method
         * @name createDatasetFromExistingName
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Upload dataset : Step 3 - Create a new dataset with a unique name (add (n)).
         */
        vm.createDatasetFromExistingName = function () {
            var file = vm.datasetFile[0];
            var name = vm.datasetName;
            name = DatasetService.getUniqueName(name);
            createDataset(file, name);
        };

        /**
         * @ngdoc method
         * @name updateExistingDataset
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Upload dataset : Step 3 bis - Update existing dataset
         */
        vm.updateExistingDataset = function () {
            var file = vm.datasetFile[0];
            var existingDataset = vm.existingDatasetFromName;

            UpdateWorkflowService.updateDataset(file, existingDataset);
        };

        /**
         * @ngdoc method
         * @name createDataset
         * @methodOf data-prep.home.controller:HomeCtrl
         * @param {object} file - the file to upload
         * @param {string} name - the dataset name
         * @description [PRIVATE] Create a new dataset
         */
        var createDataset = function (file, name) {
            var dataset = DatasetService.createDatasetInfo(file, name);
            StateService.startUploadingDataset(dataset);

            DatasetService.create(dataset)
                .progress(function (event) {
                    dataset.progress = parseInt(100.0 * event.loaded / event.total);
                })
                .then(function (event) {
                    DatasetService.getDatasetById(event.data)
                        // once the dataset entry has been created we need to refresh/filter the dataset list
                        .then(FolderService.createFolderEntry('dataset',event.data,state.folder.currentFolder)
                            .then(function(){
                                if (state.folder.currentFolder){
                                    FolderService.getFolderContent(state.folder.currentFolder);
                                }
                            })
                        )
                        .then(UploadWorkflowService.openDataset);
                })
                .catch(function () {
                    dataset.error = true;
                })
                .finally(function () {
                    StateService.finishUploadingDataset(dataset);

                });
        };
    }

    angular.module('data-prep.home')
        .controller('HomeCtrl', HomeCtrl);
})();