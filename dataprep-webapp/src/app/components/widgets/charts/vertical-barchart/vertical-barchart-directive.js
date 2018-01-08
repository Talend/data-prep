/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import d3 from 'd3';
import d3Tip from 'd3-tip';

/**
 * @ngdoc directive
 * @name talend.widget.directive:verticalBarchart
 * @description This directive renders the vertical bar chart.
 * @restrict E
 * @usage
 * <vertical-barchart id="vBarChart"
 *     width="320"
 *     height="400"
 *     on-click="clickFn(interval)"
 *     show-x-axis="show"
 *
 *     key-field="country"
 *     key-label="Country of the world"
 *     active-limits="activeLimits"
 *
 *     primary-data="primaryData"
 *     primary-value-field="occurrences"
 *
 *     secondary-data="secondaryData"
 *     secondary-value-field="filteredOccurrences"
 *
 *     tooltip-content="getTooltipContent(keyLabel, key, primaryValue, secondaryValue)">
 * </vertical-barchart>
 * @param {number}      width The chart width
 * @param {number}      height The chart height
 * @param {boolean}     showXAxis Determine if the x-axis should be drawn
 * @param {function}    onClick The callback on chart bar click. The interval is an Object {min: minValue, max, maxValue}
 * @param {function}    onCtrlClick The callback on chart bar ctrl + click. The interval is an Object {min: minValue, max, maxValue}
 * @param {function}    onShiftClick The callback on chart bar shift + click. The interval is an Object {min: minValue, max, maxValue}
 * @param {string}      keyField The key property name in primaryData elements
 * @param {string}      keyLabel The label property name in primaryData elements
 * @param {array}       primaryData The primary value array to render
 * @param {string}      primaryValueField The primary value property name in primaryData
 * @param {array}       secondaryData The secondary value array to render
 * @param {string}      secondaryValueField The secondary value property name in secondaryData
 * @param {array}       activeLimits The limits [min, max[ that represents the active part
 * @param {function}    tooltipContent The tooltip content generator. It can take 4 infos : keyLabel (the label), key (the key), primaryValue (the selected primary value), secondaryValue (the selected secondary value)
 */

export default function VerticalBarchart($timeout) {
	'ngInject';

	return {
		restrict: 'E',
		scope: {
			onClick: '&',
			onCtrlClick: '&',
			onShiftClick: '&',
			activeLimits: '=',
			keyField: '@',
			keyLabel: '@',
			primaryData: '=',
			primaryValueField: '@',
			secondaryData: '=',
			secondaryValueField: '@',
			showXAxis: '=',
			tooltipContent: '&',
		},
		link(scope, element, attrs) {
			const BAR_MIN_HEIGHT = 3;
			let oldVisuData;
			const labelTooltip = scope.keyLabel;
			let activeLimits = scope.activeLimits;
			let renderPrimaryTimeout;
			let renderSecondaryTimeout;
			let updateLimitsTimeout;
			const containerId = `#${attrs.id}`;

			// Define chart sizes and margin
			let margin;
			let containerWidth;
			let containerHeight;
			let width;
			let height;

			//------------------------------------------------------------------------------------------------------
			// ----------------------------------------------- Tooltip ----------------------------------------------
			//------------------------------------------------------------------------------------------------------
			const tooltip = d3Tip()
				.attr('class', 'vertical-barchart-cls d3-tip')
				.offset([0, -11])
				.direction('w')
				.html((primaryDatum, index) => {
					const secondaryDatum = scope.secondaryData ? scope.secondaryData[index] : undefined;
					return scope.tooltipContent({
						keyLabel: scope.keyLabel,
						key: getXAxisDomain(primaryDatum),
						primaryValue: getPrimaryValue(primaryDatum),
						secondaryValue: secondaryDatum && getSecondaryValue(secondaryDatum),
					});
				});

			//------------------------------------------------------------------------------------------------------
			// ------------------------------------------ Data adaptation -------------------------------------------
			//------------------------------------------------------------------------------------------------------
			function getXAxisDomain(data) {
				return getRangeLabel(data) || getInterval(data);
			}

			function getInterval(data) {
				const range = getRangeInfos(data);
				return [range.min, range.max];
			}

			function getRangeInfos(data) {
				return data[scope.keyField];
			}

			function getRangeLabel(data) {
				return data[scope.keyField].label;
			}

			function getPrimaryValue(data) {
				return data[scope.primaryValueField];
			}

			function getSecondaryValue(data) {
				return data[scope.secondaryValueField];
			}

			function getBottomMargin() {
				const labelLength = getRangeLabel(scope.primaryData[0]).length;
				return labelLength * 8;// the longer the label is, the more space we need
			}

			//------------------------------------------------------------------------------------------------------
			// ------------------------------------------- Chart utils ----------------------------------------------
			//------------------------------------------------------------------------------------------------------
			let svg;
			let xScale;
			let yScale;

			function initChartSizes() {
				margin = {
					top: 20,
					right: 20,
					bottom: scope.showXAxis ? getBottomMargin() : 10,
					left: 15,
				};
				containerWidth = +attrs.width;
				containerHeight = +attrs.height + margin.bottom;
				width = containerWidth - margin.left - margin.right;
				height = containerHeight - margin.top - margin.bottom;
			}

			function initScales() {
				xScale = d3.scale.ordinal().rangeRoundBands([0, width], 0.2);
				yScale = d3.scale.linear().range([height, 0]);
			}

			function configureAxisScales(statData) {
				xScale.domain(statData.map(getXAxisDomain));
				yScale.domain([0, d3.max(statData, getPrimaryValue)]);
			}

			function createContainer() {
				svg = d3.select(containerId)
					.append('svg')
					.attr('class', 'vertical-barchart-cls')
					.attr('width', containerWidth)
					.attr('height', containerHeight)
					.append('g')
					.attr('transform', `translate(${margin.left},${margin.top})`);
				svg.call(tooltip);
			}

			function createBarsContainers() {
				svg.append('g')
					.attr('class', 'primaryBar');

				svg.append('g')
					.attr('class', 'secondaryBar');
			}

			function adaptToMinHeight(realHeight) {
				return realHeight > 0 && realHeight < BAR_MIN_HEIGHT ? BAR_MIN_HEIGHT : realHeight;
			}

			function adaptToMinHeightYPosition(realYPosition) {
				const basePosition = yScale(0);
				const barHeight = adaptToMinHeight(basePosition - realYPosition);
				return basePosition - barHeight;
			}

			function drawBars(containerClassName, statData, getValue, barClassName) {
				const bars = svg.select(`.${containerClassName}`)
					.selectAll(`.${barClassName}`)
					.data(statData, d => `${getInterval(d)}`);

				// enter
				bars.enter()
					.append('rect')
					.attr('class', barClassName)
					.attr('x', d => xScale(getXAxisDomain(d)))
					.attr('width', xScale.rangeBand())
					.attr('y', () => yScale(0))
					.attr('height', 0)
					.transition()
					.ease('cubic')
					.delay((d, i) => i * 10)
					.attr('height', (d) => {
						const realHeight = height - yScale(getValue(d));
						return adaptToMinHeight(realHeight);
					})
					.attr('y', (d) => {
						const realYPosition = yScale(getValue(d));
						return adaptToMinHeightYPosition(realYPosition);
					});

				// update
				bars.transition().ease('exp').delay((d, i) => i * 30)
					.attr('height', (d) => {
						const realHeight = height - yScale(getValue(d));
						return adaptToMinHeight(realHeight);
					})
					.attr('y', (d) => {
						const realYPosition = yScale(getValue(d));
						return adaptToMinHeightYPosition(realYPosition);
					});
			}

			function drawXAxis() {
				svg.append('g')
					.attr('class', 'x axis')
					.attr('transform', `translate(0,${height})`)
					.call(d3.svg.axis()
						.scale(xScale)
						.orient('bottom')
						.ticks(5)
					)
					.selectAll('text')
					.attr('y', 5)
					.attr('x', -9)
					.attr('dy', '.35em')
					.style('text-anchor', 'end')
					.attr('transform', 'rotate(295)');
			}

			function drawYAxis() {
				svg.append('g')
					.attr('class', 'yAxis')
					.append('text')
					.attr('x', -height / 2)
					.attr('y', -2)
					.attr('transform', 'rotate(-90)')
					.style('text-anchor', 'middle')
					.text(labelTooltip);
			}

			function drawHorizontalGrid() {
				const minSizeBetweenGrid = 20;
				const ticksThreshold = Math.ceil(height / minSizeBetweenGrid);
				const ticksNbre = yScale.domain()[1] > ticksThreshold ? ticksThreshold : yScale.domain()[1];

				svg.append('g')
					.attr('class', 'grid')
					.call(d3.svg.axis()
						.scale(yScale)
						.orient('right')
						.tickSize(width, 0, 0)
						.tickFormat(d3.format(',d'))
						.ticks(ticksNbre)
					)
					// place text
					.selectAll('.tick text')
					.attr('y', -5)
					.attr('x', width)
					.attr('dy', '.15em')
					.style('text-anchor', 'end');
			}

			function drawHoverBars(statData) {
				svg.selectAll('g.bg-rect')
					.data(statData)
					.enter()
					.append('g')
					.attr('class', 'hover')
					.attr('transform', d => `translate(${xScale(getXAxisDomain(d)) - 2}, 0)`)
					.append('rect')
					.attr('width', xScale.rangeBand() + 4)
					.attr('height', height)
					.attr('class', 'bg-rect')
					.style('opacity', 0)
					.on('mouseenter', function (d, i) {
						d3.select(this).style('opacity', 0.4);
						tooltip.show(d, i);
					})
					.on('mouseleave', function (d) {
						d3.select(this).style('opacity', 0);
						tooltip.hide(d);
					})
					.on('click', (d) => {
						// create a new reference as the data object could be modified outside the component
						const interval = _.extend({}, getRangeInfos(d));
						if (d3.event.ctrlKey || d3.event.metaKey) {
							scope.onCtrlClick({ interval });
							return;
						}
						else if (d3.event.shiftKey) {
							scope.onShiftClick({ interval });
							return;
						}

						scope.onClick({ interval });
					});
			}

			//------------------------------------------------------------------------------------------------------
			// ------------------------------------------- Chart render ---------------------------------------------
			//------------------------------------------------------------------------------------------------------
			function renderWholeVBarchart(firstVisuData, secondVisuData) {
				initChartSizes();
				initScales();
				createContainer();
				configureAxisScales(firstVisuData);
				createBarsContainers();
				if (scope.showXAxis) {
					drawXAxis(firstVisuData);
				}

				drawBars('primaryBar', firstVisuData, getPrimaryValue, 'bar');
				renderSecondVBars(secondVisuData);

				drawHorizontalGrid();
				drawYAxis();
				drawHoverBars(firstVisuData);
				scope.buckets = d3.selectAll('rect.bar');
			}

			function renderSecondVBars(secondVisuData) {
				if (secondVisuData) {
					drawBars('secondaryBar', secondVisuData, getSecondaryValue, 'blueBar');
				}
			}

			function updateBarsLookFeel() {
				if (activeLimits) {
					scope.buckets.transition()
						.delay((d, i) => i * 10)
						.style('opacity', (d) => {
							const range = getRangeInfos(d);
							const rangeMin = range.min;
							const rangeMax = range.max;
							const minLimit = activeLimits[0];
							const maxLimit = activeLimits[1];
							return rangeMin === minLimit || (rangeMin < maxLimit && rangeMax > minLimit) ? '1' : '.4';
						});
				}
			}

			//------------------------------------------------------------------------------------------------------
			// ---------------------------------------------- Watchers ----------------------------------------------
			//------------------------------------------------------------------------------------------------------
			scope.$watchGroup(['primaryData', 'secondaryData'],
				function (newValues) {
					const firstVisuData = newValues[0];
					const secondVisuData = newValues[1];
					const firstDataHasChanged = firstVisuData !== oldVisuData;

					if (firstDataHasChanged) {
						oldVisuData = firstVisuData;
						element.empty();
						// because the tooltip is not a child of the vertical barchart element
						d3.selectAll('.vertical-barchart-cls.d3-tip').remove();
						if (firstVisuData) {
							$timeout.cancel(renderPrimaryTimeout);
							renderPrimaryTimeout = $timeout(renderWholeVBarchart.bind(this, firstVisuData, secondVisuData), 100, false);
						}
					}
					else {
						$timeout.cancel(renderSecondaryTimeout);
						renderSecondaryTimeout = $timeout(renderSecondVBars.bind(this, secondVisuData), 100, false);
					}
				}
			);

			scope.$watch('activeLimits',
				(newLimits) => {
					if (newLimits) {
						$timeout.cancel(updateLimitsTimeout);
						updateLimitsTimeout = $timeout(() => {
							activeLimits = newLimits;
							updateBarsLookFeel();
						}, 500, false);
					}
				}
			);

			scope.$on('$destroy', () => {
				d3.selectAll('.vertical-barchart-cls.d3-tip').remove();
				$timeout.cancel(renderPrimaryTimeout);
				$timeout.cancel(renderSecondaryTimeout);
				$timeout.cancel(updateLimitsTimeout);
			});
		},
	};
}
