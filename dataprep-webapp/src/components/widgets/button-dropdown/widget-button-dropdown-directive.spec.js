'use strict';

describe('Button Dropdown directive', function () {
    var scope, element, html;

    beforeEach(module('talend.widget'));
    beforeEach(module('htmlTemplates'));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();
        scope.buttonAction = function() {};

        html = '<talend-button-dropdown button-icon="m" button-text="Click Me" button-action="buttonAction()">' +
            '   <ul>' +
            '       <li>Menu 1</li>' +
            '       <li>Menu 2</li>' +
            '   </ul>' +
            '</talend-button-dropdown>';
        element = $compile(html)(scope);
        scope.$digest();

        spyOn(scope, 'buttonAction').and.returnValue();
    }));

    it('should call action on main button click', function() {
        //when
        element.find('.button-dropdown-main').eq(0).click();

        //then
        expect(scope.buttonAction).toHaveBeenCalled();
    });

    it('should show dropdown menu on side button click', function() {
        //given
        var menu = element.find('.dropdown-menu').eq(0);
        expect(menu.hasClass('show-menu')).toBe(false);

        //when
        element.find('.button-dropdown-side').eq(0).click();

        //then
        expect(menu.hasClass('show-menu')).toBe(true);
    });
});