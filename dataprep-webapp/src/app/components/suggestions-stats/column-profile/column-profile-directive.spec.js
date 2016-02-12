/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Suggestions Profile directive', function () {
    'use strict';

    var scope, createElement, element, stateMock;

    beforeEach(angular.mock.module('data-prep.suggestions-stats', function ($provide) {
        stateMock = {
            playground: {
                suggestions: {
                    isLoading: false
                },
                statistics: {}
            }
        };
        $provide.constant('state', stateMock);
    }));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function ($rootScope, $compile, $timeout) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<column-profile></column-profile>');
            angular.element('body').append(element);
            $compile(element)(scope);
            scope.$digest();
            $timeout.flush();
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should render chart ghost when fetching statistics', inject(function ($q, PlaygroundService) {
        //given
        spyOn(PlaygroundService, 'updateStatistics').and.returnValue($q.when());
        stateMock.playground.statistics.histogram = null;

        //when
        createElement();

        //then
        expect(element.find('#chart-ghost').length).toBe(1);
        expect(element.find('#column-profile-chart').length).toBe(0);
    }));

    it('should render a control div with "insertion-charts-control" attribute', function () {
        //given
        stateMock.playground.statistics.histogram = {};

        //when
        createElement();

        //then
        var control = element.find('#column-profile-chart > .column-profile-options > .column-profile-chart-control');
        expect(control.length).toBe(1);
        expect(control.eq(0)[0].hasAttribute('insertion-charts-control')).toBe(true);
    });

    it('should render a chart div with "insertion-charts" attribute', function () {
        //given
        stateMock.playground.statistics.histogram = {};

        //when
        createElement();

        //then
        var control = element.find('#column-profile-chart > #column-profile-chart-container');
        expect(control.length).toBe(1);
        expect(control.eq(0)[0].hasAttribute('insertion-charts')).toBe(true);
    });
});