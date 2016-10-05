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
 * @requires talend.widget.service:TalendConfirmService
 */
export default function ImportCtrl($document,
                                   state, StateService,
                                   UploadWorkflowService, UpdateWorkflowService,
                                   DatasetService,
                                   TalendConfirmService,
                                   ImportRestService) {
	'ngInject';
	const vm = this;
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

	/**
	 * @ngdoc property
	 * @name showModal
	 * @propertyOf data-prep.import.controller:ImportCtrl
	 * @description Display/Hide the import parameters modal
	 * @type {boolean}
	 */
	vm.isFetchingParameters = false;

	// --------------------------------------------------------------------------------------------
	// ---------------------------------------------Import-----------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name startDefaultImport
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Start the default import process of a dataset.
	 */
	vm.startDefaultImport = () => {
		const defaultExportType = _.find(vm.importTypes, 'defaultImport', true) || vm.importTypes[0];
		vm.startImport(defaultExportType);
	};

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
			if (vm.currentInputType.dynamic) {
				vm.isFetchingParameters = true;
				ImportRestService.importParameters(vm.currentInputType.locationType)
					.then((response) => {
						vm.currentInputType.parameters = response.data;
					})
					.finally(() => {
						vm.isFetchingParameters = false;
					});
			}

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
		const params = DatasetService.getLocationParamIteration({}, importType.parameters);
		params.type = importType.locationType;
		params.name = name;

		const dataset = DatasetService.createDatasetInfo(file, name);
		StateService.startUploadingDataset(dataset);

		return DatasetService.create(params, importType.contentType, file)
			.progress((event) => {
				dataset.progress = parseInt((100.0 * event.loaded) / event.total, 10);
			})
			.then((event) => {
				DatasetService.getDatasetById(event.data).then(UploadWorkflowService.openDataset);
			})
			.catch(() => {
				dataset.error = true;
			})
			.finally(() => {
				StateService.finishUploadingDataset(dataset);
			});
	}

	/**
	 * @ngdoc method
	 * @name import
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Import step 1 - It checks if the dataset name is available
	 * If so : the dataset is created
	 * If not : the new name modal is shown
	 */
	vm.import = (importType) => {
		const file = vm.datasetFile ? vm.datasetFile[0] : null;
		const datasetName = file ?
			file.name :
			_.find(importType.parameters, { name: 'name' }).value;

		// remove file extension and ask final name
		const name = datasetName.replace(/\.[^/.]+$/, '');

		return DatasetService.checkNameAvailability(name)
		// name available: we create the dataset
			.then(() => {
				createDataset(file, name, importType);
			})
			// name is not available, we ask for a new name
			.catch(() => {
				vm.datasetName = name;
				vm.datasetNameModal = true;
			})
			.finally(() => {
				vm.showModal = false;
			});
	};

	/**
	 * @ngdoc method
	 * @name uploadDatasetName
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Import step 2 - name entered. It checks if the name is available
	 * If so : the dataset is created
	 * If not : the user has to choose to create a new one or the update the existing one
	 */
	vm.onImportNameValidation = () => {
		const file = vm.datasetFile ? vm.datasetFile[0] : null;
		const importType = vm.currentInputType;
		const name = vm.datasetName;

		return DatasetService.checkNameAvailability(name)
		// name still exists
			.then(() => {
				createDataset(file, name, importType);
			})
			// name still exists : we ask if user want to update it
			.catch(existingDataset => updateOrCreate(file, existingDataset, importType, name));
	};

	/**
	 * @ngdoc method
	 * @name updateOrCreate
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @param {object} file The dataset file
	 * @param {object} existingDataset The dataset to update
	 * @param {object} importType The import configuration
	 * @param {string} name The dataset name
	 * @description Import step 3 - Ask to create or update the existing dataset
	 * Create : get a unique name and create
	 * Update : update the content of the existing dataset
	 */
	function updateOrCreate(file, existingDataset, importType, name) {
		return TalendConfirmService.confirm(null, ['UPDATE_EXISTING_DATASET'], { dataset: name })
		// user confirm : let's update the dataset
			.then(() => {
				UpdateWorkflowService.updateDataset(file, existingDataset);
			})
			// user dismiss : cancel
			// user select no : get unique name and create a new dataset
			.catch((cause) => {
				if (cause === 'dismiss') {
					return;
				}

				return DatasetService.getUniqueName(name)
					.then((name) => {
						return createDataset(
							file,
							name,
							importType
						);
					});
			});
	}
}
