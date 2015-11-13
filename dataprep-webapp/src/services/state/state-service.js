(function() {
    'use strict';

    var state = {};

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

            //playground - filters
            addGridFilter: PlaygroundStateService.addGridFilter,
            removeGridFilter: PlaygroundStateService.removeGridFilter,
            removeAllGridFilters: PlaygroundStateService.removeAllGridFilters,
            updateGridFilter: PlaygroundStateService.updateGridFilter,
            
            //dataset
            startUploadingDataset: DatasetStateService.startUploadingDataset,
            finishUploadingDataset: DatasetStateService.finishUploadingDataset,

            //folder
            setCurrentFolder: FolderStateService.setCurrentFolder
        };
    }

    angular.module('data-prep.services.state')
        .service('StateService', StateService)
        .constant('state', state);
})();