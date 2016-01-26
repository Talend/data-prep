(function () {
    'use strict';
    /**
     * @ngdoc service
     * @name data-prep.services.statistics.service:StatisticsTooltipService
     * @description Generate the template for the chart's tooltip
     */
    function StatisticsTooltipService(state, $translate) {
        var TOOLTIP_FILTERED_TEMPLATE =  _.template(
            '<strong><%= label %> ' + $translate.instant('TOOLTIP_MATCHING_FILTER') + ': </strong><span style="color:yellow"><%= secondaryValue %> <%= percentage %></span>' +
            '<br/><br/>' +
            '<strong><%= label %> ' + $translate.instant('TOOLTIP_MATCHING_FULL') + ': </strong><span style="color:yellow"><%= primaryValue %></span>' +
            '<br/><br/>' +
            '<strong><%= title %>: </strong><span style="color:yellow"><%= key %></span>'
        );

        var TOOLTIP_TEMPLATE =  _.template(
            '<strong><%= label %>: </strong><span style="color:yellow"><%= primaryValue %></span>' +
            '<br/><br/>' +
            '<strong><%= title %>: </strong><span style="color:yellow"><%= key %></span>'
        );

        return {
            getTooltip: getTooltip
        };

        /**
         * @name getPercentage
         * @description Compute the percentage
         * @type {Number} numer numerator
         * @type {Number} denum denumerator
         * @returns {string} The percentage label
         */
        function getPercentage(numer, denum) {
            if (numer && denum) {
                var quotient = (numer / denum) * 100;
                //toFixed(1) and not toFixed(0) because (19354/19430 * 100).toFixed(0) === '100'
                return '(' + quotient.toFixed(1) + '%)';
            }
            else {
                return '(0%)';
            }
        }

        /**
         * @ngdoc property
         * @name getTooltip
         * @propertyOf data-prep.services.statistics:StatisticsTooltipService
         * @description creates the html tooltip template
         * @type {string} keyLabel The label
         * @type {object} key The key
         * @type {string} primaryValue The primary (unfiltered) value
         * @type {string} secondaryValue The secondary (filtered) value
         * @returns {String} Compiled tooltip
         */
        function getTooltip(keyLabel, key, primaryValue, secondaryValue) {
            var title = 'Record';
            var keyString = key;

            //range
            if(key instanceof Array) {
                var uniqueValue = key[0] === key[1];
                title = uniqueValue ? 'Value' : 'Range';
                keyString = uniqueValue ? key[0] : '[' + key + '[';
            }

            if (state.playground.filter.gridFilters.length) {
                var percentage = getPercentage(secondaryValue, primaryValue);
                return TOOLTIP_FILTERED_TEMPLATE({
                    label: keyLabel,
                    title: title,
                    percentage: percentage,
                    key: keyString,
                    primaryValue: primaryValue,
                    secondaryValue: secondaryValue
                });
            }
            else {
                return TOOLTIP_TEMPLATE({
                    label: keyLabel,
                    title: title,
                    key: keyString,
                    primaryValue: primaryValue
                });
            }
        }
    }

    angular.module('data-prep.services.statistics')
        .service('StatisticsTooltipService', StatisticsTooltipService);
})();