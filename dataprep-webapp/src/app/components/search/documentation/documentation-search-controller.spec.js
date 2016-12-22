/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const searchInput = 'barcelona';

const results = [{ url: 'url', name: 'name', description: 'description' }];

describe('Documentation Search controller', () => {
	let scope;
	let ctrl;

	beforeEach(angular.mock.module('data-prep.documentation-search'));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();
		ctrl = $componentController('documentationSearch', { $scope: scope });
	}));

	describe('search ', () => {
		it('should call search service', inject(($q, SearchService) => {
			// given
			spyOn(SearchService, 'searchDocumentationAndHighlight').and.returnValue($q.when(results));

			// when
			ctrl.search(searchInput);
			scope.$digest();

			// then
			expect(SearchService.searchDocumentationAndHighlight).toHaveBeenCalledWith(searchInput);
		}));

		it('should set results', inject(($q, SearchService) => {
			// given
			spyOn(SearchService, 'searchDocumentationAndHighlight').and.returnValue($q.when(results));

			// when
			ctrl.search(searchInput);
			scope.$digest();

			// then
			expect(ctrl.results).toBe(results);
		}));

		it('should NOT set results when the search is out of date', inject(($q, SearchService) => {
			// given
			spyOn(SearchService, 'searchDocumentationAndHighlight').and.returnValue($q.when(results));

			// when
			ctrl.search(searchInput);
			ctrl.currentInput = 'other';
			scope.$digest();

			// then
			expect(ctrl.results).not.toBe(results);
		}));
	});
});
