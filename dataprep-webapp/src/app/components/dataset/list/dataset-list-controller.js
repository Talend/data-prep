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
 * @name data-prep.dataset-list.controller:DatasetListCtrl
 * @description Dataset list controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.dataset.service:DatasetService
 *
 * @requires data-prep.services.datasetWorkflowService.service:UploadWorkflowService
 * @requires data-prep.services.datasetWorkflowService.service:UpdateWorkflowService
 *
 * @requires talend.widget.service:TalendConfirmService
 * @requires data-prep.services.utils.service:MessageService
 * @requires data-prep.services.folder.service:FolderService
 * @requires data-prep.services.preparation.service:PreparationService
 */
export default class DatasetListCtrl {
    constructor($stateParams, $state,                               // ui-router
                state, StateService,                                // app state
                DatasetService, FolderService, PreparationService,  // inventory
                UploadWorkflowService, UpdateWorkflowService,       // inventory workflow
                TalendConfirmService, MessageService) {             // utils
        'ngInject';

        this.$stateParams = $stateParams;
        this.$state = $state;
        this.state = state;
        this.StateService = StateService;
        this.DatasetService = DatasetService;
        this.PreparationService = PreparationService;
        this.FolderService = FolderService;
        this.UploadWorkflowService = UploadWorkflowService;
        this.UpdateWorkflowService = UpdateWorkflowService;
        this.TalendConfirmService = TalendConfirmService;
        this.MessageService = MessageService;

        //TODO refacto inventory item to take function and remove this
        this.uploadUpdatedDatasetFile = this.uploadUpdatedDatasetFile.bind(this);
        this.openFolderSelection = this.openFolderSelection.bind(this);
        this.processCertification = this.processCertification.bind(this);
        this.remove = this.remove.bind(this);
        this.rename = this.rename.bind(this);
        this.goToFolder = this.goToFolder.bind(this);
        this.removeFolder = this.removeFolder.bind(this);
        this.renameFolder = this.renameFolder.bind(this);

        this.renamingList = [];
    }

    $onInit() {
        //Load folders content
        if (this.$stateParams.folderPath) {
            const folderDefinition = {
                path: this.$stateParams.folderPath,
                name: _.chain(this.$stateParams.folderPath)
                    .split('/')
                    .filter((part) => part)
                    .last()
                    .value(),
            };
            this.FolderService
                .getContent(folderDefinition)
                .catch(() => this.$state.go('nav.index.datasets', {folderPath: ''}));
        }
        else {
            this.FolderService.getContent();
        }
    }

    /**
     * @ngdoc method
     * @name uploadUpdatedDatasetFile
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description updates the existing dataset with the uploaded one
     * @param {object} dataset The dataset to update
     */
    uploadUpdatedDatasetFile(dataset) {
        this.UpdateWorkflowService.updateDataset(this.updateDatasetFile[0], dataset);
    }

    /**
     * @ngdoc method
     * @name remove
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description Delete a dataset
     * @param {object} dataset The dataset to delete
     */
    remove(dataset) {
        this.TalendConfirmService.confirm(
            {disableEnter: true},
            ['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'],
            {type: 'dataset', name: dataset.name}
            )
            .then(() => this.DatasetService.delete(dataset))
            .then(() => this.MessageService.success(
                'REMOVE_SUCCESS_TITLE',
                'REMOVE_SUCCESS',
                {type: 'dataset', name: dataset.name}
            ))
            .then(() => this.FolderService.getContent(this.state.inventory.currentFolder));
    }

    /**
     * @ngdoc method
     * @name rename
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @param {object} dataset The dataset to rename
     * @param {string} name The new name
     * @description Rename a dataset
     */
    rename(dataset, name) {
        const cleanName = name ? name.trim().toLowerCase() : '';
        if (!cleanName) {
            return;
        }

        if (this.renamingList.indexOf(dataset) > -1) {
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

        this.renamingList.push(dataset);

        return this.DatasetService.rename(dataset, name)
            .then(() => {
                this.MessageService.success(
                    'DATASET_RENAME_SUCCESS_TITLE',
                    'DATASET_RENAME_SUCCESS'
                )
            })
            .finally(() => {
                const index = this.renamingList.indexOf(dataset);
                this.renamingList.splice(index, 1);
            });
    }

    /**
     * @ngdoc method
     * @name processCertification
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description Ask certification for a dataset
     * @param {object[]} dataset Ask certification for the dataset
     */
    processCertification(dataset) {
        this.DatasetService
            .processCertification(dataset)
            .then(() => this.FolderService.getContent(this.state.inventory.currentFolder));
    }

    /**
     * @ngdoc method
     * @name goToFolder
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description Redirect to folder
     * @param {object} folder The target folder
     */
    goToFolder(folder) {
        this.$state.go('nav.index.datasets', {folderPath: folder.path});
    }

    /**
     * @ngdoc method
     * @name renameFolder
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
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
            .then(() => this.FolderService.getContent(this.state.inventory.currentFolder));
    }

    /**
     * @ngdoc method
     * @name removeFolder
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description Remove a folder
     * @param {object} folder The folder to remove
     */
    removeFolder(folder) {
        this.FolderService.remove(folder.path)
            .then(() => this.FolderService.getContent(this.state.inventory.currentFolder));
    }


    /**
     * @ngdoc method
     * @name openFolderSelection
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description Remove a folder
     * @param {object} dataset The dataset to clone or copy
     */
    openFolderSelection(dataset) {
        this.datasetCopyVisibility = true;
        this.datasetToCopyMove = dataset;
    }

    /**
     * @ngdoc method
     * @name clone
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description makes a copy of a dataset
     * @param {object} dataset to move
     * @param {object} destinationFolder destination folder
     * @param {string} name of the new dataset
     */
    clone(dataset, destinationFolder, name) {
        return this.DatasetService.clone(dataset, destinationFolder, name)
            .then(() => this.MessageService.success('COPY_SUCCESS_TITLE', 'COPY_SUCCESS'))
            .then(() => {
                this.datasetCopyVisibility = false
            })
            .then(() => this.FolderService.getContent(this.state.inventory.currentFolder));
    }

    /**
     * @ngdoc method
     * @name move
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description moves a dataset from 1 folder to another
     * @param {object} dataset to move
     * @param {object} destinationFolder destination folder
     * @param {string} name of the new dataset
     **/
    move(dataset, destinationFolder, name) {
        return this.DatasetService.move(dataset, destinationFolder, name)
            .then(() => this.MessageService.success('MOVE_SUCCESS_TITLE', 'MOVE_SUCCESS'))
            .then(() => {
                this.datasetCopyVisibility = false
            })
            .then(() => this.FolderService.getContent(this.state.inventory.currentFolder));
    }
}
