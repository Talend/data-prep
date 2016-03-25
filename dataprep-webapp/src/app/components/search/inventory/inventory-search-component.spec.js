/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Inventory Search component', () => {
    let scope, createElement, element;

    beforeEach(angular.mock.module('data-prep.inventory-search'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        createElement = () => {
            const template =`<inventory-search></inventory-search>`;
            element = $compile(template)(scope);
            scope.$digest();

            const typeahead = element.find('input').controller('typeahead');
            typeahead.searchString = 'aze';
            typeahead.visible = true;
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render typeahead', () => {
        //when
        createElement();

        //then
        expect(element.find('typeahead').length).toBe(1);
    });

    it('should render result', () => {
        //given
        createElement();
        const ctrl = element.controller('inventorySearch');

        //when
        ctrl.results = [{inventoryType: 'dataset'}];
        ctrl.docResults = [{}, {}];
        scope.$apply();

        //then
        expect(element.find('inventory-item').length).toBe(3);
    });

    describe('no-result', () => {
        it('should render no-result', () => {
            //given
            createElement();
            const ctrl = element.controller('inventorySearch');

            //when
            ctrl.searching = false;
            ctrl.results = [];
            ctrl.docResults = [];
            scope.$apply();

            //then
            expect(element.find('.no-results').length).toBe(1);
        });

        it('should NOT render no-result when search is in progress', () => {
            //given
            createElement();
            const ctrl = element.controller('inventorySearch');

            //when
            ctrl.searching = true;
            ctrl.results = [];
            ctrl.docResults = [];
            scope.$digest();

            //then
            expect(element.find('.no-results').length).toBe(0);
        });

        it('should NOT render no-result when there is a result', () => {
            //given
            createElement();
            const ctrl = element.controller('inventorySearch');

            //when
            ctrl.searching = false;
            ctrl.results = [{}];
            ctrl.docResults = [];
            scope.$digest();

            //then
            expect(element.find('.no-results').length).toBe(0);
        });
    });

});