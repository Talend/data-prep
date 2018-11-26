/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { HOME_PREPARATIONS_ROUTE } from '../../index-route';

const BLOCKING_ACTION_TYPES = [
	'@@preparation/SORT',
	'@@preparation/FOLDER_REMOVE',
	'@@preparation/FOLDER_FETCH',
];

export default class PreparationActionsService {
	constructor($stateParams, $translate, state, FolderService, MessageService, PreparationService,
				StateService, StorageService, ConfirmService) {
		'ngInject';
		this.$stateParams = $stateParams;
		this.state = state;
		this.FolderService = FolderService;
		this.MessageService = MessageService;
		this.PreparationService = PreparationService;
		this.StateService = StateService;
		this.StorageService = StorageService;
		this.ConfirmService = ConfirmService;

		this.i18n = {
			PREPARATION: $translate.instant('PREPARATION'),
		};
	}

	dispatch(action) {
		if (BLOCKING_ACTION_TYPES.includes(action.type)) {
			if (this.state.inventory.isFetchingPreparations) {
				return;
			}
			// all blocking action types requiring a loader
			this.StateService.setFetchingInventoryPreparations(true);
		}
		switch (action.type) {
		case '@@preparation/CREATE':
			this.StateService[action.payload.method](action.payload);
			break;
		case '@@preparation/FOLDER_REMOVE':
		case '@@preparation/SORT': {
			this.FolderService[action.payload.method](action.payload)
				.finally(() => this.StateService.setFetchingInventoryPreparations(false));
			break;
		}
		case '@@preparation/FOLDER_FETCH': {
			const folderId = this.$stateParams.folderId;
			this.StateService.setPreviousRoute(HOME_PREPARATIONS_ROUTE, { folderId });
			this.FolderService
				.init(folderId)
				.finally(() => this.StateService.setFetchingInventoryPreparations(false));
			break;
		}
		case '@@preparation/COPY_MOVE':
			this.StateService.toggleCopyMovePreparation(
				this.state.inventory.folder.metadata,
				action.payload.model
			);
			this.StateService.setCopyMoveTreeLoading(true);
			this.FolderService.tree()
				.then(tree => this.StateService.setCopyMoveTree(tree))
				.finally(() => {
					this.StateService.setCopyMoveTreeLoading(false);
				});
			break;
		case '@@preparation/SUBMIT_EDIT': {
			const newName = action.payload.value;
			const cleanName = newName && newName.trim();
			const model = action.payload.model;
			const type = model.type;

			this.StateService.disableInventoryEdit(model);
			if (cleanName && cleanName !== model.name) {
				const nameEdition = type === 'folder' ?
					this.FolderService.rename(model.id, cleanName) :
					this.PreparationService.setName(model.id, cleanName);
				nameEdition.then(() => this.FolderService.refreshCurrentFolder());
			}
			break;
		}
		case '@@preparation/REMOVE': {
			const preparation = action.payload.model;
			this.ConfirmService
				.confirm(
					'DELETE_PERMANENTLY_TITLE',
					['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'],
					{ type: this.i18n.PREPARATION, name: preparation.name },
					true
				)
				.then(() => this.PreparationService.delete(preparation))
				.then(() => this.FolderService.refreshCurrentFolder())
				.then(() => this.MessageService.success(
					'PREPARATION_REMOVE_SUCCESS_TITLE',
					'REMOVE_SUCCESS',
					{ type: this.i18n.PREPARATION, name: preparation.name }
				));
			break;
		}
		}
	}
}
