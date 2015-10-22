(function() {
    'use strict';
    function AddResetButtonToInput() {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, iElement, iAttrs, ngModel) {
                var margin = 5;
                var wrapper = angular.element('<div style="position:relative;"></div>');

                iElement.wrap(wrapper);

                var clearButton = angular.element('<div style="position: absolute;"><img src="/assets/images/common-input-clear.png" alt="x"></div>');
                var loupeIcone = angular.element('<div style="position: absolute;"><img src="/assets/images/icone-loupe.png" alt=""></div>');

                clearButton.css('top','2px');
                clearButton.css('left',iElement.width()-margin);
                clearButton.css('display','none');


                loupeIcone.css('top','2px');
                loupeIcone.css('left',iElement.width()-margin);
                loupeIcone.css('display','block');

                iElement.parent().append(clearButton);
                iElement.parent().append(loupeIcone);

                scope.$watch(function(){
                        return ngModel.$modelValue;
                    }, function (value) {
                    if(value) {
                        clearButton.css('display','block');
                        loupeIcone.css('display','none');
                    } else {
                        loupeIcone.css('display','block');
                        clearButton.css('display','none');
                    }
                });

                clearButton.on('click',function(){
                    scope.$apply(function(){
                        ngModel.$setViewValue('');
                        ngModel.$render();
                    });

                });
            }
        };
    }

    angular.module('talend.widget')
        .directive('addResetButtonToInput', AddResetButtonToInput);
})();
