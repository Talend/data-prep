/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { HOME_DATASETS_ROUTE } from '../../index-route';

const BLOCKING_ACTION_TYPES = [
	'@@dataset/SORT',
	'@@dataset/CLONE',
	'@@dataset/FAVORITE',
	'@@dataset/DATASET_FETCH',
];

export default class DatasetActionsService {
	constructor($document, $stateParams, state, StateService, StorageService, DatasetService,
				ImportService, UploadWorkflowService, MessageService, TalendConfirmService) {
		'ngInject';
		this.$document = $document;
		this.$stateParams = $stateParams;
		this.state = state;
		this.DatasetService = DatasetService;
		this.ImportService = ImportService;
		this.MessageService = MessageService;
		this.StateService = StateService;
		this.StorageService = StorageService;
		this.TalendConfirmService = TalendConfirmService;
		this.UploadWorkflowService = UploadWorkflowService;

		this.renamingList = [];
	}

	dispatch(action) {
		if (BLOCKING_ACTION_TYPES.includes(action.type)) {
			if (this.state.inventory.isFetchingDatasets) {
				return;
			}
			// all blocking action types requiring a loader
			this.StateService.setFetchingInventoryDatasets(true);
		}
		switch (action.type) {
		case '@@dataset/SORT':
		case '@@dataset/FAVORITE': {
			this.DatasetService[action.payload.method](action.payload)
				.finally(() => this.StateService.setFetchingInventoryDatasets(false));
			break;
		}
		case '@@dataset/DATASET_FETCH':
			this.StateService.setPreviousRoute(HOME_DATASETS_ROUTE);
			this.DatasetService
				.init()
				.finally(() => this.StateService.setFetchingInventoryDatasets(false));
			break;
		case '@@dataset/SUBMIT_EDIT': {
			const newName = action.payload.value;
			const cleanName = newName && newName.trim();
			const dataset = action.payload.model;

			this.StateService.disableInventoryEdit(dataset);
			if (cleanName && cleanName !== dataset.name) {
				if (this.renamingList.indexOf(dataset) > -1) {
					this.MessageService.warning(
						'DATASET_CURRENTLY_RENAMING_TITLE',
						'DATASET_CURRENTLY_RENAMING'
					);
				}
				else if (this.DatasetService.getDatasetByName(cleanName)) {
					this.MessageService.error(
						'DATASET_NAME_ALREADY_USED_TITLE',
						'DATASET_NAME_ALREADY_USED'
					);
				}
				else {
					this.renamingList.push(dataset);

					this.DatasetService.rename(dataset.model, cleanName)
						.then(() => {
							this.MessageService.success(
								'DATASET_RENAME_SUCCESS_TITLE',
								'DATASET_RENAME_SUCCESS'
							);
						})
						.finally(() => {
							const index = this.renamingList.indexOf(dataset);
							if (index > -1) {
								this.renamingList.splice(index, 1);
							}
						});
				}
			}
			break;
		}
		case '@@dataset/REMOVE': {
			const dataset = action.payload.model;
			this.TalendConfirmService
				.confirm(
					['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'],
					{ type: 'dataset', name: dataset.name }
				)
				.then(() => this.DatasetService.delete(dataset))
				.then(() => this.MessageService.success(
					'DATASET_REMOVE_SUCCESS_TITLE',
					'REMOVE_SUCCESS',
					{ type: 'dataset', name: dataset.name }
				));
			break;
		}
		case '@@dataset/CLONE': {
			const dataset = action.payload.model;
			this.DatasetService.clone(dataset)
				.then(() => this.MessageService.success('COPY_SUCCESS_TITLE', 'COPY_SUCCESS'))
				.finally(() => this.StateService.setFetchingInventoryDatasets(false));
			break;
		}
		case '@@dataset/UPDATE': {
			const { payload } = action;
			this.$document[0].getElementById('inputUpdateDataset').click();
			this.StateService.setDatasetToUpdate(payload.model);
			break;
		}
		case '@@dataset/TCOMP_EDIT': {
			const { payload } = action;
			this.StateService.setCurrentImportItem(payload.model);
			this.StateService.showImport();
			break;
		}
		case '@@dataset/CREATE': {
			if (action.payload.locationType) {
				this.ImportService.startImport(action.payload);
			}
			else {
				const importTypes = action.payload.items;
				const defaultImport = importTypes.find(type => type.defaultImport) || importTypes[0];
				this.ImportService.startImport(defaultImport);
			}
			break;
		}
		case '@@dataset/OPEN': {
			this.UploadWorkflowService.openDataset(action.payload.model, action.event);
			break;
		}
		}
	}
}
