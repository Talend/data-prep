/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:DateService
 * @description DateService service. This service help to manipulate dates
 */
export default function DateService() {
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