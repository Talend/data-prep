/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import moment from 'moment';

/**
 * @ngdoc service
 * @name data-prep.services.dataset.service:DatasetListService
 * @description Dataset grid service. This service holds the dataset list like a cache and consume DatasetRestService to access to the REST api<br/>
 * <b style="color: red;">WARNING : do NOT use this service directly.
 * {@link data-prep.services.dataset.service:DatasetService DatasetService} must be the only entry point for datasets</b>
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.dataset.service:DatasetRestService
 * @requires data-prep.services.state.service:StateService
 */
export default function DatasetListService($q, state, DatasetRestService, StateService) {
	'ngInject';

	let deferredCancel;
	let datasetsPromise;

	return {
		adaptDatasets,
		getDatasetActions,
		getDatasetIcon,
		refreshDatasets,
		getDatasetsPromise,
		hasDatasetsPromise,
		getClassName,
		getStatusActions,

		create,
		clone,
		update,
		delete: deleteDataset,

		toggleFavorite,
	};

	/**
	 * @ngdoc method
	 * @name cancelPendingGetRequest
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @description Cancel the pending datasets list GET request
	 */
	function cancelPendingGetRequest() {
		if (datasetsPromise) {
			deferredCancel.resolve('user cancel');
			datasetsPromise = null;
		}
	}

	/**
	 * @ngdoc method
	 * @name refreshDatasets
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @description Refresh datasets list
	 * @returns {promise} The pending GET promise
	 */
	function refreshDatasets() {
		cancelPendingGetRequest();
		const sort = state.inventory.datasets.sort.field;
		const order = state.inventory.datasets.sort.isDescending ? 'desc' : 'asc';

		deferredCancel = $q.defer();
		datasetsPromise = DatasetRestService.getDatasets(sort, order, deferredCancel)
			.then((res) => {
				StateService.setDatasets(this.adaptDatasets(res.data));
				return res.data;
			})
			.catch(() => {
				StateService.setDatasets([]);
				return [];
			})
			.finally(() => datasetsPromise = null);
		return datasetsPromise;
	}

	/**
	 * @ngdoc method
	 * @name adaptDatasets
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @description Adapt datasets for UI components
	 * @param {object[]} datasets The datasets
	 * @returns {object[]} The adapted datasets
	 */
	function adaptDatasets(datasets) {
		return datasets.map(item => ({
			id: item.id,
			type: 'dataset',
			name: item.name,
			author: item.owner && item.owner.displayName,
			creationDate: moment(item.created).fromNow(),
			lastModificationDate: moment(item.lastModificationDate).fromNow(),
			nbRecords: item.records,
			displayMode: 'text',
			className: this.getClassName(item).join(' '),
			icon: this.getDatasetIcon(item),
			actions: this.getDatasetActions(item),
			statusActions: this.getStatusActions(),
			preparations: item.preparations,
			model: item,
		}));
	}

	/**
	 * @ngdoc method
	 * @name getClassName
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @description Get the class names to apply on the dataset item display
	 * @param {object} dataset The dataset
	 * @returns {string} the class names
	 */
	function getClassName(dataset) {
		return dataset.favorite ?
			['list-item-favorite'] :
			[];
	}

	/**
	 * @ngdoc method
	 * @name getStatusActions
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @description Returns dataset status actions
	 * @returns {string[]} The available dataset status actions
	 */
	function getStatusActions() {
		return ['dataset:favorite'];
	}

	/**
	 * @ngdoc method
	 * @name getDatasetActions
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @description Return dataset actions
	 * @param {object} item The dataset
	 * @returns {string[]} The available actions
	 */
	function getDatasetActions(item) {
		const actions = [
			'inventory:edit',
			'dataset:update',
			'dataset:clone',
			'dataset:remove',
		];
		if (item.preparations && item.preparations.length > 0) {
			actions.splice(1, 0, 'list:dataset:preparations');
		}
		return actions;
	}

	/**
	 * @ngdoc method
	 * @name getDatasetIcon
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @description Adapt dataset icon
	 * @param {object} item The dataset
	 * @returns {string} The icon name
	 */
	function getDatasetIcon(item) {
		switch (item.type) {
		case 'text/csv': return 'talend-file-csv-o';
		case 'application/vnd.ms-excel': return 'talend-file-xls-o';
		}

		return 'talend-file-o';
	}

	/**
	 * @ngdoc method
	 * @name clone
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @param {object} dataset The dataset to clone
	 * @description Clone a dataset from backend and refresh its internal list
	 * @returns {promise} The pending GET promise
	 */
	function clone(dataset) {
		const promise = DatasetRestService.clone(dataset);
		promise.then(() => this.refreshDatasets());

		return promise;
	}

	/**
	 * @ngdoc method
	 * @name create
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @param {object} parameters The import parameters to import
	 * @param {object} file The file imported from local
	 * @param {string} contentType The request Content-Type
	 * @description Import a remote dataset from backend and refresh its internal list
	 * @returns {promise} The pending POST promise
	 */
	function create(parameters, contentType, file) {
		const promise = DatasetRestService.create(parameters, contentType, file);

		// The appended promise is not returned because DatasetRestService.import return a $upload object with progress function
		// which is used by the caller
		promise.then(() => this.refreshDatasets());

		return promise;
	}

	/**
	 * @ngdoc method
	 * @name update
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @param {object} dataset The dataset to delete
	 * @param {object} parameters The update parameters
	 * @description Update a dataset from backend and refresh its internal list
	 * @returns {promise} The pending POST promise
	 */
	function update(dataset, parameters) {
		const promise = DatasetRestService.update(dataset, parameters);

		// The appended promise is not returned because DatasetRestService.import return a $upload object with progress function
		// which is used by the caller
		promise.then(() => this.refreshDatasets());

		return promise;
	}

	/**
	 * @ngdoc method
	 * @name delete
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @param {object} dataset The dataset to delete
	 * @description Delete a dataset from backend and from its internal list
	 * @returns {promise} The pending DELETE promise
	 */
	function deleteDataset(dataset) {
		return DatasetRestService.delete(dataset)
			.then(function () {
				StateService.removeDataset(dataset);
			});
	}

	/**
	 * @ngdoc method
	 * @name getDatasetsPromise
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @description Return resolved or unresolved promise that returns the most updated datasetsList
	 * @returns {promise} Promise that resolves datasetsList
	 */
	function getDatasetsPromise() {
		return datasetsPromise || this.refreshDatasets();
	}

	/**
	 * @ngdoc method
	 * @name hasDatasetsPromise
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @description Check if datasetsPromise is true or not
	 * @returns {promise} datasetsPromise
	 */
	function hasDatasetsPromise() {
		return datasetsPromise;
	}

	/**
	 * @ngdoc method
	 * @name toggleFavorite
	 * @methodOf data-prep.services.dataset.service:DatasetListService
	 * @param {object} model The target dataset to set or unset favorite
	 * @description Set or Unset the dataset as favorite
	 * @returns {promise} The pending POST promise
	 */
	function toggleFavorite({ model }) {
		return DatasetRestService.toggleFavorite(model)
			.then(() => model.favorite = !model.favorite)
			.then(() => this.refreshDatasets());
	}
}
