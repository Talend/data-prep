/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.dataset.service:DatasetRestService
 * @description Dataset service. This service provide the entry point to the backend dataset REST api.<br/>
 * <b style="color: red;">WARNING : do NOT use this service directly.
 * {@link data-prep.services.dataset.service:DatasetService DatasetService} must be the only entry point for datasets</b>
 */
export default function DatasetRestService($rootScope, $upload, $http, RestURLs) {
	'ngInject';

	return {
		create,
		update,
		delete: deleteDataset,
		clone: cloneDataset,

		updateColumn,

		getDatasets,
		getFilteredDatasets,
		updateMetadata,
		getMetadata,
		getContent,
		getSheetPreview,
		getEncodings,
		getDatasetByName,

		toggleFavorite,

		getCompatiblePreparations,
	};

    //--------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------------Dataset----------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name create
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @description Import the remote dataset
     * @param {parameters} parameters The import parameters
     * @param {object} file The file imported from local
     * @param {string} contentType The request Content-Type
     * @returns {Promise} The POST promise
     */
	function create(parameters, contentType, file) {
		const { name, size } = parameters;
		let url = `${RestURLs.uploadDatasetUrl}?name=${encodeURIComponent(name)}`;
		if (size) {
			url += `&size=${size}`;
		}
		const req = {
			url,
			headers: {
				'Content-Type': contentType,
			},
			data: file || parameters,
		};
		return $upload.http(req);
	}

    /**
     * @ngdoc method
     * @name update
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @description Update the dataset
     * @param {dataset} dataset The dataset infos to update
	 * @param {object} parameters The update parameters
     * @returns {Promise} the $upload promise
     */
	function update(dataset, parameters) {
		let url = RestURLs.uploadDatasetUrl + '/' + dataset.id + '?name=' + encodeURIComponent(dataset.name);
		if (parameters && parameters.size) {
			url += `&size=${parameters.size}`;
		}

		return $upload.http({
			url,
			method: 'PUT',
			headers: { 'Content-Type': 'text/plain' },
			data: dataset.file,
		});
	}

    /**
     * @ngdoc method
     * @name delete
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @description Delete the dataset
     * @param {object} dataset the dataset infos to delete
     * @returns {Promise} The DELETE promise
     */
	function deleteDataset(dataset) {
		return $http.delete(RestURLs.datasetUrl + '/' + dataset.id);
	}

    /**
     * @ngdoc method
     * @name cloneDataset
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @description Clone the dataset
     * @param {Object} dataset the dataset metadata
     * @returns {Promise} The GET promise
     */
	function cloneDataset(dataset) {
		return $http.post(RestURLs.datasetUrl + '/' + dataset.id + '/copy');
	}

    //--------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------------Metadata---------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getDatasets
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @param {string} sortType Sort by specified type
     * @param {string} sortOrder Sort in specified order
     * @param {Promise} deferredAbort abort request when resolved
     * @description Get the dataset list
     * @returns {Promise} The GET call promise
     */
	function getDatasets(sortType, sortOrder, deferredAbort) {
		let url = RestURLs.datasetUrl + '/summary';

		if (sortType) {
			url += '?sort=' + sortType;
		}

		if (sortOrder) {
			url += (sortType ? '&' : '?') + 'order=' + sortOrder;
		}

		return $http({
			url,
			method: 'GET',
			timeout: deferredAbort.promise,
		});
	}

    /**
     * @ngdoc method
     * @name search
     * @methodOf data-prep.services.inventory.service:InventoryRestService
     * @param {String} name The dataset name
     * @returns {Promise} The GET promise
     */
	function getDatasetByName(name) {
		return $http.get(`${RestURLs.searchUrl}?name=${encodeURIComponent(name)}&strict=true&filter=datasets`)
            .then(resp => resp.data.datasets && resp.data.datasets[0]);
	}

    /**
     * @ngdoc method
     * @name getFilteredDatasets
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @param {string} filterParameters The url parameters
     * @description Get the dataset list respecting a filter passed in the params
     * @returns {Promise} The GET call promise
     */
	function getFilteredDatasets(filters) {
		const params = Object.keys(filters).map((key) => {
			return `${key}=${encodeURIComponent(filters[key])}`;
		}).join('&');

		return $http.get(`${RestURLs.datasetUrl}?${params}`)
				.then(resp => resp.data);
	}

    /**
     * @ngdoc method
     * @name updateMetadata
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @description Update the dataset metadata
     * @param {dataset} metadata The dataset infos to update
     * @returns {Promise} The PUT promise
     */
	function updateMetadata(metadata) {
		return $http.put(RestURLs.datasetUrl + '/' + metadata.id + '/metadata', metadata);
	}

    /**
     * @ngdoc method
     * @name updateColumn
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @description Update the dataset column
     * @param {string} datasetId The dataset id
     * @param {string} columnId The column id
     * @param {object} params The parameters containing typeId and/or domainId
     * @returns {Promise} The POST promise
     */
	function updateColumn(datasetId, columnId, params) {
		const url = RestURLs.datasetUrl + '/' + datasetId + '/column/' + columnId;
		return $http.post(url, params);
	}

    //--------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------------Content----------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getContent
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @description Get the dataset content
     * @param {string} datasetId The dataset id
     * @param {boolean} metadata If false, the metadata will not be returned
     * @returns {Promise} The GET promise
     */
	function getContent(datasetId, metadata, tql) {
		const url = `${RestURLs.datasetUrl}/${datasetId}`;
		const params = {
			metadata,
			includeTechnicalProperties: true,
		};
		if (tql) {
			params.filter = encodeURIComponent(tql);
		}
		return $http(
			{
				url,
				method: 'GET',
				params,
			}
		).then(response => response.data);
	}

    /**
     * @ngdoc method
     * @name getMetadata
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @description Get the dataset metadata
     * @param {string} datasetId The dataset id
     * @returns {Promise} The GET promise
     */
	function getMetadata(datasetId) {
		const url = RestURLs.datasetUrl + '/' + datasetId + '/metadata';
		return $http.get(url).then(response => response.data);
	}

    //--------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------Sheet Preview-------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getSheetPreview
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @description Get the dataset content
     * @param {string} datasetId The dataset id
     * @param {string} sheetName The sheet to preview
     * @returns {Promise} The GET promise
     */
	function getSheetPreview(datasetId, sheetName) {
		$rootScope.$emit('talend.loading.start');
		return $http.get(RestURLs.datasetUrl + '/preview/' + datasetId + '?metadata=true' + (sheetName ? '&sheetName=' + encodeURIComponent(sheetName) : ''))
			.then(response => response.data)
			.finally(() => {
				$rootScope.$emit('talend.loading.stop');
			});
	}

    //--------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------Toggle Favorite-----------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name toggleFavorite
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @description Toggle the Favorite flag for a dataset for the current user
     * @param {dataset} dataset The dataset to be toggled
     * @returns {Promise} The PUT promise
     */
	function toggleFavorite(dataset) {
		return $http.post(RestURLs.datasetUrl + '/favorite/' + dataset.id + '?unset=' + dataset.favorite);
	}

    //--------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------------Encodings--------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getEncodings
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @description Get the supported encoding list
     * @returns {Promise} The GET promise
     */
	function getEncodings() {
		return $http.get(RestURLs.datasetUrl + '/encodings')
            .then(response => response.data);
	}

    /**
     * @ngdoc method
     * @name getCompatiblePreparations
     * @methodOf data-prep.services.dataset.service:DatasetRestService
     * @description Get the compatible preparation list for a given dataset
     * @returns {Promise} The GET promise
     */
	function getCompatiblePreparations(datasetId) {
		return $http.get(RestURLs.datasetUrl + '/' + datasetId + '/compatiblepreparations')
            .then(response => response.data);
	}
}
