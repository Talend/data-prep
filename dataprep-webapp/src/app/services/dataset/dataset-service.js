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
 * @name data-prep.services.dataset.service:DatasetService
 * @description Dataset general service. This service manage the operations that touches the datasets
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.dataset.service:DatasetListService
 * @requires data-prep.services.dataset.service:DatasetRestService
 * @requires data-prep.services.preparation.service:PreparationListService
 * @requires data-prep.services.utils.service:StorageService
 *
 */
export default function DatasetService($q, state, StateService, DatasetListService, DatasetRestService, StorageService) {
    'ngInject';

    return {
        init: init,

        //lifecycle
        create: DatasetListService.create,
        update: DatasetListService.update,
        delete: deleteDataset,
        clone: DatasetListService.clone,

        //dataset actions
        updateColumn: DatasetRestService.updateColumn,
        processCertification: DatasetListService.processCertification,
        toggleFavorite: DatasetListService.toggleFavorite,

        //content
        getMetadata: DatasetRestService.getMetadata,
        getContent: DatasetRestService.getContent,

        //dataset getters, refresher
        refreshDatasets: DatasetListService.refreshDatasets,
        getDatasets: getDatasets,           //promise that resolves datasets list
        getDatasetById: getDatasetById,     //retrieve dataset by id
        getDatasetByName: getDatasetByName, //retrieve dataset by name
        getSheetPreview: getSheetPreview,

        //dataset update
        rename: rename,
        setDatasetSheet: setDatasetSheet,
        updateParameters: updateParameters,
        refreshSupportedEncodings: refreshSupportedEncodings,

        //compatible preparation list
        getCompatiblePreparations: getCompatiblePreparations,

        //utils
        getUniqueName: getUniqueName,
        createDatasetInfo: createDatasetInfo

    };

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------Lifecycle--------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @name _refreshDatasetsSort
     * @description Refresh the actual sort parameter
     * */
    function _refreshDatasetsSort() {
        const savedSort = StorageService.getDatasetsSort();
        if (savedSort) {
            StateService.setDatasetsSort(_.find(state.inventory.sortList, {id: savedSort}));
        }
    }

    /**
     * @ngdoc method
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @name _refreshDatasetsOrder
     * @description Refresh the actual order parameter
     */
    function _refreshDatasetsOrder() {
        const savedSortOrder = StorageService.getDatasetsOrder();
        if (savedSortOrder) {
            StateService.setDatasetsOrder(_.find(state.inventory.orderList, {id: savedSortOrder}));
        }
    }

    /**
     * @ngdoc method
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @name _refreshDatasetsOrder
     * @description Init datasets sort/order and refresh datasets list
     */
    function init() {
        _refreshDatasetsSort();
        _refreshDatasetsOrder();
        return DatasetListService.refreshDatasets();
    }

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------Lifecycle--------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name deleteDataset
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @param {object} dataset The dataset to delete
     * @description Delete a dataset.
     * @returns {promise} The pending DELETE promise
     */
    function deleteDataset(dataset) {
        return DatasetListService.delete(dataset)
            .then((response) => {
                StorageService.removeAllAggregations(dataset.id);
                return response;
            });
    }

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------Metadata---------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getDatasets
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @description Return a promise that resolves the datasets list.
     * @returns {promise} The pending GET or resolved promise
     */
    function getDatasets() {
        if (DatasetListService.hasDatasetsPromise()) {
            return DatasetListService.getDatasetsPromise();
        }
        else {
            return state.inventory.datasets !== null ?
                $q.when(state.inventory.datasets) :
                DatasetListService.refreshDatasets();
        }
    }

    /**
     * @ngdoc method
     * @name getDatasetByName
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @param {string} name The dataset name
     * @description Get the dataset that has the wanted name. The case is not important here.
     * @returns {object} The dataset that has the same name (case insensitive)
     */
    function getDatasetByName(name) {
        const lowerCaseName = name.toLowerCase();
        return _.find(state.inventory.datasets, (dataset) => {
            return dataset.name.toLowerCase() === lowerCaseName;
        });
    }

    /**
     * @ngdoc method
     * @name getDatasetById
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @param {string} datasetId The dataset id
     * @description Get the dataset that has the wanted id
     * @returns {promise} The dataset
     */
    function getDatasetById(datasetId) {
        return DatasetListService.getDatasetsPromise().then((datasetList) => {
            return _.find(datasetList, (dataset) => {
                return dataset.id === datasetId;
            });
        });
    }

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------Utils-----------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name createDatasetInfo
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @description create the dataset info from the given parameters.
     * @param {file} file - the file tu upload in case of
     * @param {string} name - the dataset name
     * @param {string} id - the dataset id (used to update existing dataset)
     * @returns {Object} - the adapted dataset infos {name: string, progress: number, file: *, error: boolean}
     */
    function createDatasetInfo(file, name, id) {
        return {
            name: name,
            progress: 0,
            file: file,
            error: false,
            id: id,
            type: file === null ? 'remote' : 'file'
        };
    }

    /**
     * @ngdoc method
     * @name getUniqueName
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @param {string} name - the base name
     * @description Get a unique name from a base name. The existence check is done on the local dataset list. It
     *     transform the base name, adding "(number)"
     * @returns {string} - the unique name
     */
    function getUniqueName(name) {
        const cleanedName = name.replace(/\([0-9]+\)$/, '').trim();
        let result = cleanedName;

        let index = 1;
        while (getDatasetByName(result)) {
            result = cleanedName + ' (' + index + ')';
            index++;
        }

        return result;
    }

    //--------------------------------------------------------------------------------------------------------------
    //------------------------------------------------Sheet Preview-------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getSheetPreview
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @param {object} metadata The dataset metadata
     * @param {string} sheetName The sheet name
     * @description Get a dataset sheet preview
     * @returns {object} The preview data
     */
    function getSheetPreview(metadata, sheetName) {
        return DatasetRestService.getSheetPreview(metadata.id, sheetName);
    }

    /**
     * @ngdoc method
     * @name setDatasetSheet
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @param {object} metadata The dataset metadata
     * @param {string} sheetName The sheet name
     * @description Set the selected sheet to the dataset
     * @returns {Promise} The process Promise
     */
    function setDatasetSheet(metadata, sheetName) {
        metadata.sheetName = sheetName;
        return DatasetRestService.updateMetadata(metadata);
    }

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------Dataset Parameters-----------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    function _extractOriginalParameters(metadata) {
        return {
            //TODO remove this and review the datasets model to NOT change the original object. This is done here to
            // avoid cyclic ref
            defaultPreparation: metadata.defaultPreparation,
            preparations: metadata.preparations,

            separator: metadata.parameters.SEPARATOR,
            encoding: metadata.encoding
        };
    }

    function _setParameters(metadata, parameters) {
        //TODO remove this and review the datasets model to NOT change the original object. This is done here to avoid
        // avoid cyclic ref
        metadata.defaultPreparation = parameters.defaultPreparation;
        metadata.preparations = parameters.preparations;

        metadata.parameters.SEPARATOR = parameters.separator;
        metadata.encoding = parameters.encoding;
    }

    /**
     * @ngdoc method
     * @name updateParameters
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @param {object} metadata The dataset metadata
     * @param {object} parameters The new parameters
     * @description Set the new parameters
     * @returns {Promise} The process Promise
     */
    function updateParameters(metadata, parameters) {
        const originalParameters = _extractOriginalParameters(metadata);
        _setParameters(metadata, parameters);

        return DatasetRestService.updateMetadata(metadata)
            .then(() => {
                metadata.defaultPreparation = originalParameters.defaultPreparation;
                metadata.preparations = originalParameters.preparations;
            })
            .catch((error) => {
                _setParameters(metadata, originalParameters);
                return $q.reject(error);
            });
    }

    /**
     * @ngdoc method
     * @name refreshEncodings
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @description Refresh the supported encodings list
     * @returns {Promise} The process Promise
     */
    function refreshSupportedEncodings() {
        return DatasetRestService.getEncodings()
            .then(StateService.setDatasetEncodings);
    }

    /**
     * @ngdoc method
     * @name getCompatiblePreparations
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @description fetches the compatible prepartions for a given dataset
     * @returns {Promise} The process Promise
     */
    function getCompatiblePreparations(datasetId) {
        return DatasetRestService.getCompatiblePreparations(datasetId)
            .then((compatiblePreparations) => {
                if (state.playground.preparation) {
                    compatiblePreparations = _.reject(compatiblePreparations, {id: state.playground.preparation.id});
                }

                return _.map(compatiblePreparations, (candidatePrepa) => {
                    return {
                        preparation: candidatePrepa,
                        dataset: _.find(state.inventory.datasets, {id: candidatePrepa.dataSetId}),
                    };
                });
            });
    }

    //--------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------Rename---------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    //TODO remove this and review the datasets model to NOT change the original object. This is done here to
    // avoid cyclic ref
    function _removePreparations(metadata) {
        const preparations = {
            defaultPreparation: metadata.defaultPreparation,
            preparations: metadata.preparations,
        };

        metadata.defaultPreparation = null;
        metadata.preparations = null;

        return preparations;
    }

    //TODO remove this and review the datasets model to NOT change the original object. This is done here to
    // avoid cyclic ref
    function _injectPreparations(metadata, preparations) {
        metadata.defaultPreparation = preparations.defaultPreparation;
        metadata.preparations = preparations.preparations;
    }

    /**
     * @ngdoc method
     * @name rename
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @param {object} metadata The dataset metadata
     * @param {string} name The new name
     * @description Set the new name
     * @returns {Promise} The process Promise
     */
    function rename(metadata, name) {
        const oldName = metadata.name;
        StateService.setDatasetName(metadata.id, name);
        const preparations = _removePreparations(metadata);

        return DatasetRestService.updateMetadata(metadata)
            .catch((error) => {
                StateService.setDatasetName(metadata.id, oldName);
                return $q.reject(error);
            })
            .finally(() => { _injectPreparations(metadata, preparations) });
    }
}