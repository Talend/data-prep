/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Actions header', () => {
    'use strict';

    let scope;
    let createElement;
    let element;
    let stateMock;

    beforeEach(angular.mock.module('data-prep.actions-header', ($provide) => {
        stateMock = {
            playground: {
                grid: {},
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            MULTI_COLUMNS_SELECTED: '{{nb}} columns selected',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        createElement = () => {
            element = angular.element('<actions-header></actions-header>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should set column name in title', () => {
        //given
        stateMock.playground.grid.selectedColumns = [{ name: 'Col 1' }];

        //when
        createElement();

        //then
        expect(element.find('.title').text().trim()).toBe('Col 1');
    });

    it('should set the number of the selected columns in title', () => {
        //given
        stateMock.playground.grid.selectedColumns = [
            { name: 'Col 1' },
            { name: 'Col 3' },
        ];

        //when
        createElement();

        //then
        expect(element.find('.title').text().trim()).toBe('2 columns selected');
    });
});
