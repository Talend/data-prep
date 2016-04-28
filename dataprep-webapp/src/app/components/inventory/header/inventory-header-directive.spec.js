/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Inventory header directive', () => {
    'use strict';

    let createElement, scope, element;

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            'SORTED_BY': 'Sorted by',
            'SORT_IN': 'in',
            'SORT_ORDER': 'order',
            'NAME_SORT': 'name',
            'ASC_ORDER': 'asc'
        });
        $translateProvider.preferredLanguage('en');
    }));
    beforeEach(angular.mock.module('data-prep.inventory-header'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new(true);
        scope.sortList = [
            {id: 'name', name: 'NAME_SORT', property: 'name'},
            {id: 'date', name: 'DATE_SORT', property: 'created'},
        ];
        scope.orderList = [
            {id: 'asc', name: 'ASC_ORDER'},
            {id: 'desc', name: 'DESC_ORDER'},
        ];
        scope.sort = scope.sortList[0];
        scope.order = scope.orderList[0];
        scope.onSortChange = jasmine.createSpy('onSortChange');
        scope.onOrderChange = jasmine.createSpy('onOrderChange');
        scope.createFolder = jasmine.createSpy('createFolder');

        createElement = (options) => {
            const html = `
                <inventory-header
                    sort="sort"
                    order="order"
                    sort-list="sortList"
                    order-list="orderList"
                    folder-list="folderList"
                    on-sort-change="onSortChange(sort)"
                    on-order-change="onOrderChange(order)"
                    ${options && options.createFolder ? 'on-folder-creation="createFolder(name)"' : ''}>
                </inventory-header>
            `;
            element = angular.element(html);
            angular.element('body').append(element);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('create folder', () => {
        it('should render "add folder" button', () => {
            // given
            const options = { createFolder: true };

            // when
            createElement(options);

            // then
            expect(element.find('#add-folder-button').length).toBe(1);
        });

        it('should NOT render "add folder" button', () => {
            // when
            createElement();

            // then
            expect(element.find('#add-folder-button').length).toBe(0);
        });

        it('should open create folder modal on button click', () => {
            //given
            const options = { createFolder: true };
            createElement(options);

            expect(angular.element('body #create-folder-modal').length).toBe(0);

            // when
            element.find('#add-folder-button').eq(0).click();

            // then
            expect(angular.element('body #create-folder-modal').length).toBe(1);
        });
    });

    describe('sort control', () => {
        it('should render sort switch', () => {
            //given
            scope.sort = {id: 'name', name: 'NAME_SORT', property: 'name'};
            scope.order = {id: 'asc', name: 'ASC_ORDER'};

            // when
            createElement();

            // then
            expect(element.find('.inventory-sort').text().replace(/[\s]+/g, ' ').trim()).toBe('Sorted by name in asc order');
        });

        it('should call sort change callback on sort switch click', () => {
            //given
            createElement();
            expect(scope.onSortChange).not.toHaveBeenCalled();

            // when
            element.find('talend-button-switch').eq(0).click();

            // then
            expect(scope.onSortChange).toHaveBeenCalledWith(scope.sortList[1]);
        });

        it('should call order change callback on order switch click', () => {
            //given
            createElement();
            expect(scope.onOrderChange).not.toHaveBeenCalled();

            // when
            element.find('talend-button-switch').eq(1).click();

            // then
            expect(scope.onOrderChange).toHaveBeenCalledWith(scope.orderList[1]);
        });
    });

});