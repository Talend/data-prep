 /*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Search bar controller', () => {
    let scope, createController;

    beforeEach(angular.mock.module('data-prep.search-bar'));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        createController = () => {
            return $componentController(
                'searchBar',
                {$scope: scope},
                {search: jasmine.createSpy('search')}
            );
        }
    }));

    describe('trigger search', () => {
        it('should set results to null', () => {
            //given
            const ctrl = createController();
            ctrl.results = [];

            //when
            ctrl.triggerSearch();

            //then
            expect(ctrl.results).toBeFalsy();
        });

        it('should call search implementation', () => {
            //given
            const ctrl = createController();

            //when
            ctrl.triggerSearch('toto');

            //then
            expect(ctrl.search).toHaveBeenCalledWith({value: 'toto'});
        });

        it('should NOT call search implementation when search input is falsy', () => {
            //given
            const ctrl = createController();

            //when
            ctrl.triggerSearch();

            //then
            expect(ctrl.search).not.toHaveBeenCalled();
        });
    });
});