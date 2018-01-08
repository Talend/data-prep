/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Filter list directive', function () {
    'use strict';

    var scope;
    var createElement;
    var element;

    var filters = [
        {
            type: 'exact',
            colId: '0000',
            colName: 'name',
            editable: true,
            args: {
                phrase: [
                    {
                        value: 'AMC Gremlin',
                    },
                ],
                caseSensitive: true,
            },
            value: [
                {
                    value: 'AMC Gremlin',
                },
            ],
        },
        {
            type: 'exact',
            colId: '0000',
            colName: 'name',
            editable: true,
            args: {
                phrase: [
                    {
                        value: 'Chevrolet Caprice Classic',
                    },
                ],
                caseSensitive: true,
            },
            value: [
                {
                    value: 'Chevrolet Caprice Classic',
                },
            ],
        },
        {
            type: 'exact',
            colId: '0000',
            colName: 'lastname',
            editable: true,
            args: {
                phrase: [
                    {
                        value: 'Audi 100 LS',
                    },
                ],
                caseSensitive: true,
            },
            value: [
                {
                    value: 'Audi 100 LS',
                },
            ],
        },
        {
            type: 'inside_range',
            colId: '0003',
            colName: 'displacement (cc)',
            editable: false,
            args: {
                intervals: [
                    {
                        value: [214, 233],
                    },
                ],
            },
            value: [
                {
                    value: '[214 .. 233]',
                },
            ],
        },
    ];

    beforeEach(angular.mock.module('data-prep.filter-list'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<filter-list filters="filters"></filter-list>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should render filter list badges', function () {
        //given
        scope.filters = filters;

        //when
        createElement();

        //then
        expect(element.find('filter-item').length).toBe(4);
    });
});
