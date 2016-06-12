/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Datepattern Search controller', () => {
    'use strict';

    let scope,createController;

    beforeEach(angular.mock.module('data-prep.datepattern-search'));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        createController = () => {
            return $componentController(
                'datepatternSearch',
                {$scope: scope},
                {submit: jasmine.createSpy('submit')}
            );
        }
    }));

    describe('search ', () => {
        it('should call datepattern search service', inject(($q, DatepatternService) => {
            spyOn(DatepatternService, 'search').and.returnValue($q.when([{}]));
            const ctrl = createController();

            //when
            ctrl.search('beer');
            scope.$digest();

            //then
            expect(DatepatternService.search).toHaveBeenCalledWith('beer');
        }));


    });

    describe('onSubmit', () => {
        it('should call datepattern create service', inject(($q, DatepatternService) => {
            spyOn(DatepatternService, 'create').and.returnValue($q.when());

            const ctrl = createController();

            //when
            ctrl.onSubmit('beer');
            scope.$digest();

            //then
            expect(DatepatternService.create).toHaveBeenCalledWith('beer');
            expect(ctrl.submit).toHaveBeenCalledWith({ datepattern: 'beer' });

    }));


});
});
