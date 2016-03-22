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
 * @name data-prep.inventory-search.controller:InventorySearchCtrl
 * @description InventorySearchCtrl controller.
 * @requires data-prep.services.inventory.service:InventoryService
 * @requires data-prep.services.datasetWorkflowService:UploadWorkflowService
 * @requires data-prep.services.folder.service:FolderService
 * @requires data-prep.services.preparation.service:PreparationService
 *
 */
class InventorySearchCtrl {

    constructor($state, UploadWorkflowService, InventoryService, FolderService, PreparationService, DocumentationService) {
        'ngInject';
        this.uploadWorkflowService = UploadWorkflowService;
        this.folderService = FolderService;
        this.preparationService = PreparationService;
        this.inventoryService = InventoryService;
        this.$state = $state;
        this.documentationService = DocumentationService;
    }

    /**
     * @ngdoc method
     * @name search
     * @methodOf data-prep.inventory-search.controller:InventorySearchCtrl
     * @description Search based on searchInput
     */
    search (searchInput) {
        if(searchInput){
            this.inventoryService.search(searchInput)
                .then((response)=> {
                    this.results = response;
                });

            this.documentationService.search(searchInput)
                .then((response)=> {
                    this.docResults = response;
                });
        }
    }

    /**
     * @ngdoc method
     * @name goToFolder
     * @methodOf data-prep.inventory-search.controller:InventorySearchCtrl
     * @description go to a folder
     */
    goToFolder (stateString, options) {
        this.$state.go(stateString, options);
    }
}

export default InventorySearchCtrl;

