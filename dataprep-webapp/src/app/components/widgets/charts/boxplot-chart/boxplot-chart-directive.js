/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import d3 from 'd3';

/**
 * @ngdoc directive
 * @name talend.widget.directive:boxplotChart
 * @description This directive renders the boxplot chart.
 * @restrict E
 * @usage
 *     <boxplot-chart-chart id="boxplotId"
 *             width="250"
 *             height="400"
 *             boxplot-data="statsDetailsCtrl.boxplotData"
 *         ></boxplot-chart-chart>
 * @param {string} boxplotId The component id
 * @param {number} width The component width
 * @param {number} height The component height
 * @param {object} boxplotData The boxplot statistics
 * */

export default function BoxplotChart($timeout, $translate) {
	'ngInject';

	return {
		restrict: 'E',
		scope: {
			boxplotData: '=',
		},
		link(scope, element, attrs) {
			let renderTimeout;
			const container = attrs.id;
			const w = +attrs.width;
			const h = +attrs.height;

			function renderBoxplotchart(boxValues) {
				const margin = { top: 30, right: 80, bottom: 70, left: 80 };
				const width = w - margin.left - margin.right;
				const height = h - margin.top - margin.bottom;

				const duration = 1000;

				const svg = d3.select(`#${container}`).append('svg')
					.attr('width', width + margin.left + margin.right)
					.attr('height', height + margin.top + margin.bottom)
					.attr('class', 'box')
					.append('g')
					.attr('transform', `translate(${margin.left},${margin.top})`);

				const quartileData = [boxValues.q1, boxValues.median, boxValues.q2];

				// Compute the new vertical-scale.
				const vScale = d3.scale.linear()
					.domain([boxValues.min, boxValues.max])
					.range([height, 0]);

				// central vertical Axis
				const center = svg.append('g');
				center.append('line')
					.attr('class', 'center')
					.attr('x1', width / 2)
					.attr('y1', () => vScale(boxValues.min))
					.attr('x2', width / 2)
					.attr('y2', () => vScale(boxValues.max))
					.style('opacity', 1e-6)
					.transition()
					.duration(duration)
					.style('opacity', 1);

				// box plot
				const boxPlot = svg.append('g');

				// top box
				const boxTop = boxPlot.append('g').selectAll('rect')
					.data([quartileData]);

				boxTop.enter().append('rect')
					.attr('x', 0)
					.attr('y', d => vScale(d[2]))
					.attr('width', width)
					.attr('height', () => vScale(boxValues.q2));

				boxTop.transition()
					.duration(duration)
					.attr('height', d => vScale(d[1]) - vScale(d[2]));

				// bottom box.
				const boxBottom = boxPlot.append('g').selectAll('rect')
					.data([quartileData]);

				boxBottom.enter().append('rect')
					.attr('x', 0)
					.attr('y', () => vScale(boxValues.q2))
					.attr('width', width)
					.attr('height', d => vScale(d[0]) - vScale(d[2]));

				boxBottom.transition()
					.duration(duration * 1.5)
					.ease('bounce')
					.attr('y', d => vScale(d[1]))
					.attr('height', d => vScale(d[0]) - vScale(d[1]));

				// whiskers
				const topWhiskerPolyg = max => `0,${vScale(max)} ${width},${vScale(max)} ${width - 20},${vScale(max) - 20} ${vScale(max) + 20},${vScale(max) - 20}`;

				const bottomWhiskerPolyg = min => `0,${vScale(min)} ${width},${vScale(min)} ${width - 20},${vScale(min) + 20} 20,${vScale(min) + 20}`;

				const gWhisker = svg.append('g');

				const gWhiskerTop = gWhisker.append('g');

				gWhiskerTop.append('polygon')
					.attr('class', 'whiskerPolyg')
					.attr('points', topWhiskerPolyg(boxValues.max));

				gWhiskerTop.append('text')
					.attr('class', 'max-min-labels')
					.attr('x', width / 2)
					.attr('y', vScale(boxValues.max) / 2)
					.text($translate.instant('MAXIMUM'))
					.style('opacity', 1)
					.attr('text-anchor', 'middle')
					.transition()
					.duration(duration)
					.attr('y', -22)
					.style('opacity', 1);

				const gWhiskerBottom = gWhisker.append('g');

				gWhiskerBottom.append('polygon')
					.attr('class', 'whiskerPolyg')
					.attr('points', bottomWhiskerPolyg(boxValues.min));

				gWhiskerBottom.append('text')
					.attr('class', 'max-min-labels')
					.attr('x', width / 2)
					.attr('y', vScale(boxValues.min) / 2)
					.text($translate.instant('MINIMUM'))
					.style('opacity', 1)
					.attr('text-anchor', 'middle')
					.transition()
					.duration(duration)
					.attr('y', vScale(boxValues.min) + 30);

				// mean circle
				const gMean = svg.insert('g');
				gMean.append('circle')
					.attr('class', 'mean')
					.attr('r', 17)
					.attr('cx', width / 2)
					.attr('cy', () => vScale(boxValues.mean))
					.style('opacity', 1)
					.transition()
					.duration(duration * 2)
					.attr('r', 7)
					.style('opacity', 1)
				;

				gMean.append('circle')
					.attr('r', 1.5)
					.attr('cx', width / 2)
					.attr('cy', () => vScale(boxValues.mean))
					.style('opacity', 1e-6)
					.style('shape-rendering', 'geometricPrecision')
					.transition()
					.duration(duration * 2)
					.style('opacity', 1);

				// text values
				const gTexts = svg.append('g');
				// max
				gTexts.append('text')
					.attr('class', 'max-min-labels')
					.attr('x', width / 2)
					.attr('y', vScale(boxValues.min) / 2)
					.text(d3.format(',')(boxValues.max))
					.style('opacity', 1)
					.attr('text-anchor', 'middle')
					.transition()
					.duration(duration)
					.attr('y', vScale(boxValues.max) - 10);

				// min
				gTexts.append('text')
					.attr('class', 'max-min-labels')
					.attr('x', width / 2)
					.attr('y', vScale(boxValues.min) / 2)
					.text(d3.format(',')(boxValues.min))
					.style('opacity', 1)
					.attr('text-anchor', 'middle')
					.transition()
					.duration(duration)
					.attr('y', vScale(boxValues.min) + 17);

				gTexts.append('text')
					.attr('class', 'mean-labels')
					.attr('x', width / 2)
					.attr('y', () => {
						if (height - vScale(boxValues.mean - boxValues.min) < 15) {
							return vScale(boxValues.mean) - 10;
						}
						else if (height - vScale(boxValues.max - boxValues.mean) < 15) {
							return vScale(boxValues.mean) + 15;
						}
						else {
							return vScale(boxValues.mean) - 10;
						}
					})
					.text(`${$translate.instant('MEAN')}${$translate.instant('COLON')}` + d3.format(',')(boxValues.mean))
					.style('opacity', 1e-6)
					.attr('text-anchor', 'middle')
					.transition()
					.duration(duration * 2)
					.style('opacity', 1);

				// lower quantile value
				gTexts.append('text')
					.attr('class', 'low-quantile-labels')
					.attr('x', width + 5)
					.attr('y', vScale(boxValues.min) / 2)
					.text(d3.format(',')(boxValues.q1))
					.style('opacity', 1)
					.attr('text-anchor', 'start')
					.transition()
					.duration(duration)
					.attr('y', vScale(boxValues.q1) + 20);

				// lower quantile text
				gTexts.append('text')
					.attr('class', 'low-quantile-labels')
					.attr('x', 10)
					.attr('y', vScale(boxValues.min) / 2)
					.text($translate.instant('LOWER_QUANTILE'))
					.style('opacity', 1)
					.attr('text-anchor', 'end')
					.transition()
					.duration(duration)
					.attr('y', vScale(boxValues.q1) + 20);

				// upper quantile value
				gTexts.append('text')
					.attr('class', 'up-quantile-labels')
					.attr('x', 10)
					.attr('y', vScale(boxValues.min) / 2)
					.text($translate.instant('UPPER_QUANTILE'))
					.style('opacity', 1)
					.attr('text-anchor', 'end')
					.transition()
					.duration(duration)
					.attr('y', vScale(boxValues.q2) - 10);

				// upper quantile text
				gTexts.append('text')
					.attr('class', 'up-quantile-labels')
					.attr('x', width + 5)
					.attr('y', vScale(boxValues.min) / 2)
					.text(d3.format(',')(boxValues.q2))
					.style('opacity', 1)
					.attr('text-anchor', 'start')
					.transition()
					.duration(duration)
					.attr('y', vScale(boxValues.q2) - 10);

				// median value
				gTexts.append('text')
					.attr('x', width - 5)
					.text(d3.format(',')(boxValues.median))
					.style('opacity', 1e-6)
					.attr('text-anchor', 'end')
					.attr('y', () => {
						return boxValues.median >= boxValues.mean ? vScale(boxValues.median) + 15 : vScale(boxValues.median) - 5;
					})
					.transition()
					.duration(duration * 3)
					.style('opacity', 1);

				// median text
				gTexts.append('text')
					.attr('x', 5)
					.text($translate.instant('MEDIAN'))
					.style('opacity', 1e-6)
					.attr('text-anchor', 'start')
					.attr('y', () => {
						return boxValues.median >= boxValues.mean ? vScale(boxValues.median) + 15 : vScale(boxValues.median) - 5;
					})
					.transition()
					.duration(duration * 3)
					.style('opacity', 1);
			}

			scope.$watch('boxplotData',
				(boxData) => {
					element.empty();
					if (boxData) {
						$timeout.cancel(renderTimeout);
						renderTimeout = $timeout(renderBoxplotchart.bind(this, boxData), 100, false);
					}
				}
			);

			scope.$on('$destroy', () => {
				$timeout.cancel(renderTimeout);
			});
		},
	};
}
