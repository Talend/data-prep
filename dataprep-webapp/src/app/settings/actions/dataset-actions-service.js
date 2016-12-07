/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class DatasetActionsService {
	constructor($stateParams, state, DatasetService, MessageService,
				StateService, StorageService, TalendConfirmService) {
		'ngInject';
		this.$stateParams = $stateParams;
		this.state = state;
		this.DatasetService = DatasetService;
		this.MessageService = MessageService;
		this.StateService = StateService;
		this.StorageService = StorageService;
		this.TalendConfirmService = TalendConfirmService;
	}

	displaySuccess(messageKey, dataset) {
		this.MessageService.success(
			`${messageKey}_TITLE`,
			messageKey,
			dataset && { type: 'dataset', name: dataset.name }
		);
	}

	dispatch(action) {
		switch (action.type) {
		case '@@dataset/DISPLAY_MODE':
			this.StateService.setDatasetsDisplayMode(action.payload.mode);
			break;
		case '@@dataset/SORT': {
			const oldSort = this.state.inventory.datasetsSort;
			const oldOrder = this.state.inventory.datasetsOrder;

			const { sortBy, sortDesc } = action.payload;
			const sortOrder = sortDesc ? 'desc' : 'asc';

			this.StateService.setDatasetsSortFromIds(sortBy, sortOrder);

			this.DatasetService.init()
				.then(() => this.StorageService.setDatasetsSort(sortBy))
				.then(() => this.StorageService.setDatasetsOrder(sortOrder))
				.catch(() => {
					this.StateService.setDatasetsSortFromIds(oldSort.id, oldOrder.id);
				});
			break;
		}
		case '@@dataset/CREATE':
			this.StateService.togglePreparationCreator();
			break;
		case '@@dataset/DATASET_FETCH':
			this.StateService.setPreviousRoute('nav.index.datasets');
			this.StateService.setFetchingInventoryDatasets(true);
			this.DatasetService.init().then(() => {
				this.StateService.setFetchingInventoryDatasets(false);
			});
			break;
		//case '@@dataset/COPY_MOVE':
		//	this.StateService.toggleCopyMovePreparation(
		//		this.state.inventory.folder.metadata,
		//		action.payload.model
		//	);
		//	break;
		//case '@@dataset/EDIT':
		//case '@@dataset/CANCEL_EDIT': {
		//	const args = action.payload.args.concat(action.payload.model);
		//	this.StateService[action.payload.method].apply(null, args);
		//	break;
		//}
		//case '@@dataset/SUBMIT_EDIT': {
		//	const newName = action.payload.value;
		//	const cleanName = newName && newName.trim();
		//	const model = action.payload.model;
		//	const type = action.payload.args[0];
		//
		//	this.StateService.disableInventoryEdit(type, model);
		//	if (cleanName && cleanName !== model.name) {
		//		const nameEdition = type === 'folder' ?
		//			this.FolderService.rename(model.id, cleanName) :
		//			this.PreparationService.setName(model.id, cleanName);
		//		nameEdition.then(() => this.refreshCurrentFolder());
		//	}
		//	break;
		//}
		//case '@@preparation/REMOVE': {
		//	const preparation = action.payload.model;
		//	this.TalendConfirmService
		//		.confirm(
		//			{ disableEnter: true },
		//			['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'],
		//			{ type: 'preparation', name: preparation.name }
		//		)
		//		.then(() => this.PreparationService.delete(preparation))
		//		.then(() => this.refreshCurrentFolder())
		//		.then(() => this.displaySuccess('REMOVE_SUCCESS', preparation));
		//	break;
		//}
		}
	}
}
