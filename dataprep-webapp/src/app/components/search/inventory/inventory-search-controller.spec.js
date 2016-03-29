/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Inventory Search controller', () => {
    'use strict';

    let component, scope;

    beforeEach(angular.mock.module('data-prep.inventory-search'));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();
        component = $componentController('inventorySearch', {$scope: scope});
    }));

    describe('search ', () => {
        it('should call inventory search service', inject(($q,InventoryService, DocumentationService) => {
            const results = [{}, {}];
            const docResults = [{url: 'url', name: 'name', description: 'description'}];
            spyOn(InventoryService, 'search').and.returnValue($q.when(results));
            spyOn(DocumentationService, 'search').and.returnValue($q.when(docResults));

            //when
            component.search('barcelona');
            scope.$digest();

            //then
            expect(InventoryService.search).toHaveBeenCalledWith('barcelona');
        }));

        it('should call documentation search service', inject(($q, InventoryService, DocumentationService) => {
            const results = [{}, {}];
            const docResults = [{url: 'url', name: 'name', description: 'description'}];
            spyOn(InventoryService, 'search').and.returnValue($q.when(results));
            spyOn(DocumentationService, 'search').and.returnValue($q.when(docResults));

            //when
            component.search('barcelona');
            scope.$digest();

            //then
            expect(DocumentationService.search).toHaveBeenCalledWith('barcelona');
        }));

        it('should set results', inject(($q, InventoryService, DocumentationService) => {
            const results = [{}, {}];
            const docResults = [{url: 'url', name: 'name', description: 'description'}];
            spyOn(InventoryService, 'search').and.returnValue($q.when(results));
            spyOn(DocumentationService, 'search').and.returnValue($q.when(docResults));

            //when
            component.search('barcelona');
            scope.$digest();

            //then
            expect(component.results).toEqual(docResults.concat(results));
        }));

        it('should NOT set results when they are out of date', inject(($q, InventoryService, DocumentationService) => {
            const results = [{}, {}];
            const docResults = [{url: 'url', name: 'name', description: 'description'}];
            spyOn(InventoryService, 'search').and.returnValue($q.when(results));
            spyOn(DocumentationService, 'search').and.returnValue($q.when(docResults));

            //when
            component.search('barcelona');
            component.currentInput = 'other';
            scope.$digest();

            //then
            expect(component.results).not.toEqual(docResults.concat(results));
        }));

        it('should set empty array as results when there are no result', inject(($q, InventoryService, DocumentationService) => {
            const results = [];
            const docResults = [];
            spyOn(InventoryService, 'search').and.returnValue($q.when(results));
            spyOn(DocumentationService, 'search').and.returnValue($q.when(docResults));
            component.results = null;

            //when
            component.search('barcelona');
            scope.$digest();

            //then
            expect(component.results).toEqual([]);
        }));
    });

    describe('element selection', () => {
        it('should go to folder', inject(($state) => {
            //given
            spyOn($state, 'go').and.returnValue();

            //when
            component.goToFolder('nav.index.datasets', {folderPath : '1/2'});

            //then
            expect($state.go).toHaveBeenCalledWith('nav.index.datasets', {folderPath : '1/2'});
        }));
    })
});
