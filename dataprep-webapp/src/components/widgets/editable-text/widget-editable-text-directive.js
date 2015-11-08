(function() {
    'use strict';

    function TalendEditableText($timeout) {
        return {
            restrict: 'E',
            templateUrl: 'components/widgets/editable-text/editable-text.html',
            scope: {
                placeholder: '@',
                text: '=',
                textTitle: '@',
                textClass: '@',
                editionMode: '=',
                onTextClick: '&',
                onValidate: '&',
                onCancel: '&'
            },
            bindToController: true,
            controller: 'TalendEditableTextCtrl',
            controllerAs: 'editableTextCtrl',
            link: function(scope, iElement, iAttrs, ctrl) {
                $timeout(function() {
                    var inputElement = iElement.find('.edition-text-input').eq(0);

                    inputElement.keydown(function(e) {
                        if (e.keyCode === 27) {
                            e.stopPropagation();
                            ctrl.cancel();
                            scope.$digest();
                        }
                    });

                    iElement.find('.edit-btn').eq(0).click(function() {
                        inputElement.focus();
                        inputElement.select();
                    });
                }, 0, false);

                scope.$watch(
                    function() {
                        return ctrl.text;
                    },
                    function() {
                        ctrl.reset();
                    }
                );
            }
        };
    }

    angular.module('talend.widget')
        .directive('talendEditableText', TalendEditableText);
})();