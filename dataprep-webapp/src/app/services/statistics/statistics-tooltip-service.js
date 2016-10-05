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
 * @name data-prep.services.statistics.service:StatisticsTooltipService
 * @description Generate the template for the chart's tooltip
 */
export default function StatisticsTooltipService($translate, state) {
	'ngInject';

	const tooltipTemplate = _.template(
		'<strong><%= label %>: </strong><span style="color:yellow"><%= primaryValue %></span>' +
		'<br/><br/>' +
		'<strong><%= title %>: </strong><span style="color:yellow"><%= key %></span>'
	);

	let tooltipFilteredTemplate = _.template('');
	$translate(['TOOLTIP_MATCHING_FILTER', 'TOOLTIP_MATCHING_FULL']).then((messages) => {
		tooltipFilteredTemplate = _.template(
			'<strong><%= label %> ' + messages.TOOLTIP_MATCHING_FILTER + ': </strong><span style="color:yellow"><%= secondaryValue %> <%= percentage %></span>' +
			'<br/><br/>' +
			'<strong><%= label %> ' + messages.TOOLTIP_MATCHING_FULL + ': </strong><span style="color:yellow"><%= primaryValue %></span>' +
			'<br/><br/>' +
			'<strong><%= title %>: </strong><span style="color:yellow"><%= key %></span>'
		);
	});

	let tooltipFilteredAggregTemplate = _.template('');
	$translate(['TOOLTIP_MATCHING_FILTER']).then((messages) => {
		tooltipFilteredAggregTemplate = _.template(
			'<strong><%= label %> ' + messages.TOOLTIP_MATCHING_FILTER + ': </strong><span style="color:yellow"><%= primaryValue %></span>' +
			'<br/><br/>' +
			'<strong><%= title %>: </strong><span style="color:yellow"><%= key %></span>'
		);
	});

	return {
		getTooltip,
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
			const quotient = (numer / denum) * 100;
			// toFixed(1) and not toFixed(0) because (19354/19430 * 100).toFixed(0) === '100'
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
		let title = 'Record';
		let keyString = key;
		const rangeLimits = state.playground.statistics.rangeLimits;
		const minLabel = $translate.instant('MIN');
		const maxLabel = $translate.instant('MAX');

		// range
		if (key instanceof Array) {
			const uniqueValue = key[0] === key[1];
			title = uniqueValue ? 'Value' : 'Range';

			if (uniqueValue) {
				keyString = key[0];
			}
			else if (key[0] <= rangeLimits.min) {
				if (key[1] >= rangeLimits.max) {
					keyString = '[' + minLabel + ',' + maxLabel + ']';
				}
				else {
					keyString = '[' + minLabel + ',' + key[1] + '[';
				}
			}
			else if (key[1] >= rangeLimits.max) {
				keyString = '[' + key[0] + ',' + maxLabel + ']';
			}
			else {
				keyString = '[' + key[0] + ',' + key[1] + '[';
			}
		}

		if (state.playground.filter.gridFilters.length) {
			if (state.playground.statistics.histogram.aggregation) {
				return tooltipFilteredAggregTemplate({
					label: keyLabel,
					title,
					key: keyString,
					primaryValue,
				});
			}
			else {
				const percentage = getPercentage(secondaryValue, primaryValue);
				return tooltipFilteredTemplate({
					label: keyLabel,
					title,
					percentage,
					key: keyString,
					primaryValue,
					secondaryValue,
				});
			}
		}
		else {
			return tooltipTemplate({
				label: keyLabel,
				title,
				key: keyString,
				primaryValue,
			});
		}
	}
}
