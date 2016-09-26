/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class PreparationCreatorCtrl {
    constructor($document, $state, $translate, state, RestURLs,
                PreparationService, DatasetService, UploadWorkflowService) {
        'ngInject';

        this.$document = $document;
        this.$state = $state;
        this.state = state;
        this.preparationService = PreparationService;
        this.datasetService = DatasetService;
        this.uploadWorkflowService = UploadWorkflowService;
        this.restURLs = RestURLs;

        this.enteredFilterText = '';
        this.filteredDatasets = [];
        this.baseDataset = null;
        this.userHasTypedName = false;
        this.uploadingDatasets = [];
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
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
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
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description imports the chosen dataset
     */
    import() {
        const file = this.datasetFile[0];

        // remove file extension and ask final name
        let datasetName = file.name.replace(/\.[^/.]+$/, '');
        this.importDisabled = true;
        this.datasetService.checkNameAvailability(datasetName)
            .catch(() => {
                return this.datasetService.getUniqueName(datasetName)
                    .then((uniqueName) => {
                        datasetName = uniqueName;
                    });
            })
            .then(() => this._createDataset(file, datasetName))
            .finally(() => {
                this.importDisabled = false;
            });
    }

    /**
     * @ngdoc method
     * @name _createDataset
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description [PRIVATE] creates a dataset and manages the progress bar
     * @params {Object} file to create
     * @params {String} name of the dataset
     */
    _createDataset(file, name) {
        const params = {
            datasetFile: '',
            type: 'local',
            name,
        };

        const dataset = this.datasetService.createDatasetInfo(file, name);
        this.uploadingDatasets.push(dataset);

        return this.datasetService.create(params, 'text/plain', file)
            .progress((event) => {
                dataset.progress = parseInt(100.0 * event.loaded / event.total, 10);
            })
            .then((event) => {
                return this.datasetService.getDatasetById(event.data)
                    .then((dataset) => {
                        this.uploadingDatasets = [];
                        this.baseDataset = dataset;
                        if (!this.userHasTypedName) {
                            this._getUniquePrepName();
                        }

                        return this.createPreparation();
                    });
            })
            .catch(() => {
                dataset.error = true;
            });
    }

    /**
     * @ngdoc method
     * @name _getUniquePrepName
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description [PRIVATE] generates a unique preparation name
     * @params {Number} index the index to increment
     */
    _getUniquePrepName(index = 0) {
        const suffix = index === 0 ?
        ' ' + this.preparationSuffix :
        ' ' + this.preparationSuffix + ' (' + index + ')';
        this.enteredName = this.baseDataset.name + suffix;
        const existingName = _.some(
            this.state.inventory.folder.content.preparations,
            { name: this.enteredName }
        );
        if (existingName) {
            this._getUniquePrepName(index + 1);
        }
    }

    /**
     * @ngdoc method
     * @name createPreparation
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
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
            this.onCreation();
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
                .then(prepid => {
                    this.onCreation();
                    this.$state.go('playground.preparation', { prepid });
                });
        }
        return promise;
    }

    /**
     * @ngdoc method
     * @name checkExistingPrepName
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
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
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description generates a unique preparation name
     */
    applyNameFilter() {
        this.loadDatasets(this.selectedFilter);
    }

    /**
     * @ngdoc method
     * @name selectBaseDataset
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
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
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
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
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
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
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
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
