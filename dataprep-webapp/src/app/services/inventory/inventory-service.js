/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
class InventoryService {

    constructor($q, InventoryRestService, TextFormatService) {
        'ngInject';
        this.deferredCancel = null;
        this.$q = $q;
        this.InventoryRestService = InventoryRestService;
        this.TextFormatService = TextFormatService;
    }

    /**
     * @ngdoc method
     * @name cancelPendingGetRequest
     * @description Cancel the pending search GET request
     */
    cancelPendingGetRequest() {
        if (this.deferredCancel) {
            this.deferredCancel.resolve('user cancel');
            this.deferredCancel = null;
        }
    }

    /**
     * @ngdoc method
     * @name search
     * @methodOf data-prep.services.lookup.service:InventoryService
     * @param {String} searchValue string
     * @description Search inventory items
     */
    search(searchValue) {
        this.cancelPendingGetRequest();

        this.deferredCancel = this.$q.defer();

        return this.InventoryRestService.search(searchValue, this.deferredCancel)
            .then((response) => {
                return this.addHtmlLabelsAndSort(searchValue, response.data);
            })
            .finally(() => this.deferredCancel = null);
    }

    /**
     * @ngdoc method
     * @name addHtmlLabelsAndSort
     * @param {String} searchValue string
     * @param {Object} data data to process
     * @description add html label to data based on searchValue and sort the results
     */
    addHtmlLabelsAndSort(searchValue, data) {
        let inventory_items = [];

        if (data.datasets && data.datasets.length) {
            _.each(data.datasets, function (item) {
                let itemToDisplay = {};

                itemToDisplay.inventoryType = 'dataset';
                itemToDisplay.author = item.author;
                itemToDisplay.created = item.created;
                itemToDisplay.records = item.records;
                itemToDisplay.name = item.name;
                itemToDisplay.path = item.path;
                itemToDisplay.type = item.type;
                itemToDisplay.originalItem = item;
                itemToDisplay.lastModificationDate = item.lastModificationDate;
                itemToDisplay.tooltipName = item.name;

                inventory_items.push(itemToDisplay);
            });

        }
        if (data.preparations && data.preparations.length) {
            _.each(data.preparations, function (item) {
                item.inventoryType = 'preparation';
                item.tooltipName = item.name;
            });
            inventory_items = inventory_items.concat(data.preparations);
        }
        if (data.folders && data.folders.length) {
            _.each(data.folders, function (item) {
                item.inventoryType = 'folder';
                item.tooltipName = item.name;
            });
            inventory_items = inventory_items.concat(data.folders);
        }

        return _.chain(inventory_items)
            .map((item) => {
                this.TextFormatService.highlight(item, 'name', searchValue, 'highlighted');
                return item;
            })
            .sortBy('lastModificationDate')
            .reverse()
            .value();
    }
}

export default InventoryService;