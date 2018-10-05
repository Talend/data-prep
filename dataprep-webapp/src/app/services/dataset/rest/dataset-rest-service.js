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
export default function DatasetRestService($rootScope, $upload, $http, RestURLs, UrlService) {
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

		getRelatedPreparations,
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
		const params = { name };

		if (size) {
			params.size = size;
		}

		return $upload.http({
			url: UrlService.build(RestURLs.uploadDatasetUrl, params),
			headers: {
				'Content-Type': contentType,
			},
			data: file || parameters,
		});
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
		const params = {
			name: dataset.name,
		};

		if (parameters && parameters.size) {
			params.size = parameters.size;
		}

		return $upload.http({
			url: UrlService.build(`${RestURLs.uploadDatasetUrl}/${dataset.id}`, params),
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
		return $http.delete(`${RestURLs.datasetUrl}/${dataset.id}`);
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
		return $http.post(`${RestURLs.datasetUrl}/${dataset.id}/copy`);
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
		const params = {};
		if (sortType) {
			params.sort = sortType;
		}

		if (sortOrder) {
			params.order = sortOrder;
		}

		return $http({
			url: UrlService.build(`${RestURLs.datasetUrl}/summary`, params),
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
		return $http.get(UrlService.build(RestURLs.searchUrl, {
			name,
			strict: true,
			categories: 'dataset',
		})).then(resp => resp.data.dataset && resp.data.dataset[0]);
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
		return $http.get(UrlService.build(RestURLs.datasetUrl, filters))
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
		return $http.put(`${RestURLs.datasetUrl}/${metadata.id}/metadata`, metadata);
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
		return $http.post(`${RestURLs.datasetUrl}/${datasetId}/column/${columnId}`, params);
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
	function getContent(datasetId, metadata, tql = null) {
		const url = `${RestURLs.datasetUrl}/${datasetId}`;
		const params = {
			metadata,
			includeTechnicalProperties: true,
			filter: tql || '',
		};

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
		return $http.get(`${RestURLs.datasetUrl}/${datasetId}/metadata`)
			.then(response => response.data);
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
		const params = { metadata: true };

		if (sheetName) {
			params.sheetName = sheetName;
		}

		$rootScope.$emit('talend.loading.start');
		return $http.get(UrlService.build(`${RestURLs.datasetUrl}/preview/${datasetId}`, params))
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
		return $http.post(UrlService.build(`${RestURLs.datasetUrl}/favorite/${dataset.id}`, {
			unset: dataset.favorite,
		}));
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
		return $http.get(`${RestURLs.datasetUrl}/encodings`, { failSilently: true })
			.then(response => response.data);
	}

	/**
	 * @ngdoc method
	 * @name getRelatedPreparations
	 * @methodOf data-prep.services.dataset.service:DatasetRestService
	 * @description Get the related preparation list for a given dataset
	 * @returns {Promise} The GET promise
	 */
	function getRelatedPreparations(datasetId) {
		return $http.get(`${RestURLs.datasetUrl}/${datasetId}/preparations`)
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
		return $http.get(`${RestURLs.datasetUrl}/${datasetId}/compatiblepreparations`)
			.then(response => response.data);
	}
}
