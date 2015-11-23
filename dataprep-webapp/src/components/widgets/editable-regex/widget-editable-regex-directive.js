(function() {
    'use strict';

    /**
     * @ngdoc directive
     * @name talend.widget.directive:EditableRegex
     * @description This directive create an regex input
     * @restrict E
     * @usage
     <editable-regex ng-model="value"></editable-regex>
     * @param {object} ngModel The model to bind
     */
    function TalendEditableRegex() {
        return {
            restrict: 'E',
            templateUrl: 'components/widgets/editable-regex/editable-regex.html',
            scope: {
                value: '=ngModel'
            },
            bindToController: true,
            controller: 'TalendEditableRegexCtrl',
            controllerAs: 'editableRegexCtrl'
        };
    }

    angular.module('talend.widget')
        .directive('talendEditableRegex', TalendEditableRegex);
})();