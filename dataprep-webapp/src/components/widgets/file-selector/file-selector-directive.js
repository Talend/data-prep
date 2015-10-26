(function() {
    'use strict';
    function TalendFileSelector() {
        return {
            restrict: 'E',
            templateUrl: 'components/widgets/file-selector/file-selector.html',
            replace: true,
            scope: {
                buttonDataIcon: '@',
                buttonTitle: '@',
                fileModel: '=',
                onFileChange: '&'
            },
            bindToController: true,
            controllerAs: 'talendFileSelectorCtrl',
            controller: function() {},
            link: function(scope, element) {
                element.find('span').bind('click', function() {
                    element.find('input').click();
                });
            }
        };
    }
    angular.module('talend.widget')
        .directive('talendFileSelector', TalendFileSelector);
})();