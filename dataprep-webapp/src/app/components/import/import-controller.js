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
 * @name data-prep.import.controller:ImportCtrl
 * @description Import controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.datasetWorkflowService.service:UploadWorkflowService
 * @requires data-prep.services.datasetWorkflowService.service:UpdateWorkflowService
 * @requires data-prep.services.dataset.service:DatasetService
 * @requires data-prep.services.folder.service:FolderService
 * @requires talend.widget.service:TalendConfirmService
 */
export default function ImportCtrl($document,
                                   state, StateService,
                                   UploadWorkflowService, UpdateWorkflowService,
                                   DatasetService, FolderService,
                                   TalendConfirmService) {
    'ngInject';
    var vm = this;
    vm.state = state;

    /**
     * @ngdoc property
     * @name importType
     * @propertyOf data-prep.import.controller:ImportCtrl
     * @description List of supported import type.
     * @type {object[]}
     */
    vm.importTypes = vm.state.import.importTypes;

    /**
     * @ngdoc property
     * @name showModal
     * @propertyOf data-prep.import.controller:ImportCtrl
     * @description Display/Hide the import parameters modal
     * @type {boolean}
     */
    vm.showModal = false;

    //--------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------Import---------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name startDefaultImport
     * @methodOf data-prep.import.controller:ImportCtrl
     * @description Start the default import process of a dataset.
     */
    vm.startDefaultImport = () => {
        let defaultExportType = _.find(vm.importTypes, 'defaultImport', true);
        vm.startImport(defaultExportType? defaultExportType : vm.importTypes[0]);
    };

    /**
     * @ngdoc method
     * @name getParamIteration
     * @methodOf data-prep.import.controller:ImportCtrl
     * @description [PRIVATE] Inner function for recursively gather params
     * @param {object} paramsAccu The parameters values accumulator
     * @param {array} parameters The parameters array
     * @returns {object} The parameters
     */
    function getParamIteration(paramsAccu, parameters) {
        if (parameters) {
            _.forEach(parameters, (paramItem) => {
                paramsAccu[paramItem.name] = typeof (paramItem.value) !== 'undefined' ? paramItem.value : paramItem.default;

                // deal with select inline parameters
                if (paramItem.type === 'select') {
                    let selectedValue = _.find(paramItem.configuration.values, {value: paramItem.value});
                    getParamIteration(paramsAccu, selectedValue.parameters);
                }
            });
        }
        return paramsAccu;
    }

    /**
     * @ngdoc method
     * @name startImport
     * @methodOf data-prep.import.controller:ImportCtrl
     * @description Start the import process of a dataset. Route the call to the right import method
     * (local or remote) depending on the import type user choice.
     */
    vm.startImport = (importType) => {
        vm.currentInputType = importType;
        switch (importType.locationType) {
            case 'local':
                $document.find('#datasetFile').eq(0).click();
                break;
            default:
                vm.showModal = true;
        }
    };


    /**
     * @ngdoc method
     * @name createDataset
     * @description Create dataset using import parameters
     * @param {object} file The file imported from local
     * @param {string} name The dataset name
     * @param {object} importType The import parameters
     */
    function createDataset(file, name, importType) {

        let params = getParamIteration({}, importType.parameters);
        params.type = importType.locationType;
        params.name = name;

        let dataset = DatasetService.createDatasetInfo(file, params.name);
        StateService.startUploadingDataset(dataset);

        DatasetService.create(state.inventory.currentFolder, params, importType.contentType, file)
            .progress((event) => {
                dataset.progress = parseInt(100.0 * event.loaded / event.total);
            })
            .then((event) => {
                DatasetService.getDatasetById(event.data).then(UploadWorkflowService.openDataset);
                FolderService.getContent(state.inventory.currentFolder);
            })
            .catch(() => {
                dataset.error = true;
            })
            .finally(() => {
                StateService.finishUploadingDataset(dataset);
                vm.datasetName = '';
                vm.datasetFile = null;
            });
    }

    /**
     * @ngdoc method
     * @name import
     * @methodOf data-prep.import.controller:ImportCtrl
     * @description Check if dataset exist already and create dataset
     */
    vm.import = (importType) => {
        let file = vm.datasetFile ? vm.datasetFile[0] : null;
        let datasetName = (file ? vm.datasetFile[0].name : _.find(importType.parameters, (o) => { return o.name ==='name'; }).value);

        // remove file extension and ask final name
        vm.datasetName = datasetName.replace(/\.[^/.]+$/, '');

        // show dataset name popup when name already exists
        if (DatasetService.getDatasetByName(vm.datasetName)) {
            vm.datasetNameModal = true;
        }
        // create dataset with calculated name if it is unique
        else {
            createDataset(file, vm.datasetName, vm.currentInputType);
        }

    };

    /**
     * @ngdoc method
     * @name uploadDatasetName
     * @methodOf data-prep.import.controller:ImportCtrl
     * @description Upload dataset : Step 2 - name entered. It ask for override if a dataset with the same name
     * exists, and trigger the upload
     */
    vm.uploadDatasetName = () => {
        // if the name exists, ask for update or creation
        vm.existingDatasetFromName = DatasetService.getDatasetByName(vm.datasetName);
        if (vm.existingDatasetFromName) {
            TalendConfirmService.confirm(null, ['UPDATE_EXISTING_DATASET'], {dataset: vm.datasetName})
                .then(vm.updateExistingDataset)
                .catch((cause) => {
                    if (cause !== 'dismiss') {
                        createDataset(vm.datasetFile ? vm.datasetFile[0] : null, DatasetService.getUniqueName(vm.datasetName), vm.currentInputType);
                    }
                });
        }
        // create with requested name
        else {
            createDataset(vm.datasetFile ? vm.datasetFile[0] : null, vm.datasetName, vm.currentInputType);
        }
    };

    /**
     * @ngdoc method
     * @name updateExistingDataset
     * @methodOf data-prep.import.controller:ImportCtrl
     * @description Upload dataset : Step 3 bis - Update existing dataset
     */
    vm.updateExistingDataset = () => {
        let file = vm.datasetFile ? vm.datasetFile[0] : null;
        let existingDataset = vm.existingDatasetFromName;

        UpdateWorkflowService.updateDataset(file, existingDataset)
            .finally(() => {
                vm.datasetName = '';
                vm.datasetFile = null;
            });
    };

}