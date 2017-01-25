/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { HOME_DATASETS_ROUTE, HOME_PREPARATIONS_ROUTE } from '../../index-route';

/**
 * @ngdoc controller
 * @name data-prep.playground.controller:PlaygroundCtrl
 * @description Playground controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.preparation.service:PreparationService
 * @requires data-prep.services.playground.service:PreviewService
 * @requires data-prep.services.onboarding.service:OnboardingService
 * @requires data-prep.services.lookup.service:LookupService
 * @requires data-prep.services.utils.service:MessageService
 */
export default function PlaygroundCtrl($timeout, $state, $stateParams, state, StateService,
                                       PlaygroundService, DatasetService, PreparationService,
                                       PreviewService, FilterManagerService,
                                       OnboardingService, LookupService, MessageService) {
	'ngInject';

	const vm = this;
	vm.$stateParams = $stateParams;
	vm.state = state;

	vm.openFeedbackForm = () => StateService.showFeedback();
	vm.toggleParameters = () => StateService.toggleDatasetParameters();
	vm.previewInProgress = () => PreviewService.previewInProgress();
	vm.startOnBoarding = tourId => OnboardingService.startTour(tourId);
	vm.fetchCompatiblePreparations = datasetId => DatasetService.getCompatiblePreparations(datasetId);
	vm.removeAllFilters = () => FilterManagerService.removeAllFilters();

	/**
	 * @ngdoc property
	 * @name showNameValidation
	 * @propertyOf data-prep.playground.controller:PlaygroundCtrl
	 * @description Flag that controls the display of the save/discard window on implicit preparation close.
	 */
	vm.showNameValidation = false;

	/**
	 * @ngdoc property
	 * @name displayPreprationPicker
	 * @propertyOf data-prep.playground.controller:PlaygroundCtrl
	 * @description Flag that controls the display of preparation picker form.
	 */
	vm.displayPreprationPicker = false;

	//--------------------------------------------------------------------------------------------------------------
	// --------------------------------------------------PREPARATION PICKER------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name showPreparationPicker
	 * @methodOf data-prep.playground.controller:PlaygroundCtrl
	 * @description Toggle preparation picker modal
	 */
	vm.showPreparationPicker = function showPreparationPicker() {
		vm.displayPreparationPicker = true;
	};

	/**
	 * @ngdoc method
	 * @name applySteps
	 * @param {string} preparationId The preparation to apply
	 * @methodOf data-prep.playground.controller:PlaygroundCtrl
	 * @description Apply the preparation steps to the current preparation
	 */
	vm.applySteps = function applySteps(preparationId) {
		return PlaygroundService.copySteps(preparationId)
			.then(() => {
				vm.displayPreparationPicker = false;
			});
	};

	//--------------------------------------------------------------------------------------------------------------
	// --------------------------------------------------RECIPE HEADER-----------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name confirmPrepNameEdition
	 * @methodOf data-prep.playground.controller:PlaygroundCtrl
	 * @description Change the preparation name
	 */
	vm.confirmPrepNameEdition = function confirmPrepNameEdition(name) {
		const cleanName = name.trim();
		if (!vm.changeNameInProgress && cleanName) {
			changeName(cleanName)
				.then(preparation => $state.go('playground.preparation', { prepid: preparation.id }));
		}
	};

	/**
	 * @ngdoc method
	 * @name changeName
	 * @methodOf data-prep.playground.controller:PlaygroundCtrl
	 * @description [PRIVATE] Create a preparation or update existing preparation name if it already exists
	 * @param {string} name The preparation name
	 * @returns {Promise} The create/update promise
	 */
	function changeName(name) {
		vm.changeNameInProgress = true;
		return PlaygroundService.createOrUpdatePreparation(name)
			.finally(() => vm.changeNameInProgress = false);
	}

	//--------------------------------------------------------------------------------------------------------------
	// ------------------------------------------------------LOOKUP--------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name toggleLookup
	 * @methodOf data-prep.playground.controller:PlaygroundCtrl
	 * @description show hides lookup panel and populates its grid
	 */
	vm.toggleLookup = function toggleLookup() {
		if (state.playground.lookup.visibility) {
			StateService.setLookupVisibility(false);
		}
		else {
			LookupService.initLookups()
				.then(StateService.setLookupVisibility.bind(null, true));
		}
	};

	//--------------------------------------------------------------------------------------------------------------
	// ------------------------------------------------------CLOSE---------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name beforeClose
	 * @methodOf data-prep.playground.controller:PlaygroundCtrl
	 * @description When the preparation is an implicit preparation, we show the save/discard modal and block the
	 *     playground close.
	 * @returns {boolean} True if the playground can be closed (no implicit preparation), False otherwise
	 */
	vm.beforeClose = function beforeClose() {
		const isDraft = state.playground.preparation && state.playground.preparation.draft;
		if (isDraft) {
			if (state.playground.recipe.current.steps.length) {
				vm.showNameValidation = true;
			}
			else {
				vm.discardSaveOnClose();
			}
		}
		else {
			vm.close();
		}
	};

	/**
	 * @ngdoc method
	 * @name discardSaveOnClose
	 * @methodOf data-prep.playground.controller:PlaygroundCtrl
	 * @description Discard implicit preparation save. This trigger a preparation delete.
	 */
	vm.discardSaveOnClose = function discardSaveOnClose() {
		PreparationService.delete(state.playground.preparation).then(vm.close);
	};

	/**
	 * @ngdoc method
	 * @name confirmSaveOnClose
	 * @methodOf data-prep.playground.controller:PlaygroundCtrl
	 * @description Save implicit preparation with provided name. The playground is then closed.
	 */
	vm.confirmSaveOnClose = function confirmSaveOnClose() {
		vm.saveInProgress = true;
		StateService.setIsSavingPreparation(true);
		let operation;

		const prepId = state.playground.preparation.id;
		const destinationId = vm.destinationFolder.id;
		const cleanName = vm.state.playground.preparationName.trim();
		if (destinationId !== state.inventory.homeFolderId) {
			operation = PreparationService.move(prepId, state.inventory.homeFolderId, destinationId, cleanName);
		}
		else {
			operation = PreparationService.setName(prepId, cleanName);
		}

		return operation
			.then(() => {
				vm.close();
			})
			.finally(() => {
				StateService.setIsSavingPreparation(false);
			});
	};

	/**
	 * @ngdoc method
	 * @name close
	 * @methodOf data-prep.playground.controller:PlaygroundCtrl
	 * @description Playground close callback. It reset the playground and redirect to the previous page
	 */
	vm.close = function close() {
		$timeout.cancel(vm.fetchStatsTimeout);
		$timeout(StateService.resetPlayground, 500, false);
		$state.go(state.route.previous, state.route.previousOptions);
	};

	//--------------------------------------------------------------------------------------------------------------
	// ------------------------------------------DATASET PARAMS------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name changeDatasetParameters
	 * @methodOf data-prep.playground.controller:PlaygroundCtrl
	 * @description Change the dataset parameters
	 * @param {object} parameters The new dataset parameters
	 */
	vm.changeDatasetParameters = function changeDatasetParameters(parameters) {
		StateService.setIsSendingDatasetParameters(true);
		PlaygroundService.changeDatasetParameters(parameters)
			.then(StateService.hideDatasetParameters)
			.finally(StateService.setIsSendingDatasetParameters.bind(null, false));
	};

	//------------------------------------------------------------------------------------------------------
	// ----------------------------------------------STATS REFRESH-------------------------------------------
	//------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name shouldFetchStatistics
	 * @methodOf data-prep.playground.controller:PlaygroundCtrl
	 * @description Check if we have the statistics or we have to fetch them
	 */
	function shouldFetchStatistics() {
		const columns = state.playground.data.metadata.columns;

		return !columns || !columns.length ||                   // no columns
			!columns[0].statistics.frequencyTable.length;   // no frequency table implies no async stats computed
	}

	/**
	 * @ngdoc method
	 * @name fetchStatistics
	 * @methodOf data-prep.playground.controller:PlaygroundCtrl
	 * @description Fetch the statistics. If the update fails (no statistics yet) a retry is triggered after 1s
	 */
	function fetchStatistics() {
		StateService.setIsFetchingStats(true);
		PlaygroundService.updateStatistics()
			.then(() => StateService.setIsFetchingStats(false))
			.catch(() => {
				vm.fetchStatsTimeout = $timeout(fetchStatistics, 1500, false);
			});
	}

	//--------------------------------------------------------------------------------------------------------------
	// ------------------------------------------------INIT----------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name errorGoBack
	 * @description go back to homePage when errors occur
	 */
	function errorGoBack(errorDisplay, errorOptions) {
		if (errorDisplay) {
			MessageService.error('PLAYGROUND_FILE_NOT_FOUND_TITLE', 'PLAYGROUND_FILE_NOT_FOUND', errorOptions);
		}
		$state.go(state.route.previous, state.route.previousOptions);
	}

	/**
	 * @ngdoc method
	 * @name loadPreparation
	 * @description open a preparation
	 */
	function loadPreparation(preparation) {
		PlaygroundService.load(preparation)
			.then(() => {
				if (shouldFetchStatistics()) {
					fetchStatistics();
				}
			})
			.catch(() => errorGoBack(true, { type: 'preparation' }));
	}

	/**
	 * @ngdoc method
	 * @name loadDataset
	 * @description open a dataset
	 */
	function loadDataset(dataset) {
		PlaygroundService.initPlayground(dataset)
			.then(() => {
				if (shouldFetchStatistics()) {
					fetchStatistics();
				}
			})
			.catch(() => errorGoBack(true, { type: 'dataset' }));
	}

	/**
	 * @ngdoc method
	 * @name shouldReloadPreparation
	 * @description Check if the preparation should be reloaded.
	 * The preparation is not reloaded if (and) :
	 * - the current playground preparation is the one we want
	 * - the route param "reload" is not set explicitly to false
	 */
	function shouldReloadPreparation() {
		const currentPrep = state.playground.preparation;
		if (!currentPrep || $stateParams.prepid !== currentPrep.id) {
			return true;
		}

		return $stateParams.reload !== false;
	}

	if ($stateParams.prepid) {
		StateService.setPreviousRoute(
			HOME_PREPARATIONS_ROUTE,
			{ folderId: state.inventory.folder.metadata.id }
		);
		if (!shouldReloadPreparation()) {
			return;
		}

		StateService.setIsLoadingPlayground(true);
		PlaygroundService.startLoader();
		PreparationService.getDetails($stateParams.prepid)
			.then((preparation) => {
				loadPreparation(preparation);
				return preparation;
			})
			.then(preparation => DatasetService.getMetadata(preparation.dataSetId))
			.then(dataset => StateService.setCurrentDataset(dataset))
			.catch(() => {
				PlaygroundService.stopLoader();
				return errorGoBack(false);
			});
	}
	else if ($stateParams.datasetid) {
		StateService.setPreviousRoute(HOME_DATASETS_ROUTE);
		StateService.setIsLoadingPlayground(true);
		PlaygroundService.startLoader();
		DatasetService.getMetadata($stateParams.datasetid)
			.then(loadDataset)
			.catch(() => {
				PlaygroundService.stopLoader();
				return errorGoBack(false);
			});
	}
}

/**
 * @ngdoc property
 * @name hasActiveStep
 * @propertyOf data-prep.playground.controller:PlaygroundCtrl
 * @description checks if there is at least 1 active step, by checking the 1st step in the recipe
 * It is bound to the status of the 1st step in the state
 * @type boolean
 */
Object.defineProperty(
	PlaygroundCtrl.prototype,
	'hasActiveStep',
	{
		enumerable: true,
		configurable: false,
		get() {
			const firstStep = this.state.playground.recipe.current.steps[0];
			return firstStep && !firstStep.inactive;
		},

		set: () => {
		},
	}
);
