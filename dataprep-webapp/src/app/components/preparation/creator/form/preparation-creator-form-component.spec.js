/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Preparation creator form component', () => {
    let scope;
    let createElement;
    let element;
    let controller;

    beforeEach(angular.mock.module('data-prep.preparation-creator'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            EXISTING_DATASETS: 'Existing Datasets',
            NAME_ALREADY_EXISTS: 'This Name already exists in the current folder',
            NEW_PREPARATION_NAME: 'Preparation Name',
            NO_DATASET: 'No dataset available',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile, $q, DatasetService) => {
        scope = $rootScope.$new(true);

        createElement = () => {
            element = angular.element(`<preparation-creator-form></preparation-creator-form>`);

            $compile(element)(scope);
            scope.$digest();

            controller = element.controller('preparationCreatorForm');
        };

        spyOn(DatasetService, 'getFilteredDatasets').and.returnValue($q.when([
            {
                id: '12c32-bf80-41c8-92e5-66d70f22ec1f',
                name: 'US States',
                type: 'text/csv',
            },
            {
                id: '02ce6c32-bf80-41c8-92e5-66d70f22ec1d',
                name: 'US States',
                type: 'application/vnd.ms-excel',
            },
        ]));
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('render', () => {
        it('should render preparation creator content', () => {
            //when
            createElement();

            //then
            expect(element.find('.base-dataset-upload').length).toBe(1);
            expect(element.find('.preparation-creator-header').length).toBe(1);
            expect(element.find('.filters-left-panel').length).toBe(1);
            expect(element.find('.import-button-panel').length).toBe(1);
            expect(element.find('.theme-filter-title').length).toBe(3);
            expect(element.find('.inventory-list').length).toBe(2);
            expect(element.find('form').length).toBe(1);
            expect(element.find('.modal-buttons').length).toBe(1);
        });

        describe('header', () => {
            it('should render header content', () => {
                //when
                createElement();

                //then
                expect(element.find('.filters-list-title').text()).toBe('Existing Datasets');
                expect(element.find('#filtered-datasets-search').length).toBe(1);
            });

            it('should disable header input while import', () => {
                //when
                createElement();
                controller.importDisabled = true;
                scope.$digest();

                //then
                expect(element.find('#filtered-datasets-search').attr('disabled')).toBe('disabled');
            });
        });

        describe('left Panel', () => {
            it('should render left panel', () => {
                //when
                createElement();

                //then
                expect(element.find('theme-filters').length).toBe(1);
                expect(element.find('#localFileImport').length).toBe(1);
                expect(element.find('.import-button-panel').length).toBe(1);
            });

            it('should disable import button when entered preparation name already exists', () => {
                //when
                createElement();
                controller.alreadyExistingName = true;
                expect(element.find('.disabled-import').length).toBe(0);
                scope.$digest();

                //then
                expect(element.find('.disabled-import').length).toBe(1);
            });

            it('should disable all left panel while import', () => {
                //when
                createElement();
                controller.importDisabled = true;
                expect(element.find('.filters-left-panel').hasClass('disabled-import')).toBe(false);
                scope.$digest();

                //then
                expect(element.find('.filters-left-panel').hasClass('disabled-import')).toBe(true);
            });
        });

        describe('right Panel', () => {
            it('should render right panel spinner', () => {
                //when
                createElement();
                expect(element.find('loader').length).toBe(0);
                controller.isFetchingDatasets = true;
                scope.$digest();

                //then
                expect(element.find('loader').length).toBe(1);
                expect(element.find('.inventory-item-row').length).toBe(0);
            });

            it('should hide right panel loader', () => {
                //given
                createElement();
                controller.isFetchingDatasets = true;
                scope.$digest();
                expect(element.find('loader').length).toBe(1);

                controller.isFetchingDatasets = false;
                scope.$digest();

                //then
                expect(element.find('loader').length).toBe(0);
            });

            it('should hide right panel loader and show datasets', () => {
                //given
                createElement();
                controller.isFetchingDatasets = true;
                scope.$digest();
                expect(element.find('loader').length).toBe(1);

                controller.isFetchingDatasets = false;
                scope.$digest();

                //then
                expect(element.find('loader').length).toBe(0);
                expect(element.find('.inventory-item-row').length).toBe(2);
            });

            it('should show a message if there is no dataset to display', () => {
                //given
                createElement();
                controller.filteredDatasets = [];
                scope.$digest();

                //then
                expect(element.find('.inventory-item-row').length).toBe(0);
                expect(element.find('.empty-message').length).toBe(1);
                expect(element.find('.empty-message').text()).toBe('No dataset available');
            });

            it('should disable right panel while import', () => {
                //when
                createElement();
                controller.importDisabled = true;
                expect(element.find('.inventory-list').hasClass('disabled-import')).toBe(false);
                scope.$digest();

                //then
                expect(element.find('.inventory-list').hasClass('disabled-import')).toBe(true);
            });

            it('should update selected dataset background color on first selection', () => {
                //given
                createElement();
                element.find('.inventory-item-row').eq(1).click();
                scope.$digest();

                //then
                expect(element.find('.inventory-item-row').eq(0).hasClass('selected-dataset')).toBe(false);
                expect(element.find('.inventory-item-row').eq(1).hasClass('selected-dataset')).toBe(true);
            });

            it('should update selected dataset background color after 1st selection', () => {
                //given
                createElement();
                element.find('.inventory-item-row').eq(0).click();
                scope.$digest();
                expect(element.find('.inventory-item-row').eq(0).hasClass('selected-dataset')).toBe(true);
                expect(element.find('.inventory-item-row').eq(1).hasClass('selected-dataset')).toBe(false);

                //when
                element.find('.inventory-item-row').eq(1).click();

                //then
                expect(element.find('.inventory-item-row').eq(0).hasClass('selected-dataset')).toBe(false);
                expect(element.find('.inventory-item-row').eq(1).hasClass('selected-dataset')).toBe(true);
            });
        });

        describe('form', () => {
            it('should render form input Label', () => {
                //when
                createElement();

                //then
                expect(element.find('.preparation-name-input').text()).toBe('Preparation Name');
            });

            it('should render form input', () => {
                //when
                createElement();

                //then
                expect(element.find('form > input').length).toBe(1);
            });

            it('should disable form input while import', () => {
                //when
                createElement();
                controller.importDisabled = true;
                scope.$digest();

                //then
                expect(element.find('form > input').eq(0).attr('disabled')).toBe('disabled');
            });

            it('should show error message when entered name already exists', () => {
                //when
                createElement();
                controller.alreadyExistingName = true;
                scope.$digest();

                //then
                expect(element.find('.name-error').text()).toBe('This Name already exists in the current folder');
            });
        });

        describe('buttons', () => {
            it('should render cancel button', () => {
                //when
                createElement();

                //then
                expect(element.find('.modal-secondary-button').length).toBe(1);
            });

            it('should disable cancel while import', () => {
                //when
                createElement();
                controller.importDisabled = true;
                scope.$digest();

                //then
                expect(element.find('.modal-secondary-button').eq(0).attr('disabled')).toBe('disabled');
            });

            it('should render confirm button', () => {
                //when
                createElement();

                //then
                expect(element.find('.modal-primary-button').length).toBe(1);
            });

            it('should disable confirm while import or entered name is already used', () => {
                //when
                createElement();
                controller.enteredName = '';
                scope.$digest();

                //then
                expect(element.find('.modal-primary-button').eq(0).attr('disabled')).toBe('disabled');
            });
        });
    });
});
