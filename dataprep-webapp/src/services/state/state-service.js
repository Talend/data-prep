(function() {
    'use strict';

    var state = {};

    /**
     * @ngdoc service
     * @name data-prep.services.state.service:StateService
     * @description Entry level for State services
     */
    function StateService(PlaygroundStateService, playgroundState, DatasetStateService, datasetState, FolderStateService, folderState) {
        state.playground = playgroundState;
        state.dataset = datasetState;
        state.folder = folderState;

        return {
            //playground
            hidePlayground: PlaygroundStateService.hide,
            showPlayground: PlaygroundStateService.show,
            resetPlayground: PlaygroundStateService.reset,
            setCurrentDataset: PlaygroundStateService.setDataset,
            setCurrentData: PlaygroundStateService.setData,
            setCurrentLookupData: PlaygroundStateService.setLookupData,
            setCurrentPreparation: PlaygroundStateService.setPreparation,
            setLookupVisibility: PlaygroundStateService.setLookupVisibility,
            setNameEditionMode: PlaygroundStateService.setNameEditionMode,
            updateColumnsStatistics: PlaygroundStateService.updateColumnsStatistics,

            //playground - recipe
            hideRecipe: PlaygroundStateService.hideRecipe,
            showRecipe: PlaygroundStateService.showRecipe,

            //playground - grid
            setColumnFocus: PlaygroundStateService.setColumnFocus,
            setGridSelection: PlaygroundStateService.setGridSelection,

            //playground - lookupGrid
            setLookupColumnFocus: PlaygroundStateService.setLookupColumnFocus,
            setLookupGridSelection: PlaygroundStateService.setLookupGridSelection,
            setLookupDataset: PlaygroundStateService.setLookupDataset,
            setLookupColumnsToAdd: PlaygroundStateService.setLookupColumnsToAdd,
            resetLookup: PlaygroundStateService.resetLookup,

            //playground - filters
            addGridFilter: PlaygroundStateService.addGridFilter,
            removeGridFilter: PlaygroundStateService.removeGridFilter,
            removeAllGridFilters: PlaygroundStateService.removeAllGridFilters,
            updateGridFilter: PlaygroundStateService.updateGridFilter,

            //playground - Suggestions
            setSuggestionsLoading: PlaygroundStateService.setSuggestionsLoading,

            //dataset
            startUploadingDataset: DatasetStateService.startUploadingDataset,
            finishUploadingDataset: DatasetStateService.finishUploadingDataset,

            //folder
            setCurrentFolder: FolderStateService.setCurrentFolder,
            setCurrentFolderContent: FolderStateService.setCurrentFolderContent,
            setFoldersStack: FolderStateService.setFoldersStack,
            setMenuChilds: FolderStateService.setMenuChilds
        };
    }

    angular.module('data-prep.services.state')
        .service('StateService', StateService)
        .constant('state', state);
})();