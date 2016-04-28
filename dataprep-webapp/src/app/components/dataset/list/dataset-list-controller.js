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
 * @requires data-prep.services.preparation.service:PreparationService
 *
 * @requires data-prep.services.datasetWorkflowService.service:UploadWorkflowService
 * @requires data-prep.services.datasetWorkflowService.service:UpdateWorkflowService
 *
 * @requires talend.widget.service:TalendConfirmService
 * @requires data-prep.services.utils.service:MessageService
 */
export default class DatasetListCtrl {
    constructor(state, StateService,                                // app state
                DatasetService, PreparationService,                 // inventory
                UploadWorkflowService, UpdateWorkflowService,       // inventory workflow
                TalendConfirmService, MessageService) {             // utils
        'ngInject';

        this.state = state;
        this.StateService = StateService;
        this.DatasetService = DatasetService;
        this.PreparationService = PreparationService;
        this.UploadWorkflowService = UploadWorkflowService;
        this.UpdateWorkflowService = UpdateWorkflowService;
        this.TalendConfirmService = TalendConfirmService;
        this.MessageService = MessageService;

        //TODO refacto inventory item to take function and remove this
        this.uploadUpdatedDatasetFile = this.uploadUpdatedDatasetFile.bind(this);
        this.processCertification = this.processCertification.bind(this);
        this.remove = this.remove.bind(this);
        this.rename = this.rename.bind(this);
        this.clone = this.clone.bind(this);

        this.renamingList = [];
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
                { type: 'dataset', name: dataset.name }
            ));
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
            this.MessageService.error('DATASET_NAME_ALREADY_USED_TITLE', 'DATASET_NAME_ALREADY_USED');
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
            .processCertification(dataset);
    }

    /**
     * @ngdoc method
     * @name clone
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description makes a copy of a dataset
     * @param {object} dataset to move
     */
    clone(dataset) {
        return this.DatasetService.clone(dataset)
            .then(() => this.MessageService.success('COPY_SUCCESS_TITLE', 'COPY_SUCCESS'));
    }
}
