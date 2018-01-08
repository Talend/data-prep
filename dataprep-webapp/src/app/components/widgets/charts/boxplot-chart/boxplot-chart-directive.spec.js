/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Boxplot chart directive', function () {
    'use strict';

    var createElement;
    var element;
    var scope;
    var boxValues;

    beforeEach(angular.mock.module('talend.widget'));
    beforeEach(inject(function ($rootScope, $compile) {
        boxValues = {
            min: 0,
            max: 100,
            q1: 8,
            q2: 90,
            median: 58,
            mean: 59.79,
            variance: 2051033.15,
        };

        createElement = function () {
            scope = $rootScope.$new();
            scope.boxValues = null;
            element = angular.element('<boxplot-chart id="boxplotId" width="200" height="400" boxplot-data="boxValues"></boxplot-chart>');

            angular.element('body').append(element);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should render the different basic components of the boxplot after a 100ms delay', inject(function ($timeout) {
        //given
        createElement();

        //when
        scope.boxValues = boxValues;
        scope.$digest();
        $timeout.flush(100);

        //then
        expect(element.find('rect').length).toBe(2);
        expect(element.find('.up-quantile-labels').length).toBe(2);
        expect(element.find('.low-quantile-labels').length).toBe(2);
        expect(element.find('.center').length).toBe(1);
        expect(element.find('.mean-labels').length).toBe(1);
        expect(element.find('.whiskerPolyg').length).toBe(2);
        expect(element.find('.max-min-labels').length).toBe(4);
    }));
});
