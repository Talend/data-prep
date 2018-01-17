/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.preparation-copy-move.controller:PreparationCopyMoveCtrl
 * @description Preparation list controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.folder.service:FolderService
 * @requires data-prep.services.utils.service:MessageService
 * @requires data-prep.services.preparation.service:PreparationService
 * @requires data-prep.services.state.service:StateService
 */
export default class PreparationCopyMoveCtrl {
	constructor(state, FolderService, MessageService, PreparationService, StateService) {
		'ngInject';

		this.state = state;
		this.FolderService = FolderService;
		this.MessageService = MessageService;
		this.PreparationService = PreparationService;
		this.StateService = StateService;
	}

	/**
	 * @ngdoc method
	 * @name copy
	 * @methodOf data-prep.preparation-copy-move.controller:PreparationCopyMoveCtrl
	 * @param {object} preparation The preparation to clone
	 * @param {object} destination The destination folder
	 * @param {string} name The new preparation name
	 * @description Trigger backend call to clone preparation
	 */
	copy(preparation, destination, name) {
		const initialFolderId = this.state.home.preparations.copyMove.initialFolder.id;
		return this.PreparationService.copy(preparation.id, destination.id, name)
			.then(() => {
				this.MessageService.success(
					'PREPARATION_COPYING_SUCCESS_TITLE',
					'PREPARATION_COPYING_SUCCESS'
				);
			})
			.then(() => {
				this.FolderService.refresh(initialFolderId);
			})
			.then(() => {
				this.StateService.toggleCopyMovePreparation();
			});
	}

	/**
	 * @ngdoc method
	 * @name move
	 * @methodOf data-prep.preparation-copy-move.controller:PreparationCopyMoveCtrl
	 * @param {object} preparation The preparation to clone
	 * @param {object} destination The destination folder
	 * @param {string} name The new preparation name
	 * @description Trigger backend call to clone preparation
	 */
	move(preparation, destination, name) {
		const initialFolderId = this.state.home.preparations.copyMove.initialFolder.id;
		return this.PreparationService.move(preparation.id, initialFolderId, destination.id, name)
			.then(() => {
				this.MessageService.success(
					'PREPARATION_MOVING_SUCCESS_TITLE',
					'PREPARATION_MOVING_SUCCESS'
				);
			})
			.then(() => {
				this.FolderService.refresh(initialFolderId);
			})
			.then(() => {
				this.StateService.toggleCopyMovePreparation();
			});
	}
}
