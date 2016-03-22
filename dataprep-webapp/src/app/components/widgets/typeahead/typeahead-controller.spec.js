/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Typeahead widget controller', () => {
    'use strict';

    let createController, scope;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        createController = () => $componentController('typeahead', {$scope: scope});
    }));

    describe('on change', () => {
        it('should update position', () => {
            //given
            var ctrl = createController();
            ctrl.search = jasmine.createSpy('search');
            ctrl.searchString = 'toto';

            //when
            ctrl.onChange();

            //then
            expect(ctrl.search).toHaveBeenCalledWith({value: 'toto'});
        });

        it('should show result block', () => {
            //given
            var ctrl = createController();
            ctrl.search = jasmine.createSpy('search');
            ctrl.searchString = 'toto';
            ctrl.results = false;

            //when
            ctrl.onChange();

            //then
            expect(ctrl.results).toBe(true);
        });
    });

    describe('results visibility', () => {
        it('should hide result block', () => {
            //given
            var ctrl = createController();
            ctrl.results = true;

            //when
            ctrl.hideResults();

            //then
            expect(ctrl.results).toBe(false);
        });

        it('should show result block', () => {
            //given
            var ctrl = createController();
            ctrl.results = false;

            //when
            ctrl.showResults();

            //then
            expect(ctrl.results).toBe(true);
        });
    });

});
