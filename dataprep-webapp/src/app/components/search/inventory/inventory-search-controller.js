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

    constructor($q, $state, UploadWorkflowService, InventoryService, FolderService, PreparationService, DocumentationService) {
        'ngInject';
        this.$q = $q;
        this.$state = $state;
        this.uploadWorkflowService = UploadWorkflowService;
        this.folderService = FolderService;
        this.preparationService = PreparationService;
        this.inventoryService = InventoryService;
        this.documentationService = DocumentationService;
    }

    /**
     * @ngdoc method
     * @name search
     * @methodOf data-prep.inventory-search.controller:InventorySearchCtrl
     * @description Search based on searchInput
     */
    search (searchInput) {
        this.results = null;
        this.docResults = null;
        this.currentInput = searchInput;

        if(searchInput){
            const inventoryPromise = this.inventoryService.search(searchInput)
                .then((response)=> {
                    this.results = searchInput === this.currentInput && response;
                });

            const docPromise = this.documentationService.search(searchInput)
                .then((response)=> {
                    this.docResults = searchInput === this.currentInput && response;
                });

            return this.$q.all([inventoryPromise, docPromise]);
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

