/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Column profile options component', () => {
    'use strict';

    let createElement;
    let scope;
    let element;

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

    beforeEach(angular.mock.module('ngSanitize'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        scope.onAggregationChange = jasmine.createSpy('onAggregationChange');
        scope.numericColumns = [
            { id: '0', name: 'revenue' },
            { id: '1', name: 'year' },
            { id: '2', name: 'age' },
        ];

        createElement = () => {
            var template = `
                <column-profile-options
                    numeric-columns="numericColumns"
                    aggregation="aggregation"
                    column="column"
                    group="group"
                    on-aggregation-change="onAggregationChange(column, aggregation)"></column-profile-options>
            `;
            element = $compile(template)(scope);
            scope.$digest();
        };
    }));

    describe('render', () => {
        it('should render a control div with "insertion-charts-control" attribute', () => {
            // when
            createElement();

            // then
            var control = element.find('#chart-control');
            expect(control.length).toBe(1);
            expect(control.eq(0)[0].hasAttribute('insertion-charts-control')).toBe(true);
        });

        it('should render dropdown trigger', () => {
            // given
            scope.group = { id: '4', name: 'id' };
            scope.column = { id: '0', name: 'revenue' };
            scope.aggregation = 'MAX';

            // when
            createElement();

            // then
            const trigger = element.find('.chart-control-trigger').eq(0);
            expect(trigger.text().trim()).toBe('Max revenue');
        });

        describe('form', () => {
            it('should render form', () => {
                // given
                scope.group = { id: '4', name: 'id' };
                scope.column = { id: '0', name: 'revenue' };
                scope.aggregation = 'MAX';

                // when
                createElement();

                // then
                const form = element.find('#chart-control-form');
                expect(form.length).toBe(1);

                const selectCol = form.eq(0).find('select#chart-control-aggregation-column');
                expect(selectCol.length).toBe(1);
                expect(selectCol.find('option').length).toBe(4);
                expect(selectCol.find('option').eq(0).text().trim()).toBe('No aggregation');
                expect(selectCol.find('option').eq(1).text().trim()).toBe('revenue');
                expect(selectCol.find('option').eq(2).text().trim()).toBe('year');
                expect(selectCol.find('option').eq(3).text().trim()).toBe('age');

                const selectAggreg = form.eq(0).find('select#chart-control-aggregation-type');
                expect(selectAggreg.length).toBe(1);
                expect(selectAggreg.find('option').length).toBe(4);
                expect(selectAggreg.find('option').eq(0).text().trim()).toBe('Sum');
                expect(selectAggreg.find('option').eq(1).text().trim()).toBe('Max');
                expect(selectAggreg.find('option').eq(2).text().trim()).toBe('Min');
                expect(selectAggreg.find('option').eq(3).text().trim()).toBe('Average');

                const buttons = form.eq(0).find('.chart-control-buttons').eq(0);
                expect(buttons.find('button#chart-control-aggregation-remove').length).toBe(1);
                expect(buttons.find('button#chart-control-aggregation-remove').text().trim()).toBe('Remove');
                expect(buttons.find('button#chart-control-aggregation-cancel').length).toBe(1);
                expect(buttons.find('button#chart-control-aggregation-cancel').text().trim()).toBe('Cancel');
                expect(buttons.find('button#chart-control-aggregation-ok').length).toBe(1);
                expect(buttons.find('button#chart-control-aggregation-ok').text().trim()).toBe('Ok');
            });

            it('should render aggregation selection when there is a selected column', () => {
                // given
                createElement();
                const ctrl = element.controller('columnProfileOptions');

                // when
                ctrl.selected.column = { id: '4', name: 'id' };
                scope.$digest();

                // then
                const form = element.find('#chart-control-form');
                const selectAggreg = form.eq(0).find('select#chart-control-aggregation-type');
                expect(selectAggreg.length).toBe(1);
            });

            it('should NOT render aggregation selection when there is NO selected column', () => {
                // given
                createElement();
                const ctrl = element.controller('columnProfileOptions');

                // when
                ctrl.selected.column = undefined;
                scope.$digest();

                // then
                const form = element.find('#chart-control-form');
                const selectAggreg = form.eq(0).find('select#chart-control-aggregation-type');
                expect(selectAggreg.length).toBe(0);
            });
        });

        describe('description', () => {
            it('should render current aggregation description', () => {
                // given
                scope.group = { id: '4', name: 'id' };
                scope.column = { id: '0', name: 'revenue' };
                scope.aggregation = 'MAX';

                // when
                createElement();

                // then
                expect(element.find('#aggregation-description').eq(0).text().trim()).toBe('Max revenue group by id');
            });

            it('should render selected aggregation description', () => {
                // given
                scope.group = { id: '4', name: 'id' };
                createElement();
                const ctrl = element.controller('columnProfileOptions');

                // when
                ctrl.selected.column = { id: '0', name: 'revenue' };
                ctrl.selected.aggregation = 'MAX';
                ctrl.updateSelectedDescription();
                scope.$digest();

                // then
                const form = element.find('#chart-control-form');
                expect(form.find('.aggregation-description').eq(0).text().trim()).toBe('Max revenue group by id');
            });
        });
    });
});
