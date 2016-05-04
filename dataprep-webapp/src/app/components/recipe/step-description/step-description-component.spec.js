/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/


describe('Step Description Component', () => {

    let scope, createElement, element;

    beforeEach(angular.mock.module('data-prep.step-description'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            'RECIPE_ITEM_ON_COL': 'on column <b>{{columnName}}</b>',
            'RECIPE_ITEM_ON_CELL': 'on cell',
            'RECIPE_ITEM_ON_LINE': 'on line <b>#{{rowId}}</b>',

            'LOOKUP_STEP_DESCRIPTION': 'done with dataset <span class=\"recipe-column-name\">{{lookupDsName}}</span>. Join has been set between <span class=\"recipe-column-name\">{{mainColName}}</span> and <span class=\"recipe-column-name\">{{lookupColName}}. </span>',
            'ONLY_1_ADDED_COL': 'The column <span class=\"recipe-column-name\">{{firstCol}}</span> has been added.',
            'ONLY_2_ADDED_COLS': 'The columns <span class=\"recipe-column-name\">{{firstCol}}</span> and <span class=\"recipe-column-name\">{{secondCol}}</span> have been added.',
            'MORE_THEN_2_ADDED_COLS': 'The columns <span class=\"recipe-column-name\">{{firstCol}}</span>, <span class=\"recipe-column-name\">{{secondCol}}</span> and <span class=\"recipe-column-name\" title=\"{{restOfCols}}\">{{restOfColsNbr}}</span> other(s) have been added.',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        createElement = () => {
            element = angular.element('<step-description step="step"></step-description>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    describe('on column scope', () => {
        it('should render the column name', () => {
            //given
            scope.step = {
                column: { id: '0', name: 'col1' },
                transformation: {
                    stepId: '13a24e8765ef4',
                    name: 'split',
                    label: 'Split',
                    category: 'split',
                    parameters: [{ name: 'pattern', type: 'string' }],
                    items: [],
                },
                actionParameters: {
                    action: 'split',
                    parameters: {
                        scope: 'column',
                        column_id: '0',
                        pattern: '/',
                    },
                },
            };

            //when
            createElement();
            scope.$digest();

            //then
            expect(element.eq(0).text().trim().replace(/\s+/g, ' ')).toBe('on column COL1');
        });
    });

    describe('on cell scope', () => {
        it('should render cell action', () => {
            //given
            scope.step = {
                column: { id: '1', name: 'col2' },
                transformation: {
                    stepId: '456bb784a9674e532fc446',
                    name: 'replace_on_value',
                    label: 'Replace value',
                    category: 'quickfix',
                    parameters: [
                        { name: 'cell_value', type: 'string' },
                        { name: 'replace_value', type: 'string' },
                    ],
                },
                actionParameters: {
                    action: 'quickfix',
                    parameters: {
                        scope: 'cell',
                        column_id: '1',
                        row_id: 56,
                    },
                },
                inactive: true,
            };

            //when
            createElement();
            scope.$digest();

            //then
            expect(element.eq(0).text().trim().replace(/\s+/g, ' ')).toBe('on cell');
        });
    });

    describe('on line scope', () => {
        it('should render the row number', () => {
            //given
            scope.step = {
                column: { id: undefined, name: undefined },
                row: { id: 125 },
                transformation: {
                    stepId: '3213ca58454a58d436',
                    name: 'delete',
                    label: 'Delete Line',
                    category: 'clean',
                    parameters: null,
                },
                actionParameters: {
                    action: 'delete',
                    parameters: {
                        scope: 'line',
                        column_id: undefined,
                        row_id: 125,
                    },
                },
                inactive: true,
            };

            //when
            createElement();
            scope.$digest();

            //then
            expect(element.eq(0).text().trim().replace(/\s+/g, ' ')).toBe('on line #125');
        });
    });

    describe('on dataset scope', () => {
        it('should show the lookup details', ()=> {
            //given
            scope.step = {
                'column': {
                    'id': '0000',
                    'name': 'id',
                },
                transformation: {
                    parameters: [],
                    label: 'Lookup',
                    name: 'lookup',
                },
                'actionParameters': {
                    'action': 'lookup',
                    'parameters': {
                        'column_id': '0000',
                        'filter': '',
                        'lookup_ds_name': 'customers_100_with_pb',
                        'lookup_ds_id': '14d116a0-b180-4c5f-ba25-46807fc61e42',
                        'lookup_ds_url': 'http://172.17.0.30:8080/datasets/14d116a0-b180-4c5f-ba25-46807fc61e42/content?metadata=true',
                        'lookup_join_on': '0000',
                        'lookup_join_on_name': 'id',
                        'lookup_selected_cols': [
                            {
                                'name': 'firstname',
                                'id': '0001'
                            },
                            {
                                'name': 'lastname',
                                'id': '0002'
                            },
                            {
                                'name': 'state',
                                'id': '0003'
                            },
                            {
                                'name': 'registration',
                                'id': '0004'
                            }
                        ],
                        'column_name': 'id',
                        'scope': 'dataset',
                    },
                },
            };

            //when
            createElement();
            scope.$digest();

            //then
            expect(element.eq(0).text().trim().replace(/\s+/g, ' ')).toBe('done with dataset customers_100_with_pb. Join has been set between id and id. The columns firstname, lastname and 2 other(s) have been added.');
        });
    });
});