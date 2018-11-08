/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { HOME_FOLDER } from '../inventory/inventory-state-service';

export const playgroundState = {
	candidatePreparations: [],
	isLoading: false,
	isSavingPreparation: false,
	preparation: null,
	preparationName: '',
	sampleType: 'HEAD',
	isReadOnly: false,
	stepInEditionMode: null,
	transformationInProgress: false,
	lastActiveStepId: null,
	isPreviewLoading: false,
};

export function PlaygroundStateService($translate,
                                       RecipeStateService, recipeState,
                                       GridStateService, gridState,
                                       FilterStateService, filterState,
                                       SuggestionsStateService, suggestionsState,
                                       LookupStateService, lookupState,
                                       StatisticsStateService, statisticsState,
                                       ParametersStateService, parametersState) {
	'ngInject';

	playgroundState.recipe = recipeState;
	playgroundState.grid = gridState;
	playgroundState.lookup = lookupState;
	playgroundState.filter = filterState;
	playgroundState.suggestions = suggestionsState;
	playgroundState.statistics = statisticsState;
	playgroundState.parameters = parametersState;

	return {
		// playground
		reset,
		setDataset,
		setIsFetchingStats,
		setIsLoading,
		setIsSavingPreparation,
		setPreparation,
		setPreparationName,
		setNameEditionMode,
		setData,
		setLastActiveStepId,
		getLastActiveStepId,
		resetLastActiveStepId,
		updateDatasetRecord,
		updateDatasetStatistics,
		setSampleType,
		setReadOnlyMode,
		setStepInEditionMode,
		setIsNameValidationVisible,
		setIsPreprationPickerVisible,
		setSavingPreparationFolders,
		setIsSavingPreparationFoldersLoading,
		setTransformationInProgress,
		setPreviewIsLoading,

		// parameters
		toggleDatasetParameters,
		hideDatasetParameters: ParametersStateService.hide,
		setIsSendingDatasetParameters: ParametersStateService.setIsSending,
		setDatasetEncodings: ParametersStateService.setEncodings,

		// recipe
		showRecipe: RecipeStateService.show,
		hideRecipe: RecipeStateService.hide,
		setHoveredStep: RecipeStateService.setHoveredStep,
		setRecipeSteps: RecipeStateService.setSteps,
		setRecipePreviewSteps: RecipeStateService.setPreviewSteps,
		restoreRecipeBeforePreview: RecipeStateService.restoreBeforePreview,
		disableRecipeStepsAfter: RecipeStateService.disableStepsAfter,
		setRecipeAllowDistributedRun: RecipeStateService.setAllowDistributedRun,

		// datagrid
		setColumnFocus: GridStateService.setColumnFocus,
		setGridSelection: GridStateService.setGridSelection,
		toggleColumnSelection: GridStateService.toggleColumnSelection,
		changeRangeSelection: GridStateService.changeRangeSelection,
		setSemanticDomains: GridStateService.setSemanticDomains,
		setPrimitiveTypes: GridStateService.setPrimitiveTypes,

		// lookup
		setLookupActions: LookupStateService.setActions,
		setLookupAddedActions: LookupStateService.setAddedActions,
		setLookupDatasets: LookupStateService.setDatasets,
		setLookupDataset: LookupStateService.setDataset,
		setLookupAddMode: LookupStateService.setAddMode,
		setLookupSelectedColumn: LookupStateService.setSelectedColumn,
		setLookupUpdateMode: LookupStateService.setUpdateMode,
		setLookupData: LookupStateService.setData,
		setLookupVisibility,
		setLookupLoading: LookupStateService.setLoading,
		setLookupModalLoading: LookupStateService.setModalLoading,
		updateLookupColumnsToAdd: LookupStateService.updateColumnsToAdd,
		setLookupDatasetsSort: LookupStateService.setSort,
		setLookupDatasetsOrder: LookupStateService.setOrder,

		// filters
		addGridFilter,
		updateGridFilter,
		updateColumnNameInFilters,
		removeGridFilter,
		removeAllGridFilters,
		enableFilters,
		disableFilters,

		// actions
		selectTransformationsTab: SuggestionsStateService.selectTab,
		setTransformations: SuggestionsStateService.setTransformations,
		setTransformationsLoading: SuggestionsStateService.setLoading,
		updateFilteredTransformations: SuggestionsStateService.updateFilteredTransformations,

		// statistics
		setStatisticsBoxPlot: StatisticsStateService.setBoxPlot,
		setStatisticsDetails: StatisticsStateService.setDetails,
		setStatisticsRangeLimits: StatisticsStateService.setRangeLimits,
		setStatisticsHistogram: StatisticsStateService.setHistogram,
		setStatisticsFilteredHistogram: StatisticsStateService.setFilteredHistogram,
		setStatisticsHistogramActiveLimits: StatisticsStateService.setHistogramActiveLimits,
		setStatisticsPatternsType: StatisticsStateService.setPatternsType,
		setStatisticsPatterns: StatisticsStateService.setPatterns,
		setStatisticsWordPatterns: StatisticsStateService.setWordPatterns,
		setStatisticsFilteredPatterns: StatisticsStateService.setFilteredPatterns,
		setStatisticsFilteredWordPatterns: StatisticsStateService.setFilteredWordPatterns,
		setStatisticsLoading: StatisticsStateService.setLoading,
	};

	//--------------------------------------------------------------------------------------------------------------
	// --------------------------------------------------PLAYGROUND--------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	function setReadOnlyMode(bool) {
		playgroundState.isReadOnly = bool;
	}

	function setSampleType(type) {
		playgroundState.sampleType = type;
	}

	function setDataset(dataset) {
		playgroundState.dataset = dataset;
	}

	function setData(data) {
		playgroundState.data = data;
		GridStateService.setData(data);
	}

	function setPreparation(preparation) {
		playgroundState.preparation = preparation;
	}

	function setPreparationName(preparationName) {
		playgroundState.preparationName = preparationName;
	}

	function setNameEditionMode(editionMode) {
		playgroundState.nameEditionMode = editionMode;
	}

	function setLastActiveStepId(stepId) {
		playgroundState.lastActiveStepId = stepId;
	}

	function getLastActiveStepId() {
		return playgroundState.lastActiveStepId || 'head';
	}

	function resetLastActiveStepId() {
		playgroundState.lastActiveStepId = null;
	}

	function updateDatasetStatistics(metadata) {
		_.forEach(playgroundState.data.metadata.columns, function (col) {
			const correspondingColumn = _.find(metadata.columns, { id: col.id });
			col.statistics = correspondingColumn.statistics;
			col.quality = correspondingColumn.quality;
		});
	}

	function updateDatasetRecord(records) {
		playgroundState.dataset.records = records;
	}

	function setIsFetchingStats(value) {
		playgroundState.isFetchingStats = value;
	}

	function setIsLoading(value) {
		playgroundState.isLoading = value;
	}

	function setIsSavingPreparation(value) {
		playgroundState.isSavingPreparation = value;
	}

	function setLookupVisibility(value) {
		if (value && playgroundState.grid.selectedColumns.length > 1) {
			playgroundState.grid.selectedColumns = [playgroundState.grid.selectedColumns[0]];
		}
		LookupStateService.setVisibility(value);
	}

	function setStepInEditionMode(step) {
		playgroundState.stepInEditionMode = step;
	}

	function setIsNameValidationVisible(bool) {
		playgroundState.isNameValidationVisible = bool;
	}

	function setIsPreprationPickerVisible(bool) {
		playgroundState.isPreprationPickerVisible = bool;
	}

	function setSavingPreparationFolders(tree) {
		if (tree.folder && tree.folder.path === HOME_FOLDER.path) {
			tree.folder.name = $translate.instant('HOME_FOLDER');
		}
		playgroundState.savingPreparationFolders = tree;
	}

	function setIsSavingPreparationFoldersLoading(bool) {
		playgroundState.isSavingPreparationFoldersLoading = bool;
	}

	function setTransformationInProgress(bool) {
		playgroundState.transformationInProgress = bool;
	}

	function setPreviewIsLoading(value) {
		playgroundState.isPreviewLoading = value;
	}

	//--------------------------------------------------------------------------------------------------------------
	// -------------------------------------------------PARAMETERS---------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	function toggleDatasetParameters() {
		console.log('[NC] toggle: ', parametersState.visible);
		if (parametersState.visible) {
			ParametersStateService.hide();
		}
		else {
			showDatasetParameters();
		}
	}

	function showDatasetParameters() {
		ParametersStateService.update(playgroundState.dataset);
		ParametersStateService.show();
	}

	//--------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------FILTERS----------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	function addGridFilter(filter) {
		FilterStateService.addGridFilter(filter);
		FilterStateService.enableFilters();
	}

	function updateColumnNameInFilters(columns) {
		FilterStateService.updateColumnNameInFilters(columns);
	}

	function updateGridFilter(oldFilter, newFilter) {
		FilterStateService.updateGridFilter(oldFilter, newFilter);
		FilterStateService.enableFilters();
	}

	function removeGridFilter(filter) {
		FilterStateService.removeGridFilter(filter);
	}

	function removeAllGridFilters() {
		FilterStateService.removeAllGridFilters();
	}

	function enableFilters() {
		FilterStateService.enableFilters();
	}

	function disableFilters() {
		FilterStateService.disableFilters();
	}

	function reset() {
		playgroundState.data = null;
		playgroundState.dataset = null;
		playgroundState.isFetchingStats = false;
		playgroundState.isLoading = false;
		playgroundState.isSavingPreparation = false;
		playgroundState.nameEditionMode = false;
		playgroundState.lookupData = null;
		playgroundState.preparation = null;
		playgroundState.sampleType = 'HEAD';
		playgroundState.lastActiveStepId = null;
		playgroundState.isReadOnly = false;
		playgroundState.stepInEditionMode = null;
		playgroundState.isNameValidationVisible = false;
		playgroundState.isPreprationPickerVisible = false;
		playgroundState.savingPreparationFolders = null;
		playgroundState.isSavingPreparationFoldersLoading = false;
		playgroundState.transformationInProgress = false;

		RecipeStateService.reset();
		FilterStateService.reset();
		GridStateService.reset();
		LookupStateService.reset();
		SuggestionsStateService.reset();
		StatisticsStateService.reset();
		ParametersStateService.reset();
	}
}
