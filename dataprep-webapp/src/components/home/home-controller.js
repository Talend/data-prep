(function() {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.home.controller:HomeCtrl
     * @description Home controller.
     * @requires data-prep.services.utils.service:MessageService
     * @requires data-prep.services.dataset.service:DatasetService
     * @requires talend.widget.service:TalendConfirmService
     * @requires data-prep.services.uploadWorkflowService.service:UploadWorkflowService
     */
    function HomeCtrl(UploadWorkflowService, MessageService, DatasetService, TalendConfirmService) {
        var vm = this;

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
            {id: 'hdfs', name: 'from HDFS (Comming soon...)'},
            {id: 'jdbc', name: 'from JDBC (Comming soon...)'}
        ];

        /**
         * @ngdoc property
         * @name showRightPanel
         * @propertyOf data-prep.home.controller:HomeCtrl
         * @description Flag that control the right panel display
         * @type {boolean}
         */
        vm.showRightPanel = true;

        /**
         * @ngdoc property
         * @name uploadingDatasets
         * @propertyOf data-prep.home.controller:HomeCtrl
         * @description The current uploading datasets
         * @type {object[]}
         */
        vm.uploadingDatasets = [];

        /**
         * @ngdoc method
         * @name toggleRightPanel
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Toggle the right panel containing inventory data
         */
        vm.toggleRightPanel = function() {
            vm.showRightPanel = !vm.showRightPanel;
        };

        /**
         * @ngdoc method
         * @name startDefaultImport
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Start the default import process of a dataset.
         */
        vm.startDefaultImport = function() {
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
        vm.startImport = function(importType) {
            switch(importType.id) {
                case 'local':
                    document.getElementById('datasetFile').click();
                    break;
                case 'http':
                    // show http dataset form
                    vm.datasetHttpModal = true;
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
        vm.importHttpDataSet = function() {
            var importParameters = {
                type: 'http',
                name: vm.datasetName,
                url: vm.datasetUrl
            };

            var dataset = DatasetService.createDatasetInfo(null, importParameters.name);
            vm.uploadingDatasets.push(dataset);

            DatasetService.import(importParameters)
                .then(function(event) {
                    vm.uploadingDatasets.splice(vm.uploadingDatasets.indexOf(dataset, 1));
                    DatasetService.getDatasetById(event.data).then(UploadWorkflowService.openDataset);
                })
                .catch(function() {
                    dataset.error = true;
                    MessageService.error('IMPORT_ERROR_TITLE', 'IMPORT_ERROR');
                });
        };

        /**
         * @ngdoc method
         * @name uploadDatasetFile
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Upload dataset : Step 1 - file selected. It takes the file name, and display the dataset name
         * change modal
         */
        vm.uploadDatasetFile = function() {
            var file = vm.datasetFile[0];

            // remove file extension and ask final name
            var name = file.name.replace(/\.[^/.]+$/, '');
            vm.datasetName = name;

            // show dataset name popup
            vm.datasetNameModal = true;
        };

        /**
         * @ngdoc method
         * @name uploadDatasetName
         * @methodOf data-prep.home.controller:HomeCtrl
         * @description Upload dataset : Step 2 - name entered. It ask for override if a dataset with the same name
         * exists, and trigger the upload
         */
        vm.uploadDatasetName = function() {
            var file = vm.datasetFile[0];
            var name = vm.datasetName;

            // if the name exists, ask for update or creation
            vm.existingDatasetFromName = DatasetService.getDatasetByName(name);
            if(vm.existingDatasetFromName) {
                TalendConfirmService.confirm(null, ['UPDATE_EXISTING_DATASET'], {dataset: vm.datasetName})
                    .then(
                        function() {
                            vm.updateExistingDataset();
                        },
                        function(cause) {
                            if(cause !== 'dismiss') {
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
        vm.createDatasetFromExistingName = function() {
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
        vm.updateExistingDataset = function() {
            var file = vm.datasetFile[0];
            var existingDataset = vm.existingDatasetFromName;

            updateDataset(file, existingDataset);
        };

        /**
         * @ngdoc method
         * @name createDataset
         * @methodOf data-prep.home.controller:HomeCtrl
         * @param {object} file - the file to upload
         * @param {string} name - the dataset name
         * @description [PRIVATE] Create a new dataset
         */
        var createDataset = function(file, name) {
            var dataset = DatasetService.createDatasetInfo(file, name);
            vm.uploadingDatasets.push(dataset);

            DatasetService.create(dataset)
                .progress(function(event) {
                    dataset.progress = parseInt(100.0 * event.loaded / event.total);
                })
                .then(function(event) {
                    vm.uploadingDatasets.splice(vm.uploadingDatasets.indexOf(dataset, 1));
                    DatasetService.getDatasetById(event.data).then(UploadWorkflowService.openDataset);
                })
                .catch(function() {
                    dataset.error = true;
                    MessageService.error('UPLOAD_ERROR_TITLE', 'UPLOAD_ERROR');
                });
        };

        /**
         * @ngdoc method
         * @name updateDataset
         * @methodOf data-prep.home.controller:HomeCtrl
         * @param {object} file - the file to upload
         * @param {object} existingDataset - the existing dataset
         * @description [PRIVATE] Update existing dataset
         */
        var updateDataset = function(file, existingDataset) {
            var dataset = DatasetService.createDatasetInfo(file, existingDataset.name, existingDataset.id);
            vm.uploadingDatasets.push(dataset);

            DatasetService.update(dataset)
                .progress(function(event) {
                    dataset.progress = parseInt(100.0 * event.loaded / event.total);
                })
                .then(function() {
                    vm.uploadingDatasets.splice(vm.uploadingDatasets.indexOf(dataset, 1));
                    MessageService.success('DATASET_UPDATE_SUCCESS_TITLE', 'DATASET_UPDATE_SUCCESS', {dataset: dataset.name});
                })
                .catch(function() {
                    dataset.error = true;
                    MessageService.error('UPLOAD_ERROR_TITLE', 'UPLOAD_ERROR');
                });
        };

    }

    angular.module('data-prep.home')
        .controller('HomeCtrl', HomeCtrl);
})();