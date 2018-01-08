/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import { PLAYGROUND_PREPARATION_ROUTE } from '../../../../index-route';

export default class PreparationCreatorFormCtrl {
	constructor($document, $state, $translate, state, StateService,
				PreparationService, DatasetService, UploadWorkflowService, ImportService) {
		'ngInject';

		this.$document = $document;
		this.$state = $state;
		this.state = state;
		this.stateService = StateService;
		this.preparationService = PreparationService;
		this.datasetService = DatasetService;
		this.uploadWorkflowService = UploadWorkflowService;
		this.importService = ImportService;

		this.enteredFilterText = '';
		this.filteredDatasets = [];
		this.baseDataset = null;
		this.userHasTypedName = false;
		this.importDisabled = false;
		this.isFetchingDatasets = false;
		this.preparationSuffix = $translate.instant('PREPARATION');
	}

	$onInit() {
		this.selectedFilter = this.datasetService.filters[0];
		this.loadDatasets(this.selectedFilter);
	}

	/**
	 * @ngdoc method
	 * @name loadDatasets
	 * @methodOf data-prep.preparation-creator.controller:PreparationCreatorFormCtrl
	 * @description loads the filtered datasets
	 * @params {Object} filter the chosen filter
	 */
	loadDatasets(filter) {
		this.selectedFilter = filter;

		this.isFetchingDatasets = true;
		this.datasetService.getFilteredDatasets(filter, this.enteredFilterText)
			.then((filteredDatasets) => {
				this.filteredDatasets = filteredDatasets;
			})
			.finally(() => {
				this.isFetchingDatasets = false;
			});
	}

	/**
	 * @ngdoc method
	 * @name import
	 * @methodOf data-prep.preparation-creator.controller:PreparationCreatorFormCtrl
	 * @description imports the chosen dataset
	 */
	import() {
		const defaultImport = {
			locationType: 'local',
			contentType: 'text/plain',
			parameters: [
				{
					name: 'datasetFile',
					type: 'file',
					implicit: false,
					canBeBlank: false,
					placeHolder: '*.csv',
					default: null,
					description: 'File',
					label: 'File',
				},
			],
		};

		this.importService.importDatasetFile = this.datasetFile;
		this.importService.currentInputType = defaultImport;

		const action = (dataset) => {
			this.baseDataset = dataset;
			if (!this.userHasTypedName) {
				this._getUniquePrepName();
			}
			return this.createPreparation();
		};

		this.importDisabled = true;
		this.importService.import(defaultImport, action)
			.finally(() => this.importDisabled = false);
	}


	/**
	 * @ngdoc method
	 * @name _getUniquePrepName
	 * @methodOf data-prep.preparation-creator.controller:PreparationCreatorFormCtrl
	 * @description [PRIVATE] generates a unique preparation name
	 * @params {Number} index the index to increment
	 */
	_getUniquePrepName(index = 0) {
		const suffix = index === 0 ?
		` ${this.preparationSuffix}` :
		` ${this.preparationSuffix} (${index})`;
		this.enteredName = this.baseDataset.name + suffix;

		if (this.state.inventory.folder.content.preparations.find(prep => prep.name === this.enteredName)) {
			this._getUniquePrepName(index + 1);
		}
	}

	/**
	 * @ngdoc method
	 * @name createPreparation
	 * @methodOf data-prep.preparation-creator.controller:PreparationCreatorFormCtrl
	 * @description created the preparation
	 */
	createPreparation() {
		const configuration = {
			dataset: {
				metadata: this.baseDataset,
				draft: this.baseDataset.draft,
			},
			preparation: {
				name: this.enteredName,
				folder: this.state.inventory.folder.metadata.id,
			},
		};

		let promise;
		if (configuration.dataset.draft) {
			this.stateService.togglePreparationCreator();
			promise = this.uploadWorkflowService.openDraft(
				configuration.dataset.metadata,
				true,
				configuration.preparation.name
			);
		}
		else {
			this.addPreparationForm.$commitViewValue();
			promise = this.preparationService
				.create(
					configuration.dataset.metadata.id,
					configuration.preparation.name,
					configuration.preparation.folder
				)
				.then((prepid) => {
					this.stateService.togglePreparationCreator();
					this.$state.go(PLAYGROUND_PREPARATION_ROUTE, { prepid });
				});
		}
		return promise;
	}

	/**
	 * @ngdoc method
	 * @name checkExistingPrepName
	 * @methodOf data-prep.preparation-creator.controller:PreparationCreatorFormCtrl
	 * @description creates a dataset and manages the progress bar
	 * @params {String} from the caller
	 */
	checkExistingPrepName(from) {
		if (from === 'user') {
			this.userHasTypedName = true;
		}

		this.alreadyExistingName = _.some(
			this.state.inventory.folder.content.preparations,
			{ name: this.enteredName }
		);
	}

	/**
	 * @ngdoc method
	 * @name applyNameFilter
	 * @methodOf data-prep.preparation-creator.controller:PreparationCreatorFormCtrl
	 * @description generates a unique preparation name
	 */
	applyNameFilter() {
		this.loadDatasets(this.selectedFilter);
	}

	/**
	 * @ngdoc method
	 * @name selectBaseDataset
	 * @methodOf data-prep.preparation-creator.controller:PreparationCreatorFormCtrl
	 * @description selects the base dataset to be used in the preparation
	 * @params {Object} dataset the base dataset
	 */
	selectBaseDataset(dataset) {
		if (this.lastSelectedDataset) {
			this.lastSelectedDataset.isSelected = false;
		}

		this.lastSelectedDataset = dataset;
		dataset.isSelected = true;
		this.baseDataset = dataset;
		if (!this.userHasTypedName) {
			this._getUniquePrepName();
		}
	}

	/**
	 * @ngdoc method
	 * @name anyMissingEntries
	 * @methodOf data-prep.preparation-creator.controller:PreparationCreatorFormCtrl
	 * @description checks if there is a unique preparation name
	 * and there is a selected base dataset
	 * @returns boolean
	 */
	anyMissingEntries() {
		return !this.enteredName || !this.baseDataset || this.alreadyExistingName;
	}

	/**
	 * @ngdoc method
	 * @name importFile
	 * @methodOf data-prep.preparation-creator.controller:PreparationCreatorFormCtrl
	 * @description triggers the click on the upload input
	 */
	importFile() {
		if (!this.importDisabled && !this.alreadyExistingName) {
			this.$document.find('#localFileImport').eq(0).click();
		}
	}

	/**
	 * @ngdoc method
	 * @name getImportTitle
	 * @methodOf data-prep.preparation-creator.controller:PreparationCreatorFormCtrl
	 * @description creates the tooltip content
	 * @returns {String} the tooltip content
	 */
	getImportTitle() {
		if (this.importDisabled) {
			return 'IMPORT_IN_PROGRESS';
		}

		if (this.alreadyExistingName) {
			return 'TRY_CHANGING_NAME';
		}

		return 'IMPORT_FILE_DESCRIPTION';
	}
}
