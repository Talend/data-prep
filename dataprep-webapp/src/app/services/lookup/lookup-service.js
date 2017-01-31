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
 * @name data-prep.services.lookup.service:LookupService
 * @description Lookup service. This service provide the entry point to load lookup content
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.transformations.service:TransformationRestService
 * @requires data-prep.services.datasets.service:DatasetRestService
 * @requires data-prep.services.utils.service:StorageService
 */
export default class LookupService {
	constructor($q, state, DatasetListService, StateService, TransformationRestService, DatasetRestService, StorageService) {
		'ngInject';

		this.$q = $q;
		this.state = state;
		this.DatasetListService = DatasetListService;
		this.StateService = StateService;
		this.TransformationRestService = TransformationRestService;
		this.DatasetRestService = DatasetRestService;
		this.StorageService = StorageService;

		this.loadFromAction = this.loadFromAction.bind(this);
	}

	/**
	 * @ngdoc method
	 * @name initLookups
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @description Load lookup panel content.
	 * We completely init the panel, so if the selected column has a lookup in the recipe, we load the last one as update.
	 * Otherwise, we init the the first lookup action as new lookup.
	 */
	initLookups() {
		return this._getDatasets()
			.then(() => {
				return this._getActions(this.state.playground.dataset.id)
					.then((lookupActions) => {
						if (!lookupActions.length) {
							return;
						}

						const step = this._getSelectedColumnLastLookup();
						if (step) {
							return this.loadFromStep(step);
						}
						else {
							return this.loadFromAction(lookupActions[0]);
						}
					});
			});
	}

	/**
	 * @ngdoc method
	 * @name loadFromAction
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @param {object} lookupAction The lookup action
	 * @description Loads the lookup dataset content.
	 * When the selected column already has a this lookup in recipe, we load it as update.
	 * Otherwise, load the lookup action as a new lookup
	 */
	loadFromAction(lookupAction) {
		const step = this._getSelectedColumnLookup(lookupAction);
		if (step) {
			return this.loadFromStep(step);
		}

		return this._getActions(this.state.playground.dataset.id)
			.then(() => {
				// lookup already loaded
				if (this.state.playground.lookup.dataset === lookupAction && !this.state.playground.lookup.step) {
					return;
				}

				// load content
				return this.DatasetRestService.getContent(this._getDsId(lookupAction), true)
					.then((lookupDsContent) => {
						this._initLookupState(lookupAction, lookupDsContent, undefined);
					});
			});
	}

	/**
	 * @ngdoc method
	 * @name loadFromStep
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @param {object} step The lookup step to load in update mode
	 * @description Loads the lookup dataset content from step in update mode
	 */
	loadFromStep(step) {
		return this._getActions(this.state.playground.dataset.id)
			.then((actions) => {
				const lookupId = step.actionParameters.parameters.lookup_ds_id;
				const lookupAction = _.find(actions, (action) => {
					return this._getDsId(action) === lookupId;
				});

				// change column selection to focus on step target
				const selectedColumn = _.find(this.state.playground.data.metadata.columns, { id: step.actionParameters.parameters.column_id });
				this.StateService.setGridSelection([selectedColumn]);

				// lookup already loaded
				if (this.state.playground.lookup.dataset === lookupAction &&
					this.state.playground.lookup.step === step) {
					return;
				}

				// load content
				return this._getActions(this.state.playground.dataset.id)
					.then(() => this.DatasetRestService.getContent(lookupId, true))
					.then((lookupDsContent) => {
						this._initLookupState(lookupAction, lookupDsContent, step);
					});
			});
	}

	/**
	 * @ngdoc method
	 * @name updateTargetColumn
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @description Update the loaded lookup.
	 * When the combination [lookup action, selected column] is a step in recipe, we load it as update.
	 * Otherwise, we load the lookup action as new lookup
	 */
	updateTargetColumn() {
		const lookupAction = this.state.playground.lookup.dataset;
		if (!this.state.playground.lookup.visibility || !lookupAction) {
			return;
		}

		return this.loadFromAction(lookupAction);
	}

	/**
	 * @ngdoc method
	 * @name updateLookupDatasets
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @description Update added datasets list
	 */
	updateLookupDatasets() {
		const actionsToAdd = _.chain(this.state.playground.lookup.datasets)
			.filter('addedToLookup') // filter addedToLookup = true
			.map((dataset) => { // map dataset to action
				return _.find(this.state.playground.lookup.actions, (action) => {
					return _.find(action.parameters, { name: 'lookup_ds_id' }).default === dataset.id;
				});
			})
			.filter(action => action) // remove falsy action (added dataset but no action with this dataset)
			.value();
		this.StateService.setLookupAddedActions(actionsToAdd);

		const datasetsIdsToSave = _.chain(this.state.playground.lookup.datasets)
			.filter('addedToLookup') // filter addedToLookup = true
			.map('id')
			.value();
		if (this.StorageService.getLookupDatasets().indexOf(this.state.playground.dataset.id) > -1) { // If the playground dataset have been saved in localStorage for the lookup
			datasetsIdsToSave.push(this.state.playground.dataset.id);
		}

		this.StorageService.setLookupDatasets(datasetsIdsToSave);
	}

	/**
	 * @ngdoc method
	 * @name disableDatasetsUsedInRecipe
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @description Disable datasets already used in a lookup step of the recipe to not to be removed
	 */
	disableDatasetsUsedInRecipe() {
		_.forEach(this.state.playground.lookup.datasets, (dataset) => {
			const lookupStep = _.find(this.state.playground.recipe.current.steps, (nextStep) => {
				return nextStep.actionParameters.action === 'lookup' &&
					dataset.id === nextStep.actionParameters.parameters.lookup_ds_id;
			});

			dataset.enableToAddToLookup = !lookupStep;
		});
	}

	//------------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------PRIVATE--------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name _getDatasets
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @description get datasets to be used for lookup
	 */
	_getDatasets() {
		if (this.state.inventory.datasets.content) {
			return this.$q.when(this.state.inventory.datasets.content);
		}
		return this.DatasetListService.refreshDatasets();
	}

	/**
	 * @ngdoc method
	 * @name _getActions
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @param {string} datasetId The dataset id
	 * @description Loads the possible lookup actions (1 action per dataset lookup)
	 */
	_getActions(datasetId) {
		if (this.state.playground.lookup.addedActions.length) {
			return this.$q.when(this.state.playground.lookup.addedActions);
		}
		else {
			return this.TransformationRestService.getDatasetTransformations(datasetId)
				.then((lookup) => {
					const actionsList = lookup.data;

					const datasetsToAdd = _.chain(actionsList)
						.map((action) => { // map action to dataset
							return this.state.inventory.datasets.content.find(dataset => dataset.id === this._getDsId(action));
						})
						.filter(dataset => dataset)
						.forEach((dataset) => {
							dataset.addedToLookup = false;
							dataset.enableToAddToLookup = true;
						})
						.value();

					this.StateService.setLookupDatasets(datasetsToAdd);
					this.StateService.setLookupActions(actionsList);

					this._initLookupDatasets();

					return this.state.playground.lookup.addedActions;
				});
		}
	}

	/**
	 * @ngdoc method
	 * @name _getDsId
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @param {object} lookup the lookup action
	 * @returns {String} The id of the lookup dataset
	 * @description Extract the dataset id from lookup action
	 */
	_getDsId(lookup) {
		return _.find(lookup.parameters, { name: 'lookup_ds_id' }).default;
	}

	/**
	 * @ngdoc method
	 * @name _initLookupState
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @param {object} lookupAction The lookup action
	 * @param {object} lookupDsContent The lookup dataset content (columns, records, ...)
	 * @param {object} step The step that the lookup is updating (falsy means no update but add mode)
	 * @description Set the lookup state
	 */
	_initLookupState(lookupAction, lookupDsContent, step) {
		if (step) {
			this.StateService.setLookupUpdateMode(lookupAction, lookupDsContent, step);
		}
		else {
			this.StateService.setLookupAddMode(lookupAction, lookupDsContent);
		}
	}

	/**
	 * @ngdoc method
	 * @name _getSelectedColumnLastLookup
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @description Fetch the last step in recipe that is a lookup action for the selected column
	 */
	_getSelectedColumnLastLookup() {
		const selectedColumn = this.state.playground.grid.selectedColumns.length ? this.state.playground.grid.selectedColumns[0] : null;
		return selectedColumn &&
			_.findLast(this.state.playground.recipe.current.steps, (nextStep) => {
				return nextStep.column.id === selectedColumn.id && nextStep.transformation.name === 'lookup';
			});
	}

	/**
	 * @ngdoc method
	 * @name _getSelectedColumnLookup
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @param {object} lookupAction The lookup action to seek
	 * @description Fetch the step in recipe that is a lookup action on a specific dataset for the selected column
	 */
	_getSelectedColumnLookup(lookupAction) {
		const datasetId = this._getDsId(lookupAction);
		const selectedColumn = this.state.playground.grid.selectedColumns.length ? this.state.playground.grid.selectedColumns[0] : null;
		return selectedColumn &&
			_.findLast(this.state.playground.recipe.current.steps, (nextStep) => {
				return nextStep.column.id === selectedColumn.id &&
					nextStep.transformation.name === 'lookup' &&
					nextStep.actionParameters.parameters.lookup_ds_id === datasetId;
			});
	}

	/**
	 * @ngdoc method
	 * @name _initLookupDatasets
	 * @methodOf data-prep.services.lookup.service:LookupService
	 * @description init added datasets list which saved in localStorage
	 */
	_initLookupDatasets() {
		const addedDatasets = this.StorageService.getLookupDatasets();

		// Consolidate addedDatasets: if lookup datasets of a step are not save in localStorage, we add them
		_.chain(this.state.playground.recipe.current.steps)
			.filter(step => step.actionParameters.action === 'lookup')
			.forEach((step) => {
				if (addedDatasets.indexOf(step.actionParameters.parameters.lookup_ds_id) === -1) {
					addedDatasets.push(step.actionParameters.parameters.lookup_ds_id);
				}
			})
			.value();
		this.StorageService.setLookupDatasets(addedDatasets);

		// Add addedToLookup flag
		_.chain(addedDatasets)
			.map((datasetId) => { // map datasetId to dataset
				return _.find(this.state.playground.lookup.datasets, { id: datasetId });
			})
			.filter(dataset => dataset) // remove falsy dataset
			.forEach((dataset) => {
				dataset.addedToLookup = true;
			})
			.value();

		// Get actions
		const actionsToAdd = _.chain(addedDatasets)
			.map((datasetId) => { // map dataset to action
				return _.find(this.state.playground.lookup.actions, (action) => {
					return _.find(action.parameters, { name: 'lookup_ds_id' }).default === datasetId;
				});
			})
			.filter(action => action) // remove falsy action
			.value();
		this.StateService.setLookupAddedActions(actionsToAdd);
	}
}
