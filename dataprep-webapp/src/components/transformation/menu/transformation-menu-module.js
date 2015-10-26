(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.transformation-menu
     * @description This module contains the controller and directives to manage the transformation menu items
     * @requires talend.widget
     * @requires data-prep.transformation-form
     * @requires data-prep.type-transformation-menu
     * @requires data-prep.services.playground
     * @requires data-prep.services.transformation
     * @requires data-prep.services.state
     */
    angular.module('data-prep.transformation-menu', [
        'talend.widget',
        'data-prep.transformation-form',
        'data-prep.type-transformation-menu',
        'data-prep.services.playground',
        'data-prep.services.transformation',
        'data-prep.services.state'
    ]);
})();