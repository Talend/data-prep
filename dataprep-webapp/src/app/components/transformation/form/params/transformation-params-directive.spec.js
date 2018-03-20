/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transformation params directive', function () {
    'use strict';
    var scope;
    var createElement;
    var stateMock;

    beforeEach(angular.mock.module('data-prep.transformation-form', function ($provide) {
        stateMock = { playground: {
                data: { metadata: { columns: [] } },
                grid: { selectedColumn: '0001' },
            }, };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function () {
            var element = angular.element('<transform-params parameters="parameters"></transform-params>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    afterEach(function () {
        scope.$destroy();
    });

    it('should render simple parameters', function () {
        //given
        scope.parameters = [
            {
                name: 'param1',
                label: 'Param 1',
                type: 'string',
                inputType: 'text',
                default: '.',
            },
            {
                name: 'param2',
                label: 'Param 2',
                type: 'integer',
                inputType: 'number',
                default: '5',
            },
        ];

        //when
        var element = createElement();

        //then
        expect(element.find('transform-simple-param').length).toBe(2);
    });

    it('should render regex parameters', function () {
        //given
        scope.parameters = [
            {
                name: 'param1',
                label: 'Param 1',
                type: 'regex',
                default: '',
            },
        ];

        //when
        var element = createElement();

        //then
        expect(element.find('transform-regex-param').length).toBe(1);
    });

    it('should render choice', function () {
        //given
        scope.parameters = [
            {
                name: 'myChoice',
                label: 'my choice',
                type: 'select',
                configuration: {
                    values: [
                        { name: 'noParamChoice1', value: 'noParamChoice1' },
                        { name: 'noParamChoice2', value: 'noParamChoice2' },
                    ],
                },
                default: '',
            },
        ];

        //when
        var element = createElement();

        //then
        expect(element.find('transform-choice-param').length).toBe(1);
    });

    it('should render date', function () {
        //given
        scope.parameters = [
            {
                name: 'param1',
                label: 'Param 1',
                type: 'date',
                default: '02/01/2012 10:00:12',
            },
        ];

        //when
        var element = createElement();

        //then
        expect(element.find('transform-date-param').length).toBe(1);
    });

    it('should render column choice', function () {
        //given
        scope.parameters = [
            {
                name: 'selected_column',
                type: 'column',
                implicit: false,
                canBeBlank: false,
                description: 'Combine the content of this column with the current one',
                label: 'The Column to concatenate',
                default: '',
            },
        ];

        //when
        var element = createElement();

        //then
        expect(element.find('transform-column-param').length).toBe(1);
    });
});
