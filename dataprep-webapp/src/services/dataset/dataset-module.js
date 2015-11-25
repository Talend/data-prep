(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.dataset
     * @description This module contains the services to manipulate datasets
     * @requires data-prep.services.utils
     * @requires data-prep.services.preparation
     */
    angular.module('data-prep.services.dataset', [
        'data-prep.services.utils',
        'data-prep.services.preparation',
        'data-prep.services.state',
        'data-prep.services.folder',
        'angularFileUpload' //file upload with progress support
    ]);
})();