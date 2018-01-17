/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transformation column param directive', () => {
    'use strict';
    let scope;
    let createElement;

    let stateMock = {
        playground: {
            // available dataset/preparation columns
            data: {
                metadata: {
                    columns: [
                        { id: '0001', name: 'first name' },
                        { id: '0002', name: 'last name' },
                        { id: '0003', name: 'birth date' },
                    ],
                },
            },
            grid: {
                // selected column
                selectedColumn: {},
            },
        },
    };

    beforeEach(angular.mock.module('data-prep.transformation-form', ($provide) => {

        // set the selected column to the first one
        stateMock.playground.grid.selectedColumns = [stateMock.playground.data.metadata.columns[0]];

        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = () => {
            let element = angular.element('<transform-column-param parameter="parameter" is-readonly="isReadonly"></transform-column-param>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    it('should render an action with a column parameter', () => {
        //given
        scope.parameter = {
            name: 'selected_column',
            type: 'column',
            implicit: false,
            canBeBlank: false,
            description: 'Combine the content of this column with the current one',
            label: 'The Column to concatenate',
            default: '',
        };

        //when
        let element = createElement();

        //then
        expect(element.find('.param-name').text().trim()).toBe('The Column to concatenate:');
        expect(element.find('.param-input').find('option').length).toBe(stateMock.playground.data.metadata.columns.length);
    });

    it('should render an action with a column parameter in readonly mode', () => {
        //given
        scope.parameter = {
            name: 'selected_column',
            type: 'column',
            implicit: false,
            canBeBlank: false,
            description: 'Combine the content of this column with the current one',
            label: 'The Column to concatenate',
            default: '',
            value: '0001',
        };
        scope.isReadonly = true;

        //when
        let element = createElement();

        //then
        expect(element.find('.param-name').text().trim()).toBe('The Column to concatenate:');
        expect(element.find('.param-input-label').text().trim()).toBe('first name');
    });
});
