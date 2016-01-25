(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.utils.service:DateService
     * @description DateService service. This service help to manipulate dates
     */
    function DateService() {
        return {
            isInDateLimits: isInDateLimits
        };

        /**
         * @ngdoc method
         * @name isInDateLimits
         * @methodOf data-prep.services.utils.service:DateService
         * @description Predicate that test if a date is in the range
         * @param {number} minTimestamp The range min timestamp
         * @param {number} maxTimestamp The range max timestamp
         * @param {Array} patterns The date patterns to use for date parsing
         */
        function isInDateLimits(minTimestamp, maxTimestamp, patterns) {
            return function (value) {
                var parsedMoment = _.chain(patterns)
                    .map(function (pattern) {
                        return moment(value, pattern, true);
                    })
                    .find(function (momentDate) {
                        return momentDate.isValid();
                    })
                    .value();

                if (!parsedMoment) {
                    return false;
                }

                var time = parsedMoment.toDate().getTime();
                return time === minTimestamp || (time > minTimestamp && time < maxTimestamp);
            };
        }
    }

    angular.module('data-prep.services.utils')
        .service('DateService', DateService);

})();