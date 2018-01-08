/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/


const filters = [
	{
		id: 'RECENT',
		order: 1,
		imageUrl: '/assets/images/inventory/recent-datasets.png',
		title: 'RECENT_DATASETS',
		description: 'RECENT_DATASETS_DESCRIPTION',
		getParametersMap: (name) => {
			return {
				sort: 'lastModificationDate',
				limit: true,
				name,
			};
		},
	},
	{
		id: 'FAVORITE',
		order: 2,
		icon: 'f',
		title: 'FAVORITE_DATASETS',
		description: 'FAVORITE_DATASETS_DESCRIPTION',
		getParametersMap: (name) => {
			return {
				favorite: true,
				name,
			};
		},
	},
	{
		id: 'ALL',
		order: 3,
		imageUrl: '/assets/images/inventory/all-datasets.png',
		title: 'ALL_DATASETS',
		description: 'ALL_DATASETS_DESCRIPTION',
		getParametersMap: (name) => {
			return {
				name,
			};
		},
	},
];

/**
 * @ngdoc service
 * @name data-prep.services.dataset.service:DatasetService
 * @description Dataset general service. This service manage the operations that touches the datasets
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.dataset.service:DatasetListService
 * @requires data-prep.services.dataset.service:DatasetRestService
 * @requires data-prep.services.utils.service:StorageService
 *
 */
export default function DatasetService($q, state, StateService, DatasetListService, DatasetRestService, StorageService) {
	'ngInject';

	return {
		filters,
		init,

		// lifecycle
		create: DatasetListService.create,
		update: DatasetListService.update,
		delete: deleteDataset,
		clone: DatasetListService.clone,

		// dataset actions
		updateColumn: DatasetRestService.updateColumn,
		toggleFavorite: DatasetListService.toggleFavorite,
		getStatusActions: DatasetListService.getStatusActions,
		getClassName: DatasetListService.getClassName,
		isRemoveEnabled,
		isRenameEnabled,
		adaptDatasets: DatasetListService.adaptDatasets,
		getDatasetActions: DatasetListService.getDatasetActions,
		getDatasetIcon: DatasetListService.getDatasetIcon,

		// content
		getMetadata: DatasetRestService.getMetadata,
		getContent: DatasetRestService.getContent,

		// dataset getters, refresher
		changeSort,
		refreshDatasets: DatasetListService.refreshDatasets,
		getDatasets,           // promise that resolves datasets list
		getFilteredDatasets, // retrieve datasets given a set of filters
		getDatasetById,     // retrieve dataset by id
		getDatasetByName, // retrieve dataset by name
		getSheetPreview,
		loadFilteredDatasets: DatasetRestService.loadFilteredDatasets, // retrieve datasets given a set of filters

		// dataset update
		rename,
		setDatasetSheet,
		updateParameters,
		refreshSupportedEncodings,

		// compatible preparation list
		getCompatiblePreparations,

		// utils
		getUniqueName,
		createDatasetInfo,
		checkNameAvailability,
		getLocationParamIteration: getParamIteration,
	};

	//--------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------Lifecycle--------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @methodOf data-prep.services.dataset.service:DatasetService
	 * @name refreshDatasetsSort
	 * @description Refresh the actual sort parameter
	 * */
	function refreshDatasetsSort() {
		const savedSort = StorageService.getDatasetsSort();
		if (savedSort) {
			StateService.setDatasetsSort(savedSort.field, savedSort.isDescending);
		}
	}

	/**
	 * @ngdoc method
	 * @methodOf data-prep.services.dataset.service:DatasetService
	 * @name refreshDatasetsOrder
	 * @description Init datasets sort/order and refresh datasets list
	 */
	function init() {
		refreshDatasetsSort();
		return DatasetListService.refreshDatasets();
	}

	//--------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------Lifecycle--------------------------------------------------
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

	/**
	 * @ngdoc method
	 * @name isRemoveEnabled
	 * @methodOf data-prep.services.dataset.service:DatasetService
	 * @description check if Remove is enabled
	 */
	function isRemoveEnabled() {
		return true;
	}

	/**
	 * @ngdoc method
	 * @name isRenameEnabled
	 * @methodOf data-prep.services.dataset.service:DatasetService
	 * @description check if Rename is enabled
	 */
	function isRenameEnabled() {
		return true;
	}

	//--------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------Metadata---------------------------------------------------
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
			return state.inventory.datasets.content !== null ?
				$q.when(state.inventory.datasets.content) :
				DatasetListService.refreshDatasets();
		}
	}

	/**
	 * @ngdoc method
	 * @name getFilteredDatasets
	 * @methodOf data-prep.services.dataset.service:DatasetService
	 * @param {object} filter The filter definition
	 * @param {string} name The dataset name (or part of it)
	 * @description Return a promise that resolves the datasets list.
	 * @returns {promise} The GET promise
	 */
	function getFilteredDatasets(filter, name) {
		return DatasetRestService.getFilteredDatasets(filter.getParametersMap(name));
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
		return state.inventory.datasets.content.find(
			dataset => dataset.name.toLowerCase() === lowerCaseName
		);
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

	/**
	 * @ngdoc method
	 * @name changeSort
	 * @methodOf data-prep.services.dataset.service:DatasetService
	 * @description Change the datasets list sort
	 * @param {string} field The sort field
	 * @param {string} isDescending True if sort is descending
	 */
	function changeSort({ field, isDescending }) {
		const oldField = state.inventory.datasets.sort.field;
		const oldIsDescending = state.inventory.datasets.sort.isDescending;

		StateService.setDatasetsSort(field, isDescending);

		return DatasetListService.refreshDatasets()
			.then(() => StorageService.setDatasetsSort(field, isDescending))
			.catch(() => StateService.setDatasetsSort(oldField, oldIsDescending));
	}

	//--------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------Utils-----------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name createDatasetInfo
	 * @methodOf data-prep.services.dataset.service:DatasetService
	 * @description create the dataset info from the given parameters.
	 * @param {file} file The file tu upload in case of
	 * @param {string} name The dataset name
	 * @param {string} id The dataset id (used to update existing dataset)
	 * @returns {Object} The adapted dataset infos {name: string, progress: number, file: *, error: boolean}
	 */
	function createDatasetInfo(file, name, id) {
		return {
			name,
			progress: 0,
			file,
			error: false,
			id,
			type: file === null ? 'remote' : 'file',
		};
	}

	/**
	 * @ngdoc method
	 * @name getUniqueName
	 * @methodOf data-prep.services.dataset.service:DatasetService
	 * @param {string} name The base name
	 * @param {number} index The number to add initialized to 1
	 * @description Get a unique name from a base name. The existence check is done on the local dataset list. It
	 *     transform the base name, adding "(number)"
	 * @returns {promise} The process to get a unique name
	 */
	function getUniqueName(name, index = 1) {
		const cleanedName = name.replace(/\([0-9]+\)$/, '').trim();
		const result = cleanedName + ' (' + index + ')';

		return checkNameAvailability(result)
			.catch(() => getUniqueName(name, index + 1));
	}

	/**
	 * @ngdoc method
	 * @name checkNameAvailability
	 * @methodOf data-prep.services.dataset.service:DatasetService
	 * @description Check if the dataset name is available.
	 * @param {string} name The dataset name
	 * @returns {promise} Resolve the promise if it is available,
	 * reject it with the existing dataset if not
	 */
	function checkNameAvailability(name) {
		return DatasetRestService.getDatasetByName(name)
			.then((dataset) => {
				if (dataset) {
					return $q.reject(dataset);
				}
				else {
					return $q.when(name);
				}
			});
	}

	//--------------------------------------------------------------------------------------------------------------
	// ------------------------------------------------Sheet Preview-------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name getSheetPreview
	 * @methodOf data-prep.services.dataset.service:DatasetService
	 * @param {object} dataset The dataset
	 * @param {string} sheetName The sheet name
	 * @description Get a dataset sheet preview
	 * @returns {object} The preview data
	 */
	function getSheetPreview(dataset, sheetName) {
		return DatasetRestService.getSheetPreview(dataset.id, sheetName);
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
	// ---------------------------------------------Dataset Parameters-----------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	function extractOriginalParameters(metadata) {
		return {
			separator: metadata.parameters.SEPARATOR,
			encoding: metadata.encoding,
		};
	}

	function setParameters(metadata, parameters) {
		metadata.parameters.SEPARATOR = parameters.separator;
		metadata.parameters.ESCAPE_CHAR = parameters.escapeCharacter;
		metadata.parameters.TEXT_ENCLOSURE_CHAR = parameters.textEnclosureCharacter;
		metadata.encoding = parameters.encoding;
	}

	/**
	 * @ngdoc method
	 * @name getParamIteration
	 * @methodOf data-prep.services.dataset.service:DatasetService
	 * @description function for recursively gather params
	 * @param {object} paramsAccu The parameters values accumulator
	 * @param {array} parameters The parameters array
	 * @returns {object} The parameters
	 */
	function getParamIteration(paramsAccu, parameters) {
		if (parameters) {
			_.forEach(parameters, (paramItem) => {
				paramsAccu[paramItem.name] = typeof (paramItem.value) !== 'undefined' ? paramItem.value : paramItem.default;

				// deal with select inline parameters
				if (paramItem.type === 'select') {
					const selectedValue = _.find(paramItem.configuration.values, { value: paramItem.value });
					getParamIteration(paramsAccu, selectedValue.parameters);
				}
			});
		}

		return paramsAccu;
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
		const originalParameters = extractOriginalParameters(metadata);
		setParameters(metadata, parameters);

		return DatasetRestService.updateMetadata(metadata)
			.then(() => {
				metadata.preparations = originalParameters.preparations;
			})
			.catch((error) => {
				setParameters(metadata, originalParameters);
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
					compatiblePreparations = _.reject(compatiblePreparations, { id: state.playground.preparation.id });
				}

				return _.map(compatiblePreparations, (candidatePrepa) => {
					return {
						preparation: candidatePrepa,
						dataset: state.inventory.datasets.content.find(dataset => dataset.id === candidatePrepa.dataSetId),
					};
				});
			});
	}

	//--------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------Rename---------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
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
		metadata.name = name;
		return DatasetRestService.updateMetadata(metadata)
			.catch((error) => {
				StateService.setDatasetName(metadata.id, oldName);
				return $q.reject(error);
			});
	}
}
