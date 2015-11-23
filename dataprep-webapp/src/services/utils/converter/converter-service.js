(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.utils.service:ConverterService
     * @description Converter service. This service help to convert data
     */
    function ConverterService() {
        return {
            isNumber: isNumber,

            //types
            toInputType: toInputType,
            simplifyType: simplifyType,
            adaptValue: adaptValue
        };

        /**
         * @ngdoc method
         * @name isNumber
         * @methodOf data-prep.services.utils.service:ConverterService
         * @param {String} value The value to test
         * @description Check if the entered string is valid number
         * @return {boolean}
         */
        function isNumber (value) {
            return /^\s*[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?\s*$/.test(value);
        }

        //--------------------------------------------------------------------------------------------------------------
        //------------------------------------------------------TYPES---------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name toInputType
         * @methodOf data-prep.services.utils.service:ConverterService
         * @param {string} type The type to convert
         * @description Convert backend type to HTML input type
         * @returns {string} The converted type
         */
        function toInputType(type) {
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
        }

        /**
         * @ngdoc method
         * @name simplifyType
         * @methodOf data-prep.services.utils.service:ConverterService
         * @param {string} type The type to convert
         * @description Convert backend type to a simplified, more user friendly, one
         * @returns {string} The simplified type
         */
        function simplifyType(type) {
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
        }

        /**
         * @ngdoc method
         * @name adaptValue
         * @methodOf data-prep.services.utils.service:ConverterService
         * @param {object} type The target type of the given value
         * @param {object} value The value to adapt
         * @description [PRIVATE] Adapt the given value to the target type
         * @returns {Object} The adapted value
         */
        function adaptValue(type, value) {
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
        }
    }

    angular.module('data-prep.services.utils')
        .service('ConverterService', ConverterService);

})();