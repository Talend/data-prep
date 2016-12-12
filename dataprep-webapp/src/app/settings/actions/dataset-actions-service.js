/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class DatasetActionsService {
	constructor($stateParams, state, DatasetService,
				MessageService, StateService, StorageService) {
		'ngInject';
		this.$stateParams = $stateParams;
		this.state = state;
		this.DatasetService = DatasetService;
		this.MessageService = MessageService;
		this.StateService = StateService;
		this.StorageService = StorageService;

		this.renamingList = [];
	}

	dispatch(action) {
		switch (action.type) {
		case '@@dataset/SORT': {
			const oldSort = this.state.inventory.datasetsSort;
			const oldOrder = this.state.inventory.datasetsOrder;

			const { sortBy, sortDesc } = action.payload;
			const sortOrder = sortDesc ? 'desc' : 'asc';

			this.StateService.setDatasetsSortFromIds(sortBy, sortOrder);

			this.DatasetService.refreshDatasets()
				.then(() => this.StorageService.setDatasetsSort(sortBy))
				.then(() => this.StorageService.setDatasetsOrder(sortOrder))
				.catch(() => {
					this.StateService.setDatasetsSortFromIds(oldSort.id, oldOrder.id);
				});
			break;
		}
		case '@@dataset/DATASET_FETCH':
			this.StateService.setPreviousRoute('nav.index.datasets');
			this.StateService.setFetchingInventoryDatasets(true);
			this.DatasetService.init().then(() => {
				this.StateService.setFetchingInventoryDatasets(false);
			});
			break;
		case '@@dataset/SUBMIT_EDIT': {
			const newName = action.payload.value;
			const cleanName = newName && newName.trim();
			const model = action.payload.model;

			this.StateService.disableInventoryEdit(model);
			if (cleanName && cleanName !== model.name) {

				if (this.renamingList.indexOf(model) > -1) {
					this.MessageService.warning(
						'DATASET_CURRENTLY_RENAMING_TITLE',
						'DATASET_CURRENTLY_RENAMING'
					);
					return;
				}

				if (this.DatasetService.getDatasetByName(cleanName)) {
					this.MessageService.error(
						'DATASET_NAME_ALREADY_USED_TITLE',
						'DATASET_NAME_ALREADY_USED'
					);
					return;
				}

				this.renamingList.push(model);

				return this.DatasetService.rename(model.model, cleanName)
					.then(() => {
						this.MessageService.success(
							'DATASET_RENAME_SUCCESS_TITLE',
							'DATASET_RENAME_SUCCESS'
						);
					})
					.finally(() => {
						const index = this.renamingList.indexOf(model);
						this.renamingList.splice(index, 1);
					});
			}
			break;
		}
		}

	}
}
