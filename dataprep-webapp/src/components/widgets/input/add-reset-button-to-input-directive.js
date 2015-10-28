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
                var searchIcon = angular.element('<div style="position: absolute;"><img src="/assets/images/actions-search-icon.png" alt=""></div>');

                clearButton.css('top','2px');
                clearButton.css('left',iElement.width()-margin);
                clearButton.css('display','none');


                searchIcon.css('top','2px');
                searchIcon.css('left',iElement.width()-margin);
                searchIcon.css('display','block');

                iElement.parent().append(clearButton);
                iElement.parent().append(searchIcon);

                scope.$watch(function(){
                        return ngModel.$modelValue;
                    }, function (value) {
                    if(value) {
                        clearButton.css('display','block');
                        searchIcon.css('display','none');
                    } else {
                        searchIcon.css('display','block');
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
