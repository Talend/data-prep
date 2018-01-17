/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Column profile options controller', () => {
    'use strict';

    let createController;
    let scope;

    const numericColumns = [
        { id: '0', name: 'revenue' },
        { id: '1', name: 'year' },
        { id: '2', name: 'age' },
    ];
    const initialColumn = { id: '0', name: 'revenue' };
    const initialAggreg = 'MAX';
    const group = { id: '', name: 'id' };

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en_US', {
            AVERAGE: 'Average',
            MAX: 'Max',
            MIN: 'Min',
            SUM: 'Sum',
            CURRENT_NO_AGGREGATION: 'No aggregation',
            REMOVE: 'Remove',
            CANCEL: 'Cancel',
            OK: 'Ok',
            AGGREGATION_DETAILS: '{{aggreg}} <span class=\"highlight\">{{col}}</span> group by <span class=\"highlight\">{{group}}</span>',
        });
        $translateProvider.preferredLanguage('en_US');
    }));

    beforeEach(angular.mock.module('data-prep.column-profile-options'));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        createController = () => $componentController(
            'columnProfileOptions',
            { $scope: scope },
            {
                numericColumns: numericColumns,
                column: initialColumn,
                aggregation: initialAggreg,
                group: group,
                onAggregationChange: jasmine.createSpy('onAggregationChange'),
            }
        );
    }));

    describe('on init', () => {
        it('should create current aggregation description', () => {
            // given
            const ctrl = createController();

            // when
            ctrl.$onInit();
            scope.$digest();

            // then
            expect(ctrl.description).toBe('Max <span class="highlight">revenue</span> group by <span class="highlight">id</span>');
        });

        it('should create current aggregation short description', () => {
            // given
            const ctrl = createController();

            // when
            ctrl.$onInit();
            scope.$digest();

            // then
            expect(ctrl.shortDescription).toBe('Max revenue');
        });

        it('should init selected values to the current aggregation', () => {
            // given
            const ctrl = createController();

            // when
            ctrl.$onInit();

            // then
            expect(ctrl.selected.column).toBe(initialColumn);
            expect(ctrl.selected.aggregation).toBe(initialAggreg);
        });

        it('should create selected aggregation description', () => {
            // given
            const ctrl = createController();

            // when
            ctrl.$onInit();
            scope.$digest();

            // then
            expect(ctrl.selected.description).toBe('Max <span class="highlight">revenue</span> group by <span class="highlight">id</span>');
        });

        it('should init filtered numeric columns', () => {
            // given
            const ctrl = createController();

            // when
            ctrl.$onInit();
            scope.$digest();

            // then
            expect(ctrl.filteredNumericColumns).toEqual(numericColumns);
        });
    });

    describe('on changes', () => {
        describe('#column', () => {
            let ctrl;

            beforeEach(() => {
                // given
                ctrl = createController();
                ctrl.description = '';
                ctrl.shortDescription = '';

                // when
                ctrl.column = { id: '5', name: 'year' };
                ctrl.$onChanges({ column: {} });
                scope.$digest();
            });

            it('should update current aggregation description', () => {
                // then
                expect(ctrl.description).toBe('Max <span class="highlight">year</span> group by <span class="highlight">id</span>');
            });

            it('should update current aggregation short description', () => {
                // then
                expect(ctrl.shortDescription).toBe('Max year');
            });
        });

        describe('#aggregation', () => {
            let ctrl;

            beforeEach(() => {
                // given
                ctrl = createController();
                ctrl.description = '';
                ctrl.shortDescription = '';

                // when
                ctrl.aggregation = 'AVERAGE';
                ctrl.$onChanges({ aggregation: {} });
                scope.$digest();
            });

            it('should update current aggregation description', () => {
                // then
                expect(ctrl.description).toBe('Average <span class="highlight">revenue</span> group by <span class="highlight">id</span>');
            });

            it('should update current aggregation short description', () => {
                // then
                expect(ctrl.shortDescription).toBe('Average revenue');
            });
        });

        describe('#group', () => {
            let ctrl;

            beforeEach(() => {
                // given
                ctrl = createController();
                ctrl.description = '';
                ctrl.shortDescription = '';
                ctrl.filteredNumericColumns = [];

                // when
                ctrl.group = numericColumns[1];
                ctrl.$onChanges({ group: {} });
                scope.$digest();
            });

            it('should update current aggregation description', () => {
                // then
                expect(ctrl.description).toBe('Max <span class="highlight">revenue</span> group by <span class="highlight">year</span>');
            });

            it('should update current aggregation short description', () => {
                // then
                expect(ctrl.shortDescription).toBe('Max revenue');
            });

            it('should update filtered numeric columns', () => {
                // then
                expect(ctrl.filteredNumericColumns).toEqual([numericColumns[0], numericColumns[2]]);
            });
        });

        describe('#numericColumns', () => {
            let ctrl;

            beforeEach(() => {
                // given
                ctrl = createController();
                ctrl.group = { id: '2', name: 'titi' };
                ctrl.filteredNumericColumns = [];

                // when
                ctrl.numericColumns = [
                    { id: '0', name: 'toto' },
                    { id: '1', name: 'tata' },
                    { id: '2', name: 'titi' },
                ];
                ctrl.$onChanges({ numericColumns: {} });
                scope.$digest();
            });

            it('should update filtered numeric columns', () => {
                // then
                expect(ctrl.filteredNumericColumns).toEqual([
                    { id: '0', name: 'toto' },
                    { id: '1', name: 'tata' },
                ]);
            });
        });
    });

    describe('select column', () => {
        it('should set aggregation to null when there is no selected column', () => {
            // given
            const ctrl = createController();
            ctrl.selected = {
                column: '',
                aggregation: 'MAX',
            };

            // when
            ctrl.selectColumn();

            // then
            expect(ctrl.selected.aggregation).toBe(null);
        });

        it('should init selected aggregation to the current aggregation type', () => {
            // given
            const ctrl = createController();
            ctrl.selected = {
                column: { name: 'id' },
                aggregation: undefined,
            };

            // when
            ctrl.selectColumn();

            // then
            expect(ctrl.selected.aggregation).toBe(initialAggreg);
        });

        it('should init selected aggregation to the "SUM" if there is no current aggregation', () => {
            // given
            const ctrl = createController();
            ctrl.selected = {
                column: { name: 'id' },
                aggregation: undefined,
            };
            ctrl.aggregation = undefined;

            // when
            ctrl.selectColumn();

            // then
            expect(ctrl.selected.aggregation).toBe('SUM');
        });

        it('should update selected aggregation description', () => {
            // given
            const ctrl = createController();
            ctrl.selected = {
                column: { name: 'id' },
                aggregation: undefined,
            };

            // when
            ctrl.selectColumn();

            // then
            expect(ctrl.selected.description).toBe('Max <span class="highlight">id</span> group by <span class="highlight">id</span>');
        });
    });

    describe('reset selected', () => {
        it('should init selected values to the current aggregation', () => {
            // given
            const ctrl = createController();
            ctrl.selected = {
                column: '',
                aggregation: undefined,
            };

            // when
            ctrl.resetSelected();

            // then
            expect(ctrl.selected.column).toBe(initialColumn);
            expect(ctrl.selected.aggregation).toBe(initialAggreg);
        });

        it('should update selected aggregation description', () => {
            // given
            const ctrl = createController();
            ctrl.selected = {
                column: '',
                aggregation: undefined,
            };

            // when
            ctrl.resetSelected();

            // then
            expect(ctrl.selected.description).toBe('Max <span class="highlight">revenue</span> group by <span class="highlight">id</span>');
        });
    });

    describe('change aggregation', () => {
        it('should do nothing when selected aggregation is the current one', () => {
            // given
            const ctrl = createController();
            ctrl.selected = {
                column: initialColumn,
                aggregation: initialAggreg,
            };

            // when
            ctrl.changeAggregation();

            // then
            expect(ctrl.onAggregationChange).not.toHaveBeenCalled();
        });

        it('should call change callback when selected aggregation has changed', () => {
            // given
            const ctrl = createController();
            ctrl.selected = {
                column: numericColumns[2],
                aggregation: 'AVERAGE',
            };

            expect(ctrl.onAggregationChange).not.toHaveBeenCalled();

            // when
            ctrl.changeAggregation();

            // then
            expect(ctrl.onAggregationChange).toHaveBeenCalledWith({
                column: numericColumns[2],
                aggregation: 'AVERAGE',
            });
        });
    });

    describe('remove aggregation', () => {
        it('should call change callback with empty aggregation', () => {
            // given
            const ctrl = createController();
            ctrl.selected = {
                column: numericColumns[2],
                aggregation: 'AVERAGE',
            };

            expect(ctrl.onAggregationChange).not.toHaveBeenCalled();

            // when
            ctrl.removeAggregation();

            // then
            expect(ctrl.selected.column).toBeUndefined();
            expect(ctrl.selected.aggregation).toBeUndefined();
            expect(ctrl.onAggregationChange).toHaveBeenCalledWith({
                column: undefined,
                aggregation: undefined,
            });
        });
    });

    describe('selected aggregation description', () => {
        it('should update description', () => {
            // given
            const ctrl = createController();
            ctrl.selected = {
                column: numericColumns[2],
                aggregation: 'AVERAGE',
            };

            // when
            ctrl.updateSelectedDescription();

            // then
            expect(ctrl.selected.description).toBe('Average <span class="highlight">age</span> group by <span class="highlight">id</span>');
        });
    });
});
