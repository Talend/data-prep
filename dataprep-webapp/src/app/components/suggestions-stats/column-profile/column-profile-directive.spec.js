/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Column Profile directive', () => {
    'use strict';

    let scope;
    let createElement;
    let element;
    let stateMock;

    beforeEach(angular.mock.module('data-prep.suggestions-stats', ($provide) => {
        stateMock = {
            playground: {
                suggestions: {
                    isLoading: false,
                },
                statistics: {},
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $compile, $timeout) => {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<column-profile></column-profile>');
            angular.element('body').append(element);
            $compile(element)(scope);
            scope.$digest();
            $timeout.flush();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render chart ghost when fetching statistics', inject(() => {
        //given
        stateMock.playground.isFetchingStats = true;

        //when
        createElement();

        //then
        expect(element.find('#chart-ghost').length).toBe(1);
        expect(element.find('#column-profile-chart').length).toBe(0);
    }));

    it('should render column profile options', () => {
        //when
        createElement();

        //then
        expect(element.find('column-profile-options').length).toBe(1);
    });

    it('should render a chart div with "insertion-charts" attribute', () => {
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
