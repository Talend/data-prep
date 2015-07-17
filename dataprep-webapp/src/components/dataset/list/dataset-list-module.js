(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.dataset-list
     * @description This module contains the controller and directives to manage the dataset list
     * @requires talend.widget.service:TalendConfirmService
     * @requires data-prep.services.dataset.service:DatasetService
     * @requires data-prep.services.playground.service:PlaygroundService
     * @requires data-prep.services.utils.service:MessageService
     * @requires data-prep.services.uploadWorkflowService.service:UploadWorkflowService
     */
    angular.module('data-prep.dataset-list', [
        'ui.router',
        'pascalprecht.translate',
        'talend.widget',
        'data-prep.dataset-xls-preview',
        'data-prep.services.dataset',
        'data-prep.services.playground',
        'data-prep.services.utils',
        'data-prep.services.uploadWorkflowService',
        'angular-momentjs'
    ])
    // TODO maybe could be move in an external filters file?
    .filter('moment_from_now', function($moment) {
              return function(dateString,format) {
                return $moment(dateString,format?format:'x').fromNow();
              };
    });

})();