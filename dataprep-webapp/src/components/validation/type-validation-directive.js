(function() {
    'use strict';

    var INTEGER_REGEXP = /^\-?\d+$/;
    var DOUBLE_REGEXP = /^\-?\d+(\.\d+)?$/;

    /**
     * @ngdoc directive
     * @name data-prep.type-validation.directive:TypeValidation
     * @description This directive perform a type validation on input value modification.
     * @restrict E
     * @usage
     <input
            ...
            type-validation='integer' />
     * @param {string} typeValidation The wanted type (integer | numeric | double | float)
     */
    function TypeValidation() {
        return {
            require: 'ngModel',
            link: function(scope, elm, attrs, ctrl) {
                var type = attrs.typeValidation;
                ctrl.$validators.typeValidation = function(modelValue, viewValue) {
                    if (ctrl.$isEmpty(modelValue)) {
                        return true;
                    }

                    switch(type) {
                        case 'integer':
                            if (! INTEGER_REGEXP.test(viewValue)) {
                                return false;
                            }
                            break;
                        case 'numeric':
                        case 'double':
                        case 'float':
                            if (! DOUBLE_REGEXP.test(viewValue)) {
                                return false;
                            }
                            break;
                        default:
                            break;
                    }

                    return true;
                };
            }
        };
    }

    angular.module('data-prep.validation')
        .directive('typeValidation', TypeValidation);
})();