/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Tab item controller', () => {
    'use strict';

    let createController;
    let scope;
    let stateMock;

    beforeEach(angular.mock.module('data-prep.tab-item', ($provide) => {
        stateMock = {
            playground: {
                filter: {},
                grid: {
                    selectedColumns: [],
                },
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $controller) => {
        scope = $rootScope.$new();

        createController = () => $controller('TabItemCtrl', { $scope: scope });
    }));

    describe('shouldRenderAction', () => {
        it('should render all transformations from non "Suggestion" category', () => {
            //given
            stateMock.playground.filter.applyTransformationOnFilters = false;
            stateMock.playground.grid.selectedColumns = [{}];
            const transformation = { category: 'strings', actionScope: [] };
            const category = { category: 'strings' };
            const ctrl = createController();

            //when
            const result = ctrl.shouldRenderAction(category, transformation);

            //then
            expect(result).toBe(true);
        });

        it('should render "filtered" transformations on filter data application', () => {
            //given
            stateMock.playground.filter.applyTransformationOnFilters = true;
            stateMock.playground.grid.selectedColumns = [{}];
            const transformation = { category: 'filtered', actionScope: ['column_filtered'] };
            const category = { category: 'suggestions' };
            const ctrl = createController();

            //when
            const result = ctrl.shouldRenderAction(category, transformation);

            //then
            expect(result).toBe(true);
        });

        it('should NOT render "filtered" transformations without filter data application', () => {
            //given
            stateMock.playground.filter.applyTransformationOnFilters = false;
            stateMock.playground.grid.selectedColumns = [{}];
            const transformation = { category: 'filtered', actionScope: ['column_filtered'] };
            const category = { category: 'suggestions' };
            const ctrl = createController();

            //when
            const result = ctrl.shouldRenderAction(category, transformation);

            //then
            expect(result).toBe(false);
        });

        it('should render suggestion transformations on single column selection', () => {
            //given
            stateMock.playground.filter.applyTransformationOnFilters = false;
            stateMock.playground.grid.selectedColumns = [{}];
            const transformation = { category: 'strings', actionScope: [] };
            const category = { category: 'suggestions' };
            const ctrl = createController();

            //when
            const result = ctrl.shouldRenderAction(category, transformation);

            //then
            expect(result).toBe(true);
        });

        it('should NOT render suggestion transformations on multi column selection', () => {
            //given
            stateMock.playground.filter.applyTransformationOnFilters = false;
            stateMock.playground.grid.selectedColumns = [{}, {}];
            const transformation = { category: 'strings', actionScope: [] };
            const category = { category: 'suggestions'};
            const ctrl = createController();

            //when
            const result = ctrl.shouldRenderAction(category, transformation);

            //then
            expect(result).toBe(false);
        });
    });

    describe('shouldRenderCategory', () => {
        it('should render category when category is not "suggestion"', () => {
            //given
            stateMock.playground.filter.applyTransformationOnFilters = false;
            const categoryTransformations = {
                category: 'quickfix',
                transformations: [{ category: 'filtered', actionScope: ['column_filtered'] }],
            };
            const ctrl = createController();

            //when
            const result = ctrl.shouldRenderCategory(categoryTransformations);

            //then
            expect(result).toBeTruthy();
        });

        it('should render "suggestion" category on filter application', () => {
            //given
            stateMock.playground.filter.applyTransformationOnFilters = true;
            const categoryTransformations = {
                category: 'suggestions',
                transformations: [{ category: 'filtered', actionScope: ['column_filtered'] }],
            };
            const ctrl = createController();

            //when
            const result = ctrl.shouldRenderCategory(categoryTransformations);

            //then
            expect(result).toBeTruthy();
        });

        it('should render "suggestion" category when only 1 column is selected', () => {
            //given
            stateMock.playground.filter.applyTransformationOnFilters = false;
            stateMock.playground.grid.selectedColumns = [{}];
            const categoryTransformations = {
                category: 'suggestions',
                transformations: [{ category: 'suggestions', actionScope: [] }],
            };
            const ctrl = createController();

            //when
            const result = ctrl.shouldRenderCategory(categoryTransformations);

            //then
            expect(result).toBeTruthy();
        });

        it('should NOT render "suggestion" category on multi column selection without filter', () => {
            //given
            stateMock.playground.filter.applyTransformationOnFilters = false;
            stateMock.playground.grid.selectedColumns = [{}, {}];
            const categoryTransformations = {
                category: 'suggestions',
                transformations: [
                    { category: 'filtered', actionScope: ['column_filtered'] },
                    { category: 'suggestions', actionScope: [] }
                ],
            };
            const ctrl = createController();

            //when
            const result = ctrl.shouldRenderCategory(categoryTransformations);

            //then
            expect(result).toBeFalsy();
        });

        it('should returns the appropriate invalid selection key', () => {
            const ctrl = createController();

            ctrl.scope = 'column';
            expect(ctrl.getInvalidSelectionKey()).toBe('SELECT_COLUMN_TO_DISPLAY_ACTIONS');

            ctrl.scope = 'line';
            expect(ctrl.getInvalidSelectionKey()).toBe('SELECT_LINE_TO_DISPLAY_ACTIONS');
        });

        it('should returns the appropriate state', () => {
            const columnState = { test: 42 };
            const lineState = { test: 43 };
            const datasetState = { test: 44 };
            stateMock.playground.suggestions = {
                column: columnState,
                line: lineState,
                dataset: datasetState,
            };
            const ctrl = createController();

            ctrl.scope = 'column';
            expect(ctrl.getSuggestionsState()).toBe(columnState);

            ctrl.scope = 'line';
            expect(ctrl.getSuggestionsState()).toBe(lineState);

            ctrl.scope = 'dataset';
            expect(ctrl.getSuggestionsState()).toBe(datasetState);
        });
    });
});
