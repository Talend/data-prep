/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.preparation-list.controller:PreparationListCtrl
 * @description Preparation list controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.folder.service:FolderService
 * @requires data-prep.services.preparation.service:PreparationService
 * @requires data-prep.services.utils.service:MessageService
 * @requires talend.widget.service:TalendConfirmService
 */
export default class PreparationListCtrl {
    constructor($state, $stateParams, state, StateService,
                FolderService, PreparationService, MessageService, TalendConfirmService) {
        'ngInject';

        this.$state = $state;
        this.$stateParams = $stateParams;
        this.state = state;
        this.StateService = StateService;
        this.FolderService = FolderService;
        this.PreparationService = PreparationService;
        this.MessageService = MessageService;
        this.TalendConfirmService = TalendConfirmService;

        this.remove = this.remove.bind(this);
        this.rename = this.rename.bind(this);
        this.openCopyMoveModal = this.openCopyMoveModal.bind(this);
        this.copy = this.copy.bind(this);
        this.remove = this.remove.bind(this);
        this.goToFolder = this.goToFolder.bind(this);
        this.renameFolder = this.renameFolder.bind(this);
        this.removeFolder = this.removeFolder.bind(this);
    }

    /**
     * @ngdoc method
     * @name delete
     * @methodOf data-prep.preparation-list.controller:PreparationListCtrl
     * @param {object} preparation - the preparation to delete
     * @description Delete a preparation
     */
    remove(preparation) {
        this.TalendConfirmService.confirm({ disableEnter: true }, ['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'], {
                type: 'preparation',
                name: preparation.name,
            })
            .then(() => this.PreparationService.delete(preparation))
            .then(() => {
                this.FolderService.refreshContent(this.state.inventory.folder.metadata.path)
            })
            .then(() => {
                this.MessageService.success(
                    'REMOVE_SUCCESS_TITLE',
                    'REMOVE_SUCCESS',
                    { type: 'preparation', name: preparation.name }
                );
            });
    }

    /**
     * @ngdoc method
     * @name rename
     * @methodOf data-prep.preparation.controller:PreparationListCtrl
     * @param {object} preparation The preparation to rename
     * @param {string} newName The new name for the given preparation
     * @description Trigger backend call to update preparation name
     */
    rename(preparation, newName) {
        var cleanName = newName ? newName.trim() : '';
        if (cleanName) {
            return this.PreparationService.setName(preparation.id, newName)
                .then(() => {
                    this.FolderService.refreshContent(this.state.inventory.folder.metadata.path)
                })
                .then(() => {
                    this.MessageService.success(
                        'PREPARATION_RENAME_SUCCESS_TITLE',
                        'PREPARATION_RENAME_SUCCESS'
                    );
                });
        }
    }

    /**
     * @ngdoc method
     * @name openCopyMoveModal
     * @methodOf data-prep.preparation.controller:PreparationListCtrl
     * @param {object} preparation The preparation to copy/move
     * @description Trigger backend call to clone preparation
     */
    openCopyMoveModal(preparation) {
        this.preparationToCopyMove = preparation;
        this.copyMoveModal = true;
    }

    /**
     * @ngdoc method
     * @name copy
     * @methodOf data-prep.preparation.controller:PreparationListCtrl
     * @param {object} preparation The preparation to clone
     * @param {object} destination The destination folder
     * @param {string} name The new preparation name
     * @description Trigger backend call to clone preparation
     */
    copy(preparation, destination, name) {
        return this.PreparationService.copy(preparation.id, destination.path, name)
            .then(() => {
                this.MessageService.success(
                    'PREPARATION_COPYING_SUCCESS_TITLE',
                    'PREPARATION_COPYING_SUCCESS'
                );
            })
            .then(() => {
                this.FolderService.refreshContent(this.state.inventory.folder.metadata.path)
            })
            .then(() => {
                this.copyMoveModal = false
            });
    }

    /**
     * @ngdoc method
     * @name move
     * @methodOf data-prep.preparation.controller:PreparationListCtrl
     * @param {object} preparation The preparation to clone
     * @param {object} destination The destination folder
     * @param {string} name The new preparation name
     * @description Trigger backend call to clone preparation
     */
    move(preparation, destination, name) {
        const currentPath = this.state.inventory.folder.metadata.path;
        return this.PreparationService.move( preparation.id, currentPath, destination.path, name)
            .then(() => {
                this.MessageService.success(
                    'PREPARATION_MOVING_SUCCESS_TITLE',
                    'PREPARATION_MOVING_SUCCESS'
                );
            })
            .then(() => {
                this.FolderService.refreshContent(currentPath)
            })
            .then(() => {
                this.copyMoveModal = false
            });
    }

    //------------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------------FOLDER----------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name goToFolder
     * @methodOf data-prep.preparation.controller:PreparationListCtrl
     * @description Redirect to folder
     * @param {object} folder The target folder
     */
    goToFolder(folder) {
        this.$state.go('nav.index.preparations', { folderPath: folder.path });
    }

    /**
     * @ngdoc method
     * @name renameFolder
     * @methodOf data-prep.preparation.controller:PreparationListCtrl
     * @description Rename a folder
     * @param {object} folder the folder to rename
     * @param {string} newName the new last part of the path
     */
    renameFolder(folder, newName) {
        const path = folder.path;
        const lastSlashIndex = path.lastIndexOf('/');
        const parentFolder = path.substring(0, lastSlashIndex);
        const newPath = `${parentFolder}/${newName}`;

        this.FolderService.rename(path, newPath)
            .then(() => {
                this.FolderService.refreshContent(this.state.inventory.folder.metadata.path)
            });
    }

    /**
     * @ngdoc method
     * @name removeFolder
     * @methodOf data-prep.preparation.controller:PreparationListCtrl
     * @description Remove a folder
     * @param {object} folder The folder to remove
     */
    removeFolder(folder) {
        this.FolderService.remove(folder.path)
            .then(() => {
                this.FolderService.refreshContent(this.state.inventory.folder.metadata.path)
            });
    }
}