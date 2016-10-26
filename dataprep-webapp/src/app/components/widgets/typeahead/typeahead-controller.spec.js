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

    let createController;
    let scope;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        createController = () => $componentController('typeahead', { $scope: scope });
    }));

    describe('on change', () => {
        it('should trigger search callback', () => {
            //given
            var ctrl = createController();
            ctrl.search = jasmine.createSpy('search');
            ctrl.searchString = 'toto';

            //when
            ctrl.onChange();

            //then
            expect(ctrl.search).toHaveBeenCalledWith({ value: 'toto' });
        });

        it('should show result block', () => {
            //given
            var ctrl = createController();
            ctrl.search = jasmine.createSpy('search');
            ctrl.searchString = 'toto';
            ctrl.visible = false;

            //when
            ctrl.onChange();

            //then
            expect(ctrl.visible).toBe(true);
        });

        it('should manage searching flag', inject(($q) => {
            //given
            var ctrl = createController();
            ctrl.search = jasmine.createSpy('search').and.returnValue($q.when());
            ctrl.searchString = 'toto';
            ctrl.searching = false;

            //when
            ctrl.onChange();
            expect(ctrl.searching).toBe(true);
            scope.$digest();

            //then
            expect(ctrl.searching).toBe(false);
        }));

        it('should not reset searching flag when result is out of date', inject(($q) => {
            //given
            var ctrl = createController();
            ctrl.search = jasmine.createSpy('search').and.returnValue($q.when());
            ctrl.searchString = 'toto';
            ctrl.searching = false;

            //when
            ctrl.onChange();
            expect(ctrl.searching).toBe(true);
            ctrl.searchString = 'tata';
            scope.$digest();

            //then
            expect(ctrl.searching).toBe(true);
        }));
    });

    describe('results visibility', () => {
        it('should hide result block', () => {
            //given
            var ctrl = createController();
            ctrl.visible = true;

            //when
            ctrl.hideResults();

            //then
            expect(ctrl.visible).toBe(false);
        });

        it('should show result block', () => {
            //given
            var ctrl = createController();
            ctrl.visible = false;

            //when
            ctrl.showResults();

            //then
            expect(ctrl.visible).toBe(true);
        });
    });

    describe('search input', () => {
        it('should toogle search input visibility', () => {
            //given
            var ctrl = createController();
            ctrl.inputVisible = true;

            //when
            ctrl.toogleSearchInput();

            //then
            expect(ctrl.inputVisible).toBe(false);
        });
    });
});
