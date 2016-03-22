/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Documentation Search controller', function () {
    'use strict';

    var component, scope;

    beforeEach(angular.mock.module('data-prep.documentation-search'));

    beforeEach(inject(function($rootScope, $componentController) {
        scope = $rootScope.$new();
        component = $componentController('documentationSearch', {$scope: scope});
    }));

    describe('search ', function() {
        it('should call documentation search service', inject(function ($q,DocumentationService) {
            spyOn(DocumentationService, 'search').and.returnValue($q.when([{url: 'url', name: 'name', description: 'description'}]));

            //when
            component.search('barcelona');
            scope.$digest();

            //then
            expect(DocumentationService.search).toHaveBeenCalledWith('barcelona');
            expect(component.results).toEqual([{url: 'url', name: 'name', description: 'description'}]);
        }));
    });
});
