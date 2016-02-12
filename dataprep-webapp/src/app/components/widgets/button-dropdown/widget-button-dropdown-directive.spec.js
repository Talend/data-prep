/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

'use strict';

describe('Button Dropdown directive', function () {
    var scope, element, createElementWithAction, createElementWithoutAction;

    beforeEach(angular.mock.module('talend.widget'));
    beforeEach(angular.mock.module('htmlTemplates'));

    afterEach(function () {
        scope.$destroy();
        element.remove();
        jasmine.clock().uninstall();
    });

    beforeEach(inject(function ($rootScope, $compile, $timeout) {
        jasmine.clock().install();
        scope = $rootScope.$new();

        createElementWithAction = function() {
            scope.buttonAction = jasmine.createSpy('buttonAction');

            var html = '<talend-button-dropdown button-icon="m" button-text="Click Me" button-action="buttonAction()">' +
                '   <ul>' +
                '       <li>Menu 1</li>' +
                '       <li>Menu 2</li>' +
                '   </ul>' +
                '</talend-button-dropdown>';
            element = $compile(html)(scope);
            $timeout.flush();
            scope.$digest();
        };

        createElementWithoutAction = function() {
             var html = '<talend-button-dropdown button-icon="m" button-text="Click Me" button-action="">' +
                 '   <ul>' +
                 '       <li>Menu 1</li>' +
                 '       <li>Menu 2</li>' +
                 '   </ul>' +
                 '</talend-button-dropdown>';
             element = $compile(html)(scope);
            $timeout.flush();
             scope.$digest();
         };
    }));

    it('should call action on main button click', function() {
        //given
        createElementWithAction();

        //when
        element.find('.button-dropdown-main').eq(0).click();

        //then
        expect(scope.buttonAction).toHaveBeenCalled();
    });

    it('should show dropdown menu on side button click', function() {
        //given
        createElementWithAction();
        var menu = element.find('.dropdown-menu').eq(0);
        expect(menu.hasClass('show-menu')).toBe(false);

        //when
        element.find('.button-dropdown-side').eq(0).click();
        jasmine.clock().tick(200);

        //then
        expect(menu.hasClass('show-menu')).toBe(true);
    });

    it('should call action on main button click', function() {
        //given
        createElementWithoutAction();
        var menu = element.find('.dropdown-menu').eq(0);
        expect(menu.hasClass('show-menu')).toBe(false);

        //when
        element.find('.button-dropdown-main').eq(0).click();
        jasmine.clock().tick(200);

        //then
        expect(menu.hasClass('show-menu')).toBe(true);

    });

    it('should show dropdown menu on side button click', function() {
        //given
        createElementWithoutAction();
        var menu = element.find('.dropdown-menu').eq(0);
        expect(menu.hasClass('show-menu')).toBe(false);

        //when
        element.find('.button-dropdown-side').eq(0).click();
        jasmine.clock().tick(200);

        //then
        expect(menu.hasClass('show-menu')).toBe(true);
    });
});
