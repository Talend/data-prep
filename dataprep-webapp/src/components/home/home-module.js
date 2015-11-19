(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.home
     * @description This module contains the home page of the app.
     * @requires talend.widget
     * @requires data-prep.dataset-upload-list
     * @requires data-prep.dataset-list
     * @requires data-prep.playground
     * @requires data-prep.preparation-list
     * @requires data-prep.services.dataset
     * @requires data-prep.services.utils
     * @requires data-prep.services.datasetWorkflowService
     * @requires data-prep.services.state
     * @requires data-prep.folder
     * @requires data-prep.services.folder
     */
    angular.module('data-prep.home', [
        'talend.widget',
        'data-prep.dataset-upload-list',
        'data-prep.dataset-list',
        'data-prep.playground',
        'data-prep.preparation-list',
        'data-prep.services.dataset',
        'data-prep.services.utils',
        'data-prep.services.datasetWorkflowService',
        'data-prep.services.state',
        'data-prep.folder',
        'data-prep.services.folder'
    ]);
})();