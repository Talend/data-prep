(function () {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.dataset-workflow
     * @description This module contains the services to manage the upload & update of datasets
     * @requires data-prep.services.dataset
     * @requires data-prep.services.utils
     * @requires ui.router
     * @requires data-prep.services.dataset
     */
    angular.module('data-prep.services.datasetWorkflowService', [
        'data-prep.services.dataset',
        'data-prep.services.utils',
        'ui.router',
        'data-prep.services.state'
    ]);
})();