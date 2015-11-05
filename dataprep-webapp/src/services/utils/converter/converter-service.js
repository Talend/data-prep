(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.utils.service:ConverterService
     * @description Converter service. This service help to convert data
     */
    function ConverterService() {

        /**
         * @ngdoc method
         * @name toNumber
         * @methodOf data-prep.services.utils.service:ConverterService
         * @param {String} type - the type of the given value
         * @description [PRIVATE] trims and checks if the entered string is valid number
         * @return Number or undefined
         */
        this.isNumber = function toNumber (value) {
            value = value.trim();
            if(/^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/.test(value)){
                return true;
            }
            return false;
        };

        /**
         * @ngdoc method
         * @name toInputType
         * @methodOf data-prep.services.utils.service:ConverterService
         * @param {string} type - the type to convert
         * @description Convert backend type to HTML input type
         * @returns {string} - the converted type
         */
        this.toInputType = function (type) {
            switch (type) {
                case 'numeric':
                case 'integer':
                case 'double':
                case 'float':
                    return 'number';
                case 'boolean':
                    return 'checkbox';
                default:
                    return 'text';
            }
        };

        /**
         * @ngdoc method
         * @name simplifyType
         * @methodOf data-prep.services.utils.service:ConverterService
         * @param {string} type - the type to convert
         * @description Convert backend type to a simplified, more user friendly, one
         * @returns {string} - the simplified type
         */
        this.simplifyType = function (type) {
            switch (type.toLowerCase()) {
                case 'numeric':
                case 'integer':
                    return 'integer';
                case 'double':
                case 'float':
                case 'decimal':
                    return 'decimal';
                case 'boolean':
                    return 'boolean';
                case 'string':
                case 'char':
                    return 'text';
                case 'date':
                    return 'date';
                default:
                    return 'unknown';
            }
        };

        /**
         * @ngdoc method
         * @name adaptValue
         * @methodOf data-prep.services.utils.service:ConverterService
         * @param {object} type - the target type of the given value
         * @param {object} value - the value to adapt
         * @description [PRIVATE] Adapt the given value to the target type
         */
        this.adaptValue = function (type, value) {
            switch (type) {
                case 'numeric':
                case 'integer':
                case 'double':
                case 'float':
                    return parseFloat(value) || 0;
                case 'boolean':
                    return value === 'true' || value === true;
                default :
                    return value;
            }
        };
    }

    angular.module('data-prep.services.utils')
        .service('ConverterService', ConverterService);

})();