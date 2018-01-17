/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Horizontal Barchart directive', function () {
    'use strict';

    var createElement;
    var element;
    var scope;
    var statsData;
    var filteredStatsData;
    var flushAllD3Transitions = function () {
        var now = Date.now;
        Date.now = function () {
            return Infinity;
        };

        d3.timer.flush();
        Date.now = now;
    };

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(function ($rootScope, $compile) {
        statsData = [
            { data: 'Johnson', occurrences: 9 },
            { data: 'Roosevelt', occurrences: 8 },
            { data: 'Pierce', occurrences: 6 },
            { data: 'Wilson', occurrences: 5 },
            { data: 'Adams', occurrences: 4 },
            { data: 'Quincy', occurrences: 4 },
            { data: 'Clinton', occurrences: 4 },
            { data: 'Harrison', occurrences: 4 },
        ];
        filteredStatsData = [
            { data: 'Johnson', filteredOccurrences: 4 },
            { data: 'Roosevelt', filteredOccurrences: 4 },
            { data: 'Pierce', filteredOccurrences: 4 },
            { data: 'Wilson', filteredOccurrences: 4 },
            { data: 'Adams', filteredOccurrences: 4 },
            { data: 'Quincy', filteredOccurrences: 4 },
            { data: 'Clinton', filteredOccurrences: 4 },
            { data: 'Harrison', filteredOccurrences: 4 },
        ];

        scope = $rootScope.$new();
        scope.onClick = jasmine.createSpy('on bar click fn');

        createElement = function () {
            var html = '<horizontal-barchart ' +
                'id="barChart" ' +
                'width="250" ' +
                'height="400"' +
                'on-click="onClick(item)"' +
                'key-field="data"' +
                'primary-data="primaryData"' +
                'primary-value-field="occurrences"' +
                'primary-bar-class="{{primaryBarClass}}"' +
                'secondary-data="secondaryData"' +
                'secondary-value-field="filteredOccurrences"' +
                'secondary-bar-class="{{secondaryBarClass}}"' +
                '></horizontal-barchart>';

            element = angular.element(html);
            angular.element('body').append(element);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    describe('render', function () {
        it('should render labels after a 100ms delay', inject(function ($timeout) {
            //given
            createElement();

            //when
            scope.primaryData = statsData;
            scope.$digest();
            $timeout.flush(100);

            //then
            expect(element.find('.label').length).toBe(statsData.length);
            expect(element.find('.label').eq(0).text()).toBe('Johnson');
            expect(element.find('.label').eq(1).text()).toBe('Roosevelt');
            expect(element.find('.label').eq(2).text()).toBe('Pierce');
            expect(element.find('.label').eq(3).text()).toBe('Wilson');
            expect(element.find('.label').eq(4).text()).toBe('Adams');
            expect(element.find('.label').eq(5).text()).toBe('Quincy');
            expect(element.find('.label').eq(6).text()).toBe('Clinton');
            expect(element.find('.label').eq(7).text()).toBe('Harrison');
        }));

        it('should render hover rect after a 100ms delay', inject(function ($timeout) {
            //given
            createElement();

            //when
            scope.primaryData = statsData;
            scope.$digest();
            $timeout.flush(100);

            //then
            expect(element.find('.bg-rect').length).toBe(statsData.length);
        }));

        it('should render grid after a 100ms delay with maximum occurrences >= 1e9', inject(function ($timeout) {
            //given
            createElement();

            //when
            scope.primaryData = [
                { data: 'Johnson', occurrences: 9e9 },
                { data: 'Roosevelt', occurrences: 8 },
            ];
            scope.$digest();
            $timeout.flush(100);

            //then
            expect(element.find('.grid text').length).toBe(2);
            expect(element.find('.grid text').eq(0).text()).toBe('0');
            expect(element.find('.grid text').eq(1).text()).toBe('5,000,000,000');
        }));

        it('should render grid after a 100ms delay with (1e6<= maximum occurrences < 1e9)', inject(function ($timeout) {
            //given
            createElement();

            //when
            scope.primaryData = [
                { data: 'Johnson', occurrences: 1e8 },
                { data: 'Roosevelt', occurrences: 8 },
            ];
            scope.$digest();
            $timeout.flush(100);

            //then
            expect(element.find('.grid text').length).toBe(3);
            expect(element.find('.grid text').eq(0).text()).toBe('0');
            expect(element.find('.grid text').eq(1).text()).toBe('50,000,000');
            expect(element.find('.grid text').eq(2).text()).toBe('100,000,000');
        }));

        it('should render grid after a 100ms delay with (1e3<= maximum occurrences < 1e6)', inject(function ($timeout) {
            //given
            createElement();

            //when
            scope.primaryData = [
                { data: 'Johnson', occurrences: 1e5 },
                { data: 'Roosevelt', occurrences: 8 },
            ];
            scope.$digest();
            $timeout.flush(100);

            //then
            expect(element.find('.grid text').length).toBe(6);
            expect(element.find('.grid text').eq(0).text()).toBe('0');
            expect(element.find('.grid text').eq(1).text()).toBe('20,000');
            expect(element.find('.grid text').eq(2).text()).toBe('40,000');
            expect(element.find('.grid text').eq(3).text()).toBe('60,000');
            expect(element.find('.grid text').eq(4).text()).toBe('80,000');
            expect(element.find('.grid text').eq(5).text()).toBe('100,000');
        }));

        it('should render grid after a 100ms delay with maximum of ticks < 1e3', inject(function ($timeout) {
            //given
            createElement();

            //when
            scope.primaryData = statsData;
            scope.$digest();
            $timeout.flush(100);

            //then
            expect(element.find('.grid text').length).toBe(10);
            expect(element.find('.grid text').eq(0).text()).toBe('0');
            expect(element.find('.grid text').eq(1).text()).toBe('1');
            expect(element.find('.grid text').eq(2).text()).toBe('2');
            expect(element.find('.grid text').eq(3).text()).toBe('3');
            expect(element.find('.grid text').eq(4).text()).toBe('4');
            expect(element.find('.grid text').eq(5).text()).toBe('5');
            expect(element.find('.grid text').eq(6).text()).toBe('6');
            expect(element.find('.grid text').eq(7).text()).toBe('7');
            expect(element.find('.grid text').eq(8).text()).toBe('8');
            expect(element.find('.grid text').eq(9).text()).toBe('9');
        }));

        it('should render primary data after a 100ms delay', inject(function ($timeout) {
            //given
            createElement();

            //when
            scope.primaryData = statsData;
            scope.$digest();
            $timeout.flush(100);

            //then
            expect(element.find('.primaryBar > rect').length).toBe(statsData.length);
            expect(element.find('.secondaryBar > rect').length).toBe(0);
        }));

        it('should render primary and secondary data after a 100ms delay', inject(function ($timeout) {
            //given
            createElement();

            //when
            scope.primaryData = statsData;
            scope.secondaryData = filteredStatsData;
            scope.$digest();
            $timeout.flush(100);

            //then
            expect(element.find('.primaryBar > rect').length).toBe(statsData.length);
            expect(element.find('.secondaryBar > rect').length).toBe(statsData.length);
        }));

        it('should render secondary data after a 100ms delay', inject(function ($timeout) {
            //given
            createElement();
            scope.primaryData = statsData;
            scope.$digest();
            $timeout.flush(100);

            expect(element.find('.secondaryBar > rect').length).toBe(0);

            //when
            scope.secondaryData = filteredStatsData;
            scope.$digest();
            $timeout.flush(100);

            //then
            expect(element.find('.secondaryBar > rect').length).toBe(statsData.length);
        }));

        it('should render tiny bars with a 3px width bar', inject(function ($timeout) {
            //given
            createElement();

            //when
            scope.primaryData = [
                { data: 'Johnson', occurrences: 90000000 },
                { data: 'Hoover', occurrences: 0 },
                { data: 'Roosevelt', occurrences: 1 },
            ];
            scope.$digest();
            $timeout.flush(100);
            flushAllD3Transitions();

            //then
            expect(element.find('.primaryBar > rect').eq(0).attr('width')).toBe('220');
            expect(element.find('.primaryBar > rect').eq(1).attr('width')).toBe('0');
            expect(element.find('.primaryBar > rect').eq(2).attr('width')).toBe('3');
        }));
    });

    describe('bar class name', function () {
        it('should set "transparentBar" class as primary bars default class name', inject(function ($timeout) {
            //given
            createElement();

            //when
            scope.primaryData = statsData;
            scope.$digest();
            $timeout.flush(100);

            //then
            expect(element.find('.primaryBar > rect.transparentBar').length).toBe(statsData.length);
        }));

        it('should set "blueBar" class as secondary bars default class name', inject(function ($timeout) {
            //given
            createElement();

            //when
            scope.primaryData = statsData;
            scope.secondaryData = filteredStatsData;
            scope.$digest();
            $timeout.flush(100);

            //then
            expect(element.find('.secondaryBar > rect.blueBar').length).toBe(statsData.length);
        }));

        it('should set custom secondary bars class name', inject(function ($timeout) {
            //given
            createElement();

            //when
            scope.primaryData = statsData;
            scope.primaryBarClass = 'blueBar';
            scope.secondaryData = filteredStatsData;
            scope.secondaryBarClass = 'brownBar';
            scope.$digest();
            $timeout.flush(100);

            //then
            expect(element.find('.primaryBar > rect.blueBar').length).toBe(statsData.length);
            expect(element.find('.secondaryBar > rect.brownBar').length).toBe(statsData.length);
        }));
    });
});
