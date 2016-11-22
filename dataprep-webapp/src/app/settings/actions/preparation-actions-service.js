/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class PreparationActionsService {
	constructor($stateParams, state, FolderService, MessageService, PreparationService,
				StateService, StorageService, TalendConfirmService) {
		'ngInject';
		this.$stateParams = $stateParams;
		this.state = state;
		this.FolderService = FolderService;
		this.MessageService = MessageService;
		this.PreparationService = PreparationService;
		this.StateService = StateService;
		this.StorageService = StorageService;
		this.TalendConfirmService = TalendConfirmService;
	}

	dispatch(action) {
		switch (action.type) {
		case '@@preparation/DISPLAY_MODE':
			this.StateService.setPreparationsDisplayMode(action.payload.mode);
			break;
		case '@@preparation/SORT': {
			const oldSort = this.state.inventory.preparationsSort;
			const oldOrder = this.state.inventory.preparationsOrder;

			const { sortBy, sortDesc } = action.payload;
			const sortOrder = sortDesc ? 'desc' : 'asc';

			this.StateService.setPreparationsSortFromIds(sortBy, sortOrder);

			this.FolderService
				.refresh(this.state.inventory.folder.metadata.id)
				.then(() => this.StorageService.setPreparationsSort(sortBy))
				.then(() => this.StorageService.setPreparationsOrder(sortOrder))
				.catch(() => {
					this.StateService.setPreparationsSortFromIds(oldSort.id, oldOrder.id);
				});
			break;
		}
		case '@@preparation/CREATE':
			this.StateService.togglePreparationCreator();
			break;
		case '@@preparation/FOLDER_FETCH': {
			const folderId = this.$stateParams.folderId;
			this.StateService.setPreviousRoute('nav.index.preparations', { folderId });
			this.StateService.setFetchingInventoryPreparations(true);
			this.FolderService
				.init(folderId)
				.then(() => this.StateService.setFetchingInventoryPreparations(false));
			break;
		}
		case '@@preparation/COPY_MOVE':
			this.StateService.toggleCopyMovePreparation(
				this.state.inventory.folder.metadata,
				action.payload.model
			);
			break;
		case '@@preparation/REMOVE': {
			const preparation = action.payload.model;
			this.TalendConfirmService
				.confirm(
					{ disableEnter: true },
					['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'],
					{ type: 'preparation', name: preparation.name }
				)
				.then(() => this.PreparationService.delete(preparation))
				.then(() => {
					this.FolderService.refresh(this.state.inventory.folder.metadata.id);
				})
				.then(() => {
					this.MessageService.success(
						'REMOVE_SUCCESS_TITLE',
						'REMOVE_SUCCESS',
						{ type: 'preparation', name: preparation.name }
					);
				});
			break;
		}
		}
	}
}
