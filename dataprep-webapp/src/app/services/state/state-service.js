/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https:// github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const state = {};

/**
 * @ngdoc service
 * @name data-prep.services.state.service:StateService
 * @description Entry level for State services
 */
export function StateService(RouteStateService, routeState,
                             PlaygroundStateService, playgroundState,
                             DatasetStateService, datasetState,
                             EasterEggsStateService, easterEggsState,
                             InventoryStateService, inventoryState,
                             FeedbackStateService, feedbackState,
                             ImportStateService, importState,
                             ExportStateService, exportState,
                             HomeStateService, homeState,
                             ProgressStateService, progressState,
                             SearchStateService, searchState,
                             MessageStateService, messageState) {
	'ngInject';

	state.route = routeState;
	state.playground = playgroundState;
	state.dataset = datasetState;
	state.easterEggsState = easterEggsState;
	state.inventory = inventoryState;
	state.feedback = feedbackState;
	state.import = importState;
	state.export = exportState;
	state.home = homeState;
	state.search = searchState;
	state.progress = progressState;
	state.message = messageState;

	return {
		// route
		setPreviousRoute: RouteStateService.setPrevious,
		setNextRoute: RouteStateService.setNext,
		resetPreviousRoute: RouteStateService.resetPrevious,
		resetNextRoute: RouteStateService.resetNext,
		resetRoute: RouteStateService.reset.bind(RouteStateService),

		// home
		setBuilds: HomeStateService.setBuilds,
		setCopyMoveTree: HomeStateService.setCopyMoveTree,
		setCopyMoveTreeLoading: HomeStateService.setCopyMoveTreeLoading,
		setHomeSidePanelDock: HomeStateService.setSidePanelDock,
		toggleHomeSidepanel: HomeStateService.toggleSidepanel,
		toggleCopyMovePreparation: HomeStateService.toggleCopyMovePreparation,
		toggleFolderCreator: HomeStateService.toggleFolderCreator,
		togglePreparationCreator: HomeStateService.togglePreparationCreator,
		toggleAbout: HomeStateService.toggleAbout,

		// playground
		resetPlayground: PlaygroundStateService.reset,
		setCurrentDataset: PlaygroundStateService.setDataset,
		setCurrentData: PlaygroundStateService.setData,
		setCurrentPreparation: PlaygroundStateService.setPreparation,
		setIsFetchingStats: PlaygroundStateService.setIsFetchingStats,
		setIsLoadingPlayground: PlaygroundStateService.setIsLoading,
		setIsSavingPreparation: PlaygroundStateService.setIsSavingPreparation,
		setNameEditionMode: PlaygroundStateService.setNameEditionMode,
		setPreparationName: PlaygroundStateService.setPreparationName,
		updateDatasetStatistics: PlaygroundStateService.updateDatasetStatistics,
		updateDatasetRecord: PlaygroundStateService.updateDatasetRecord,
		setCurrentSampleType: PlaygroundStateService.setSampleType,
		setPlaygroundReadOnlyMode: PlaygroundStateService.setReadOnlyMode,
		setStepInEditionMode: PlaygroundStateService.setStepInEditionMode,
		setIsNameValidationVisible: PlaygroundStateService.setIsNameValidationVisible,
		setIsPreprationPickerVisible: PlaygroundStateService.setIsPreprationPickerVisible,
		setSavingPreparationFolders: PlaygroundStateService.setSavingPreparationFolders,
		setIsSavingPreparationFoldersLoading: PlaygroundStateService.setIsSavingPreparationFoldersLoading,
		setPreviewDisabled: PlaygroundStateService.setPreviewDisabled,

		// playground - dataset parameters
		toggleDatasetParameters: PlaygroundStateService.toggleDatasetParameters,
		hideDatasetParameters: PlaygroundStateService.hideDatasetParameters,
		setIsSendingDatasetParameters: PlaygroundStateService.setIsSendingDatasetParameters,
		setDatasetEncodings: PlaygroundStateService.setDatasetEncodings,

		// playground - recipe
		hideRecipe: PlaygroundStateService.hideRecipe,
		showRecipe: PlaygroundStateService.showRecipe,
		setHoveredStep: PlaygroundStateService.setHoveredStep,
		setRecipeSteps: PlaygroundStateService.setRecipeSteps,
		setRecipePreviewSteps: PlaygroundStateService.setRecipePreviewSteps,
		restoreRecipeBeforePreview: PlaygroundStateService.restoreRecipeBeforePreview,
		disableRecipeStepsAfter: PlaygroundStateService.disableRecipeStepsAfter,
		setRecipeAllowDistributedRun: PlaygroundStateService.setRecipeAllowDistributedRun,

		// playground - grid
		setColumnFocus: PlaygroundStateService.setColumnFocus,
		setGridSelection: PlaygroundStateService.setGridSelection,
		toggleColumnSelection: PlaygroundStateService.toggleColumnSelection,
		changeRangeSelection: PlaygroundStateService.changeRangeSelection,
		setSemanticDomains: PlaygroundStateService.setSemanticDomains,
		setPrimitiveTypes: PlaygroundStateService.setPrimitiveTypes,

		// playground - lookup
		setLookupActions: PlaygroundStateService.setLookupActions,
		setLookupAddedActions: PlaygroundStateService.setLookupAddedActions,
		setLookupDatasets: PlaygroundStateService.setLookupDatasets,
		setLookupDataset: PlaygroundStateService.setLookupDataset,
		setLookupSelectedColumn: PlaygroundStateService.setLookupSelectedColumn,
		setLookupVisibility: PlaygroundStateService.setLookupVisibility,
		updateLookupColumnsToAdd: PlaygroundStateService.updateLookupColumnsToAdd,
		setLookupDatasetsSort: PlaygroundStateService.setLookupDatasetsSort,
		setLookupDatasetsOrder: PlaygroundStateService.setLookupDatasetsOrder,
		setLookupData: PlaygroundStateService.setLookupData,

		// playground - filters
		addGridFilter: PlaygroundStateService.addGridFilter,
		removeGridFilter: PlaygroundStateService.removeGridFilter,
		removeAllGridFilters: PlaygroundStateService.removeAllGridFilters,
		updateGridFilter: PlaygroundStateService.updateGridFilter,
		enableFilters: PlaygroundStateService.enableFilters,
		disableFilters: PlaygroundStateService.disableFilters,

		// playground - Actions
		selectTransformationsTab: PlaygroundStateService.selectTransformationsTab,
		setTransformations: PlaygroundStateService.setTransformations,
		setTransformationsLoading: PlaygroundStateService.setTransformationsLoading,
		updateFilteredTransformations: PlaygroundStateService.updateFilteredTransformations,

		// playground - Statistics
		setStatisticsBoxPlot: PlaygroundStateService.setStatisticsBoxPlot,
		setStatisticsDetails: PlaygroundStateService.setStatisticsDetails,
		setStatisticsRangeLimits: PlaygroundStateService.setStatisticsRangeLimits,
		setStatisticsHistogram: PlaygroundStateService.setStatisticsHistogram,
		setStatisticsFilteredHistogram: PlaygroundStateService.setStatisticsFilteredHistogram,
		setStatisticsHistogramActiveLimits: PlaygroundStateService.setStatisticsHistogramActiveLimits,
		setStatisticsPatterns: PlaygroundStateService.setStatisticsPatterns,
		setStatisticsFilteredPatterns: PlaygroundStateService.setStatisticsFilteredPatterns,

		// dataset
		startUploadingDataset: DatasetStateService.startUploadingDataset,
		finishUploadingDataset: DatasetStateService.finishUploadingDataset,

		// easter eggs
		enableEasterEgg: EasterEggsStateService.enableEasterEgg,
		disableEasterEgg: EasterEggsStateService.disableEasterEgg,

		// inventory
		enableInventoryEdit: InventoryStateService.enableEdit,
		disableInventoryEdit: InventoryStateService.disableEdit,
		setDatasetName: InventoryStateService.setDatasetName,
		setDatasetToUpdate: InventoryStateService.setDatasetToUpdate,
		setDatasets: InventoryStateService.setDatasets,
		removeDataset: InventoryStateService.removeDataset,
		setDatasetsSort: InventoryStateService.setDatasetsSort,
		setDatasetsDisplayMode: InventoryStateService.setDatasetsDisplayMode,
		setPreparationsSort: InventoryStateService.setPreparationsSort,
		setPreparationsDisplayMode: InventoryStateService.setPreparationsDisplayMode,
		setHomeFolderId: InventoryStateService.setHomeFolderId,

		setFolder: InventoryStateService.setFolder,
		setBreadcrumb: InventoryStateService.setBreadcrumb,
		setBreadcrumbChildren: InventoryStateService.setBreadcrumbChildren,

		setFetchingInventoryDatasets: InventoryStateService.setFetchingDatasets,
		setFetchingInventoryPreparations: InventoryStateService.setFetchingPreparations,

		// feedback
		showFeedback: FeedbackStateService.show,
		hideFeedback: FeedbackStateService.hide,

		// import
		showImport: ImportStateService.showImport,
		hideImport: ImportStateService.hideImport,
		setCurrentImportItem: ImportStateService.setCurrentImportItem,

		// export
		setExportTypes: ExportStateService.setExportTypes,
		setDefaultExportType: ExportStateService.setDefaultExportType,
		resetExportTypes: ExportStateService.reset,

		// search
		toggleSearch: SearchStateService.toggle,
		setSearching: SearchStateService.setSearching,
		setSearchInput: SearchStateService.setSearchInput,
		setSearchResults: SearchStateService.setSearchResults,
		setSearchCategories: SearchStateService.setSearchCategories,
		setFocusedSectionIndex: SearchStateService.setFocusedSectionIndex,
		setFocusedItemIndex: SearchStateService.setFocusedItemIndex,

		// progress
		startProgress: ProgressStateService.start,
		nextProgress: ProgressStateService.next,
		resetProgress: ProgressStateService.reset,
		getCurrentProgressStep: ProgressStateService.getCurrentStep,
		addProgressSchema: ProgressStateService.addSchema,

		// message
		pushMessage: MessageStateService.push,
		popMessage: MessageStateService.pop,
	};
}
