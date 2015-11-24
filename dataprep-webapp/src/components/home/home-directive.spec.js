describe('Home directive', function() {
    'use strict';

    var scope, createElement, element;

    beforeEach(module('data-prep.home'));
    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile, $q, FolderService) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<home></home>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
        spyOn(FolderService, 'getFolderContent').and.returnValue($q.when(true));
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should render subheader bar', inject(function (FolderService) {
        //when
        createElement();

        //then
        expect(element.find('header.subheader').length).toBe(1);
        // TODO called only if homeCtrl.$state.includes('nav.home.datasets')
        // maybe worth to test that
        //expect(FolderService.getFolderContent).toHaveBeenCalled();
    }));

    it('should render home main panel', inject(function (FolderService) {
        //when
        createElement();

        //then
        var home = element.find('.home');
        expect(home.length).toBe(1);
        expect(home.find('.side-menu').length).toBe(1);
        expect(home.find('ui-view[name="home-content"]').length).toBe(1);
        expect(home.find('.inventory-data').length).toBe(1);
        // TODO called only if homeCtrl.$state.includes('nav.home.datasets')
        // maybe worth to test that
        //expect(FolderService.getFolderContent).toHaveBeenCalled();
    }));
});