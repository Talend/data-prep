/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('verticalBarchart directive', () => {
	'use strict';

	let createElement;
	let element;
	let scope;
	let statsData;
	let secondaryStatsData;
	let isolateScope;

	function flushAllD3Transitions() {
		const now = Date.now;
		Date.now = function () {
			return Infinity;
		};

		d3.timer.flush();
		Date.now = now;
	}

	beforeEach(angular.mock.module('talend.widget'));

	beforeEach(inject(($rootScope, $compile) => {
		statsData = [
			{ data: { min: 0, max: 5, label: '[0 .. 5[' }, occurrences: 9 },
			{ data: { min: 5, max: 10 }, occurrences: 8 },
			{ data: { min: 10, max: 15 }, occurrences: 6 },
			{ data: { min: 15, max: 20 }, occurrences: 5 },
		];
		secondaryStatsData = [
			{ data: { min: 0, max: 5 }, filteredOccurrences: 9 },
			{ data: { min: 5, max: 10 }, filteredOccurrences: 8 },
			{ data: { min: 10, max: 15 }, filteredOccurrences: 6 },
			{ data: { min: 15, max: 20 }, filteredOccurrences: 5 },
		];

		createElement = () => {
			scope = $rootScope.$new();
			scope.onClick = jasmine.createSpy('onClick');

			element = angular.element('<vertical-barchart id="barChart" width="250" height="400"' +
				'show-x-axis="showXAxis"' +
				'on-click="onClick(interval)"' +
				'key-field="data"' +
				'key-label="Occurrences"' +
				'feature="{{feature}}"' +
				'primary-data="primaryData"' +
				'primary-value-field="occurrences"' +
				'secondary-data="secondaryData"' +
				'secondary-value-field="filteredOccurrences"' +
				'active-limits="activeLimits"' +
				'></vertical-barchart>');

			angular.element('body').append(element);
			$compile(element)(scope);
			scope.$digest();

			isolateScope = element.isolateScope();
		};
	}));

	beforeEach(() => {
		jasmine.clock().install();
	});

	afterEach(() => {
		jasmine.clock().uninstall();

		scope.$destroy();
		element.remove();
	});

	describe('render', () => {
		it('should set custom data-feature on bars', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.feature = 'my.feature';

			scope.$digest();
			$timeout.flush(100);

			expect(element.find('[data-feature="my.feature"]').length).toBe(statsData.length);
		}));

		it('should render svg container with adapted bottom margin: with X-axis', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.secondaryData = secondaryStatsData;
			scope.keyField = 'data';
			scope.showXAxis = true;
			scope.$digest();
			$timeout.flush(100);

			expect(+element.find('.vertical-barchart-cls').attr('height')).toBe(400 + statsData[0].data.label.length * 8);
		}));

		it('should render svg container with adapted bottom margin: without X-axis', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.secondaryData = secondaryStatsData;
			scope.keyField = 'data';
			scope.showXAxis = false;
			scope.$digest();
			$timeout.flush(100);

			expect(+element.find('.vertical-barchart-cls').attr('height')).toBe(400 + 10);
		}));

		it('should render y axis after a 100ms delay', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.$digest();
			$timeout.flush(100);

			expect(element.find('.yAxis > text').length).toBe(1);
			expect(element.find('.yAxis > text').text()).toBe('Occurrences');
		}));

		it('should render grid after a 100ms delay', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.$digest();
			$timeout.flush(100);

			expect(element.find('.grid > .tick').length).toBe(10);
			expect(element.find('.grid > .tick').eq(0).text()).toBe('0');
			expect(element.find('.grid > .tick').eq(1).text()).toBe('1');
			expect(element.find('.grid > .tick').eq(2).text()).toBe('2');
			expect(element.find('.grid > .tick').eq(3).text()).toBe('3');
			expect(element.find('.grid > .tick').eq(4).text()).toBe('4');
			expect(element.find('.grid > .tick').eq(5).text()).toBe('5');
			expect(element.find('.grid > .tick').eq(6).text()).toBe('6');
			expect(element.find('.grid > .tick').eq(7).text()).toBe('7');
			expect(element.find('.grid > .tick').eq(8).text()).toBe('8');
			expect(element.find('.grid > .tick').eq(9).text()).toBe('9');
		}));

		it('should render hover bars after a 100ms delay', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.$digest();
			$timeout.flush(100);

			expect(element.find('.bg-rect').length).toBe(statsData.length);
		}));

		it('should render primary bars after a 100ms delay', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.$digest();
			$timeout.flush(100);

			expect(element.find('.primaryBar > rect').length).toBe(statsData.length);
			expect(element.find('.secondaryBar > rect').length).toBe(0);
			expect(element.find('.grid').length).toBe(1);
			expect(element.find('.bg-rect').length).toBe(statsData.length);
		}));

		it('should render x-axis', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.secondaryData = secondaryStatsData;
			scope.keyField = 'data';
			scope.showXAxis = true;
			scope.$digest();
			$timeout.flush(100);

			expect(element.find('.x.axis').length).toBe(1);
		}));

		it('should NOT render x-axis', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.secondaryData = secondaryStatsData;
			scope.dataType = false;
			scope.$digest();
			$timeout.flush(100);

			expect(element.find('.x.axis').length).toBe(0);
		}));

		it('should render primary and secondary bars after a 100ms delay', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.secondaryData = secondaryStatsData;
			scope.$digest();
			$timeout.flush(100);

			expect(element.find('.primaryBar > rect').length).toBe(statsData.length);
			expect(element.find('.secondaryBar > rect').length).toBe(statsData.length);
		}));

		it('should render secondary bars after a 100ms delay', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.$digest();
			$timeout.flush(100);

			expect(element.find('.secondaryBar > rect').length).toBe(0);

			scope.secondaryData = secondaryStatsData;
			scope.$digest();
			$timeout.flush(100);

			expect(element.find('.secondaryBar > rect').length).toBe(statsData.length);
		}));

		it('should render tiny bars with a 3px height bar', inject(($timeout) => {
			createElement();

			scope.primaryData = [
				{ data: { min: 0, max: 5 }, occurrences: 9000000 },
				{ data: { min: 5, max: 10 }, occurrences: 0 },
				{ data: { min: 10, max: 15 }, occurrences: 1 },
			];
			scope.$digest();
			$timeout.flush(100);
			flushAllD3Transitions();

			// 400: passed chart height, 20: top margin to which the svg was translated, 3: the default tiny bar value
			expect(element.find('.primaryBar > rect').eq(0).attr('height')).toBe('380'); // 400 - 20px margin
			expect(element.find('.primaryBar > rect').eq(1).attr('height')).toBe('0');
			expect(element.find('.primaryBar > rect').eq(2).attr('height')).toBe('3');
			expect(element.find('.primaryBar > rect').eq(0).attr('y')).toBe('0');
			expect(element.find('.primaryBar > rect').eq(1).attr('y')).toBe('380'); // 400 - 20px margin
			expect(element.find('.primaryBar > rect').eq(2).attr('y')).toBe('377'); // 400 - 20px margin - 3px bar height
		}));
	});

	describe('active bars', () => {
		it('should set the initial bars to full opacity', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.$digest();
			$timeout.flush(100);

			_.each(isolateScope.buckets[0], (bucket) => {
				expect(d3.select(bucket).style('opacity')).toBe('1');
			});
		}));

		it('should set the bars to inactive opacity = 0.4', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.$digest();
			$timeout.flush(100);
			flushAllD3Transitions();

			scope.activeLimits = [105, 200];
			scope.$digest();
			$timeout.flush(500);
			flushAllD3Transitions();

			_.each(isolateScope.buckets[0], (bucket) => {
				const opacity = Number(d3.select(bucket)
					.style('opacity'))
					.toFixed(1);
				expect(opacity).toBe('0.4');
			});
		}));

		it('should update the bars opacity depending on the active limits', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.$digest();
			$timeout.flush(100);
			flushAllD3Transitions();

			scope.activeLimits = [15, 20];
			scope.$digest();
			$timeout.flush(600);
			flushAllD3Transitions();

			const expectedOpacities = ['0.4', '0.4', '0.4', '1.0'];

			_.each(isolateScope.buckets[0], (bucket, index) => {
				const opacity = Number(d3.select(bucket)
					.style('opacity'))
					.toFixed(1);
				expect(opacity).toBe(expectedOpacities[index]);
			});
		}));

		it('should set bars opacity to full opacity when it is in the intersection or a limit', inject(($timeout) => {
			createElement();

			scope.primaryData = statsData;
			scope.$digest();
			$timeout.flush(100);
			flushAllD3Transitions();

			scope.activeLimits = [13, 20];
			scope.$digest();
			$timeout.flush(600);

			flushAllD3Transitions();

			const expectedOpacities = ['0.4', '0.4', '1.0', '1.0'];

			_.each(isolateScope.buckets[0], (bucket, index) => {
				const opacity = Number(d3.select(bucket)
					.style('opacity'))
					.toFixed(1);
				expect(opacity).toBe(expectedOpacities[index]);
			});
		}));
	});
});
