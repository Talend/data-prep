/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

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

	let tooltipTemplate = _.template('');
	$translate(['COLON']).then((i18n) => {
		tooltipTemplate = _.template(
			`<strong><%= label %>${i18n.COLON}</strong> <span style="color:yellow"><%= primaryValue %></span><br/><br/><strong><%= title %>${i18n.COLON}</strong> <span style="color:yellow"><%= key %></span>`
		);
	});

	let tooltipFilteredTemplate = _.template('');
	$translate(['TOOLTIP_MATCHING_FILTER', 'TOOLTIP_MATCHING_FULL', 'COLON']).then((i18n) => {
		tooltipFilteredTemplate = _.template(
			`<strong><%= label %> ${i18n.TOOLTIP_MATCHING_FILTER}${i18n.COLON}</strong> <span style="color:yellow"><%= secondaryValue %> <%= percentage %></span><br/><br/><strong><%= label %> ${i18n.TOOLTIP_MATCHING_FULL}${i18n.COLON}</strong> <span style="color:yellow"><%= primaryValue %></span><br/><br/><strong><%= title %>${i18n.COLON}</strong> <span style="color:yellow"><%= key %></span>`
		);
	});

	let tooltipFilteredAggregTemplate = _.template('');
	$translate(['TOOLTIP_MATCHING_FILTER', 'COLON']).then((i18n) => {
		tooltipFilteredAggregTemplate = _.template(
			`<strong><%= label %> ${i18n.TOOLTIP_MATCHING_FILTER}${i18n.COLON}</strong> <span style="color:yellow"><%= primaryValue %></span><br/><br/><strong><%= title %>${i18n.COLON}</strong> <span style="color:yellow"><%= key %></span>`
		);
	});

	const t = {
		MAX: $translate.instant('MAX'),
		MIN: $translate.instant('MIN'),
		RANGE: $translate.instant('RANGE'),
		RECORD: $translate.instant('RECORD'),
		VALUE: $translate.instant('VALUE'),
	};

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
			return `(${quotient.toFixed(1)}%)`;
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
		let title = t.RECORD;
		let keyString = key;
		const rangeLimits = state.playground.statistics.rangeLimits;

		// range
		if (key instanceof Array) {
			const uniqueValue = key[0] === key[1];
			title = uniqueValue ? t.VALUE : t.RANGE;

			if (uniqueValue) {
				keyString = key[0];
			}
			else if (key[0] <= rangeLimits.min) {
				if (key[1] >= rangeLimits.max) {
					keyString = `[${t.MIN},${t.MAX}]`;
				}
				else {
					keyString = `[${t.MIN},${key[1]}[`;
				}
			}
			else if (key[1] >= rangeLimits.max) {
				keyString = `[${key[0]},${t.MAX}]`;
			}
			else {
				keyString = `[${key[0]},${key[1]}[`;
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
