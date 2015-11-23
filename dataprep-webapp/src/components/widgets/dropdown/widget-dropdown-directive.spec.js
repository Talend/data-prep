'use strict';

describe('Dropdown directive', function () {
    var scope, element;

    beforeEach(module('talend.widget'));
    beforeEach(module('htmlTemplates'));

    var clickDropdownToggle = function (elm) {
        elm = elm || element;
        elm.find('.dropdown-action').eq(0).click();
    };

    var clickDropdownItem = function (elm) {
        elm = elm || element;
        elm.find('a[role="menuitem"]').eq(0).click();
    };

    beforeEach(function () {
        jasmine.clock().install();
    });
    afterEach(function () {
        jasmine.clock().uninstall();

        scope.$destroy();
        element.remove();
    });

    describe('closeable dropdown', function () {

        beforeEach(inject(function ($rootScope, $compile) {

            scope = $rootScope.$new();

            var html = '<talend-dropdown id="dropdown1">' +
                '    <div class="dropdown-container grid-header">' +
                '        <div class="dropdown-action">' +
                '            <div class="grid-header-title dropdown-button">{{ column.id }}</div>' +
                '            <div class="grid-header-type">{{ column.type }}</div>' +
                '        </div>' +
                '        <ul class="dropdown-menu grid-header-menu">' +
                '            <li role="presentation"><a role="menuitem" href="#">Hide Column</a></li>' +
                '            <li class="divider"></li>' +
                '            <li role="presentation"><a role="menuitem" href="#">Split first Space</a></li>' +
                '            <li role="presentation"><a role="menuitem" href="#">Uppercase</a></li>' +
                '        </ul>' +
                '    </div>' +
                '</talend-dropdown>';
            element = $compile(html)(scope);
            scope.$digest();
        }));

        it('should show dropdown-menu on dropdown-action click', function () {
            //given
            var menu = element.find('.dropdown-menu').eq(0);
            expect(menu.hasClass('show-menu')).toBe(false);

            //when
            clickDropdownToggle();

            //then
            expect(menu.hasClass('show-menu')).toBe(true);
        });

        it('should focus on dropdown menu when it is shown', function () {
            //given
            var menu = element.find('.dropdown-menu').eq(0)[0];
            var body = angular.element('body');
            body.append(element);
            expect(document.activeElement).not.toBe(menu);

            //when
            clickDropdownToggle();

            //then
            expect(document.activeElement).not.toBe(element.find('.dropdown-menu').eq(0)[0]);
        });

        it('should hide dropdown-menu on dropdown-action click when menu is visible', function () {
            //given
            var menu = element.find('.dropdown-menu').eq(0);
            menu.addClass('show-menu');

            //when
            clickDropdownToggle();

            //then
            expect(menu.hasClass('show-menu')).toBe(false);
        });

        it('should hide dropdown-menu on item click', function () {
            //given
            var menu = element.find('.dropdown-menu').eq(0);
            menu.addClass('show-menu');

            //when
            clickDropdownItem();

            //then
            expect(menu.hasClass('show-menu')).toBe(false);
        });

        it('should register window scroll handler on open', inject(function ($window) {
            //given
            expect($._data(angular.element($window)[0], 'events')).not.toBeDefined();

            //when
            clickDropdownToggle();

            //then
            expect($._data(angular.element($window)[0], 'events')).toBeDefined();
            expect($._data(angular.element($window)[0], 'events').scroll.length).toBe(1);
        }));

        it('should unregister window scroll on close', inject(function ($window) {
            //given
            clickDropdownToggle();
            expect($._data(angular.element($window)[0], 'events').scroll.length).toBe(1);

            //when
            clickDropdownToggle();

            //then
            expect($._data(angular.element($window)[0], 'events')).not.toBeDefined();
        }));

        it('should hide dropdown-menu on body mousedown', function () {
            //given
            var menu = element.find('.dropdown-menu').eq(0);
            menu.addClass('show-menu');

            //when
            angular.element('body').mousedown();

            //then
            expect(menu.hasClass('show-menu')).toBe(false);
        });

        it('should unregister body mousedown on element remove', function () {
            //given
            expect($._data(angular.element('body')[0], 'events').mousedown.length).toBe(1);

            //when
            element.remove();

            //then
            expect($._data(angular.element('body')[0], 'events')).not.toBeDefined();
        });

        it('should stop mousedown propagation on dropdown-menu mousedown', function () {
            //given
            var bodyMouseDown = false;
            angular.element('body').mousedown(function () {
                bodyMouseDown = true;
            });

            //when
            element.find('.dropdown-menu').mousedown();

            //then
            expect(bodyMouseDown).toBe(false);
        });

        it('should hide dropdown menu on ESC', function () {
            //given
            var menu = element.find('.dropdown-menu').eq(0);
            menu.addClass('show-menu');

            var event = angular.element.Event('keydown');
            event.keyCode = 27;

            //when
            menu.trigger(event);

            //then
            expect(menu.hasClass('show-menu')).toBe(false);
        });

        it('should not hide dropdown menu on not ESC keydown', function () {
            //given
            var menu = element.find('.dropdown-menu').eq(0);
            menu.addClass('show-menu');

            var event = angular.element.Event('keydown');
            event.keyCode = 13;

            //when
            menu.trigger(event);

            //then
            expect(menu.hasClass('show-menu')).toBe(true);
        });

        it('should focus on dropdown action when menu is hidden by ESC', function () {
            //given
            var action = element.find('.dropdown-action').eq(0);
            var menu = element.find('.dropdown-menu').eq(0);
            angular.element('body').append(element);
            expect(document.activeElement).not.toBe(menu[0]);

            clickDropdownToggle();

            var event = angular.element.Event('keydown');
            event.keyCode = 27;

            //when
            menu.trigger(event);
            jasmine.clock().tick(100);

            //then
            expect(document.activeElement).toBe(action[0]);
        });

        it('should show menu on first click then close menu on the second click', function () {
            //given
            var menu = element.find('.dropdown-menu').eq(0);
            expect(menu.hasClass('show-menu')).toBe(false);

            //when
            clickDropdownToggle();
            expect(menu.hasClass('show-menu')).toBe(true);

            clickDropdownToggle();
            //then
            expect(menu.hasClass('show-menu')).toBe(false);
        });
    });

    describe('not closeable on click dropdown', function () {
        beforeEach(inject(function ($rootScope, $compile) {
            scope = $rootScope.$new();

            var html = '<talend-dropdown id="dropdown1" close-on-select="false">' +
                '    <div class="dropdown-container grid-header">' +
                '        <div class="dropdown-action">' +
                '            <div class="grid-header-title dropdown-button">{{ column.id }}</div>' +
                '            <div class="grid-header-type">{{ column.type }}</div>' +
                '        </div>' +
                '        <ul class="dropdown-menu grid-header-menu">' +
                '            <li role="presentation"><a role="menuitem" href="#">Hide Column</a></li>' +
                '            <li class="divider"></li>' +
                '            <li role="presentation"><a role="menuitem" href="#">Split first Space</a></li>' +
                '            <li role="presentation"><a role="menuitem" href="#">Uppercase</a></li>' +
                '        </ul>' +
                '    </div>' +
                '</talend-dropdown>';
            element = $compile(html)(scope);
            scope.$digest();
        }));

        it('should not hide dropdown-menu on item click if closeOnSelect is false', function () {
            //given
            var menu = element.find('.dropdown-menu').eq(0);
            menu.addClass('show-menu');

            //when
            clickDropdownItem();

            //then
            expect(menu.hasClass('show-menu')).toBe(true);
        });
    });

    describe('with onOpen action', function () {
        beforeEach(inject(function ($rootScope, $compile) {
            scope = $rootScope.$new();
            scope.onOpen = jasmine.createSpy('onOpen');

            var html = '<talend-dropdown id="dropdown1" on-open="onOpen()">' +
                '    <div class="dropdown-container grid-header">' +
                '        <div class="dropdown-action">' +
                '            <div class="grid-header-title dropdown-button">{{ column.id }}</div>' +
                '            <div class="grid-header-type">{{ column.type }}</div>' +
                '        </div>' +
                '        <ul class="dropdown-menu grid-header-menu">' +
                '            <li role="presentation"><a role="menuitem" href="#">Hide Column</a></li>' +
                '            <li class="divider"></li>' +
                '            <li role="presentation"><a role="menuitem" href="#">Split first Space</a></li>' +
                '            <li role="presentation"><a role="menuitem" href="#">Uppercase</a></li>' +
                '        </ul>' +
                '    </div>' +
                '</talend-dropdown>';
            element = $compile(html)(scope);
            scope.$digest();
        }));

        it('should call action on open click', function () {
            //given
            expect(scope.onOpen).not.toHaveBeenCalled();

            //when
            clickDropdownToggle();

            //then
            expect(scope.onOpen).toHaveBeenCalled();
        });
    });

    describe('force placement side', function () {
        var createElement;

        beforeEach(inject(function ($rootScope, $compile) {
            scope = $rootScope.$new();

            createElement = function() {
                var html = '<talend-dropdown id="dropdown1" force-side="{{side}}">' +
                    '    <div class="dropdown-container grid-header">' +
                    '        <div class="dropdown-action">Action</div>' +
                    '        <ul class="dropdown-menu grid-header-menu">' +
                    '            <li role="presentation">toto</li>' +
                    '        </ul>' +
                    '    </div>' +
                    '</talend-dropdown>';
                element = angular.element(html);
                $compile(element)(scope);
                scope.$digest();
            };
        }));

        it('should set menu placement to the right by default', function () {
            //given
            scope.side = null;
            createElement();

            var menu = element.find('.dropdown-menu').eq(0);
            expect(menu.hasClass('right')).toBe(false);

            //when
            clickDropdownToggle();

            //then
            expect(menu.hasClass('right')).toBe(true);
        });

        it('should force menu placement to the left', function () {
            //given
            scope.side = 'left';
            createElement();

            var menu = element.find('.dropdown-menu').eq(0);
            menu.addClass('right');

            //when
            clickDropdownToggle();

            //then
            expect(menu.hasClass('right')).toBe(false);
        });

        it('should force menu placement to the right', function () {
            //given
            scope.side = 'right';
            createElement();

            var menu = element.find('.dropdown-menu').eq(0);
            expect(menu.hasClass('right')).toBe(false);

            //when
            clickDropdownToggle();

            //then
            expect(menu.hasClass('right')).toBe(true);
        });
    });
});