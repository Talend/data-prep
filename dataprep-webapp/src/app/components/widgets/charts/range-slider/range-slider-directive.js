/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import d3 from 'd3';
import template from './range-slider.html';

/**
 * Return the timestamp at midnight
 */
function setDateTimeToMidnight(timeStamp) {
	const dateToSetTimeToMidnight = new Date(timeStamp);
	dateToSetTimeToMidnight.setHours(0, 0, 0, 0);
	return dateToSetTimeToMidnight.getTime();
}

/**
 * @ngdoc directive
 * @name talend.widget.directive:rangeSlider
 * @description This directive renders the rangeSlider.
 * @restrict E
 * @usage
 *     <range-slider
 *             id="domId"
 *             width="300"
 *             height="100"
 *             range-limits="rangeLimits"
 *             on-brush-end="onBrushEndFn(interval)"
 *         ></range-slider>
 * @param {string} id The element id
 * @param {number} width The width of the slider
 * @param {number} height The height of the slider
 * @param {object} rangeLimits {min: number, max: number} The limits of the slider
 * @param {function} onBrushEnd The callback on slider move
 * */

export default function RangeSlider($timeout) {
	'ngInject';

	return {
		restrict: 'E',
		scope: {
			rangeLimits: '<',
			onBrushEnd: '&',
		},
		controller: 'RangeSliderCtrl',
		controllerAs: 'rangeSliderCtrl',
		bindToController: true,
		templateUrl: template,

		link(scope, element, attrs, ctrl) {
			let renderTimeout;

			// d3
			let scale;
			let svg;
			let brush;
			let brushg;

			// the left and right margins MUST be the same as the vertical Barchart ones
			const MARGIN = { top: 5, right: 20, bottom: 5, left: 15 };
			const WIDTH = attrs.width - MARGIN.left - MARGIN.right;
			const HEIGHT = attrs.height - MARGIN.top - MARGIN.bottom;

			//--------------------------------------------------------------------------------------------------
			// ----------------------------------------------CONTAINER-------------------------------------------
			//--------------------------------------------------------------------------------------------------
			/**
			 * @ngdoc method
			 * @name initContainer
			 * @methodOf data-prep.rangeSlider.directive:RangeSlider
			 * @description Init the svg container
			 */
			function initContainer() {
				svg = d3.select('.range-slider-id')
					.append('svg')
					.attr('width', WIDTH + MARGIN.left + MARGIN.right)
					.attr('height', HEIGHT + MARGIN.top + MARGIN.bottom)
					.attr('class', 'range-slider-cls')
					.append('g')
					.attr('transform', `translate(${MARGIN.left},${MARGIN.top})`);
			}

			/**
			 * @ngdoc method
			 * @name initScale
			 * @methodOf data-prep.rangeSlider.directive:RangeSlider
			 * @description Init the d3 scale
			 */
			function initScale(limits) {
				scale = d3.scale
					.linear()
					.domain([limits.min, limits.max])
					.range([0, WIDTH]);
			}

			//--------------------------------------------------------------------------------------------------
			// ------------------------------------------------BRUSH---------------------------------------------
			//--------------------------------------------------------------------------------------------------
			/**
			 * @ngdoc method
			 * @name initBrushAxis
			 * @methodOf data-prep.rangeSlider.directive:RangeSlider
			 * @description Draw brush axis
			 */
			function drawBrushAxis(isDateType, limits) {
				const axisTicksNumber = (isDateType || (limits.max < 1e10 && limits.min > 1e-10)) ? 3 : 1;

				svg.append('g')
					.attr('class', 'x axis')
					.attr('transform', `translate(0,${MARGIN.top + 20})`)
					.call(d3.svg.axis()
						.scale(scale)
						.orient('top')
						.ticks(axisTicksNumber)
						.tickFormat((d) => {
							return isDateType ? d3.time.format('%b %Y')(new Date(d)) : d3.format(',')(d);
						}))
					.selectAll('text')
					.attr('y', -13);
			}

			/**
			 * @ngdoc method
			 * @name drawBrushAxisLimitsText
			 * @methodOf data-prep.rangeSlider.directive:RangeSlider
			 * @description Draw brush axis limits text
			 */
			function drawBrushAxisLimitsText(texts) {
				svg.append('g')
					.append('text')
					.attr('class', 'the-minimum-label')
					.attr('x', -10)
					.attr('y', HEIGHT)
					.attr('text-anchor', 'start')
					.attr('fill', 'grey')
					.text(() => texts.minText);

				svg.append('g').append('text')
					.attr('class', 'the-maximum-label')
					.attr('x', WIDTH + 10)
					.attr('y', HEIGHT)
					.attr('text-anchor', 'end')
					.attr('fill', 'grey')
					.text(() => texts.maxText);
			}

			/**
			 * @ngdoc method
			 * @name drawBrush
			 * @methodOf data-prep.rangeSlider.directive:RangeSlider
			 * @description Draw brush
			 */
			function drawBrush(brushValues) {
				const centerValue = (brushValues.min + brushValues.max) / 2;

				// init brush with limits centered
				brush = d3.svg
					.brush()
					.x(scale)
					.extent([centerValue, centerValue]);

				// brush
				brushg = svg.append('g')
					.attr('transform', `translate(0,${MARGIN.top + 10})`)
					.attr('class', 'brush')
					.call(brush);

				// brush rect that reflect the values
				brushg.selectAll('.resize')
					.append('rect')
					.attr('transform', (d, i) => `translate(${i ? '-10' : '0'}, 0)`)
					.attr('width', 10)
					.attr('height', 20);

				// brush visual range style : fill the space between the limits rect
				brushg.select('.extent')
					.attr('height', 14)
					.attr('transform', 'translate(0,3)');
			}

			/**
			 * @ngdoc method
			 * @name getCurrentBrushValues
			 * @methodOf data-prep.rangeSlider.directive:RangeSlider
			 * @description Get brush values
			 */
			function getCurrentBrushValues() {
				const brushExtent = brush.extent();
				return [
					ctrl.isDateType() ? setDateTimeToMidnight(brushExtent[0]) : +brushExtent[0].toFixed(ctrl.nbDecimals),
					ctrl.isDateType() ? setDateTimeToMidnight(brushExtent[1]) : +brushExtent[1].toFixed(ctrl.nbDecimals),
				];
			}

			/**
			 * @ngdoc method
			 * @name addBrushListeners
			 * @methodOf data-prep.rangeSlider.directive:RangeSlider
			 * @description Attach brush listeners
			 */
			function addBrushListeners() {
				let originalBrushValues;

				brush
					.on('brushstart', () => {
						// save original brush values
						originalBrushValues = getCurrentBrushValues();
					})

					.on('brush', () => {
						// get current values and update input values
						const brushValues = getCurrentBrushValues();

						// update inputs value for user visual corresponding value
						$timeout(() => {
							ctrl.setInputValue({
								min: brushValues[0],
								max: brushValues[1],
							});
						});
					})

					// propagate changed values
					.on('brushend', () => {
						const brushValues = getCurrentBrushValues();

						// the values have not changed
						if (brushValues[0] === originalBrushValues[0] &&
							brushValues[1] === originalBrushValues[1]) {
							return;
						}

						ctrl.onBrushChange({
							min: brushValues[0],
							max: brushValues[1],
						});
					});
			}

			/**
			 * @ngdoc method
			 * @name initBrush
			 * @methodOf data-prep.rangeSlider.directive:RangeSlider
			 * @description Draw and configure the brush
			 */
			function initBrush(isDateType, limits, initialValues, limitsText) {
				drawBrushAxis(isDateType, limits);
				drawBrushAxisLimitsText(limitsText);
				drawBrush(initialValues);
				addBrushListeners();

				ctrl.brush = brush;
				ctrl.brushg = brushg;
				ctrl.updateBrush(initialValues);
			}

			//--------------------------------------------------------------------------------------------------
			//--------------------------------------------------------------------------------------------------
			//--------------------------------------------------------------------------------------------------
			/**
			 * @ngdoc method
			 * @name renderRangerSlider
			 * @methodOf data-prep.rangeSlider.directive:RangeSlider
			 * @description Render the slider and attach the actions listeners
			 **/
			function renderRangerSlider() {
				initScale(ctrl.rangeLimits);
				initContainer();
				initBrush(
					ctrl.isDateType(),
					ctrl.rangeLimits,
					ctrl.lastValues.brush,
					ctrl.getLimitsText()
				);
			}

			/**
			 * @ngdoc method
			 * @name initInputs
			 * @methodOf data-prep.rangeSlider.directive:RangeSlider
			 * @description init component model and reset inputs
			 **/
			function initInputs() {
				ctrl.initModel();
				ctrl.resetInputValues();
			}

			/**
			 * @ngdoc method
			 * @name adaptDateRangeLimits
			 * @methodOf data-prep.rangeSlider.directive:RangeSlider
			 * @description Set dates inputs to midgnight (timestamp)
			 **/
			function adaptDateRangeLimits(rangeLimits) {
				if (rangeLimits.type !== 'date') {
					return;
				}

				rangeLimits.min = setDateTimeToMidnight(rangeLimits.min);
				rangeLimits.max = setDateTimeToMidnight(rangeLimits.max);

				if (typeof rangeLimits.minBrush !== 'undefined') {
					rangeLimits.minBrush = setDateTimeToMidnight(rangeLimits.minBrush);
				}

				if (typeof rangeLimits.maxBrush !== 'undefined') {
					rangeLimits.maxBrush = setDateTimeToMidnight(rangeLimits.maxBrush);
				}

				if (typeof rangeLimits.minFilterVal !== 'undefined') {
					rangeLimits.minFilterVal = setDateTimeToMidnight(rangeLimits.minFilterVal);
				}

				if (typeof rangeLimits.maxFilterVal !== 'undefined') {
					rangeLimits.maxFilterVal = setDateTimeToMidnight(rangeLimits.maxFilterVal);
				}
			}

			/**
			 * @ngdoc method
			 * @name initInputs
			 * @methodOf data-prep.rangeSlider.directive:RangeSlider
			 * @description Check if the range slider should be re rendered
			 **/
			function shouldRerender(newRangeLimits, oldRangeLimits) {
				return !oldRangeLimits || !ctrl.lastValues ||
					oldRangeLimits.min !== newRangeLimits.min ||
					oldRangeLimits.max !== newRangeLimits.max ||
					ctrl.lastValues.brush.min !== newRangeLimits.minBrush ||
					ctrl.lastValues.brush.max !== newRangeLimits.maxBrush;
			}

			scope.$watch(
				() => ctrl.rangeLimits,
				(newRangeLimits, oldRangeLimits) => {
					if (!newRangeLimits) {
						element.find('svg').remove();
					}
					else {
						adaptDateRangeLimits(newRangeLimits);
						if (!shouldRerender(newRangeLimits, oldRangeLimits)) {
							return;
						}

						$timeout.cancel(renderTimeout);
						element.find('svg').remove();
						initInputs();
						renderTimeout = $timeout(renderRangerSlider, 0, false);
					}
				}
			);

			scope.$on('$destroy', () => {
				$timeout.cancel(renderTimeout);
			});
		},
	};
}
