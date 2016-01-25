(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.data-prep.inventory-item
     * @description This module contains the controller and directives to manage an inventory item
     * @requires talend.widget
     */
    angular.module('data-prep.inventory-item', [
        'ui.router',
        'pascalprecht.translate',
        'talend.widget',
        'data-prep.services.utils'
    ]);
})();