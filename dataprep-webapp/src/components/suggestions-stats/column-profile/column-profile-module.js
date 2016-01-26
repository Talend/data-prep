(function() {
    'use strict';

    angular.module('data-prep.column-profile', [
        'talend.widget',
        'data-prep.services.dataset',
        'data-prep.services.filter',
        'data-prep.services.playground',
        'data-prep.services.statistics',
        'data-prep.services.state'
    ]);
})();