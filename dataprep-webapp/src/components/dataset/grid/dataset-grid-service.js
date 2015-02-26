(function() {
    'use strict';

    function DatasetGridService($rootScope) {
        var self = this;

        self.visible = false;
        self.metadata = null;
        self.data = null;

        /**
         * Set visibility flag to true
         */
        self.show = function() {
            self.visible = true;
        };

        /**
         * Set visibility flag to false
         */
        self.hide = function() {
            self.visible = false;
        };

        /**
         * Set dataset metadata and records
         * @param metadata - the new metadata
         * @param data - the new data
         */
        self.setDataset = function(metadata, data) {
            self.metadata = metadata;
            self.data = data;

            $rootScope.$emit('talend.loading.datagrid.stop.all');
        };

        /**
         * Update data records
         * @param records - the new records
         */
        self.updateRecords = function(records) {
            self.data.records = records;
        };
    }

    angular.module('data-prep-dataset')
        .service('DatasetGridService', DatasetGridService);
})();