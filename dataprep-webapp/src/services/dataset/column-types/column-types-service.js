(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.dataset.service:ColumnTypesService
     * @description Column types service
     */
    function ColumnTypesService($q, $http, RestURLs) {
        var types;

        /**
         * @ngdoc method
         * @name getTypes
         * @methodOf data-prep.services.dataset.service:ColumnTypesService
         * @description Return all primitive types
         * @returns {Promise} The GET promise
         */
        this.getTypes = function getTypes() {
            if (types) {
                return $q.when(types);
            }
            return $http.get(RestURLs.typesUrl).then(function (response) {
                types = response.data;
                return types;
            });
        };
    }

    angular.module('data-prep.services.dataset')
        .service('ColumnTypesService', ColumnTypesService);
})();