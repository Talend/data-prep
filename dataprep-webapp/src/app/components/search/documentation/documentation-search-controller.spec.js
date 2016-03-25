/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Documentation Search controller', () => {
    'use strict';

    var component, scope;

    beforeEach(angular.mock.module('data-prep.documentation-search'));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();
        component = $componentController('documentationSearch', {$scope: scope});
    }));

    describe('search ', () => {
        it('should call documentation search service', inject(($q, DocumentationService) => {
            const results = [{url: 'url', name: 'name', description: 'description'}];
            spyOn(DocumentationService, 'search').and.returnValue($q.when(results));

            //when
            component.search('barcelona');
            scope.$digest();

            //then
            expect(DocumentationService.search).toHaveBeenCalledWith('barcelona');
        }));

        it('should set results', inject(($q, DocumentationService) => {
            const results = [{url: 'url', name: 'name', description: 'description'}];
            spyOn(DocumentationService, 'search').and.returnValue($q.when(results));

            //when
            component.search('barcelona');
            scope.$digest();

            //then
            expect(component.results).toBe(results);
        }));

        it('should NOT set results when the search is out of date', inject(($q, DocumentationService) => {
            const results = [{url: 'url', name: 'name', description: 'description'}];
            spyOn(DocumentationService, 'search').and.returnValue($q.when(results));

            //when
            component.search('barcelona');
            component.currentInput = 'other';
            scope.$digest();

            //then
            expect(component.results).not.toBe(results);
        }));
    });
});
