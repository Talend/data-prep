describe('Transformation menu directive', function () {
    'use strict';
    var scope, createElement, element;

    beforeEach(module('data-prep.transformation-menu'));
    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function() {
            element = angular.element('<transform-menu column="column" menu-items="menu"></transform-menu>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should render a simple action', function() {
        //given
        scope.menu = [{label: 'uppercase'}];

        //when
        var element = createElement();

        //then
        expect(element.find('li[ng-click="menuCtrl.select(menu)"]').text().trim()).toBe('uppercase');
    });

    it('should render an action with parameters', function() {
        //given
        scope.menu = [{
            name: 'menuWithParam',
            label: 'menu with param',
            parameters: [
                {
                    'name': 'param1',
                    'type': 'string',
                    'inputType': 'text',
                    'default': '.'
                },
                {
                    'name': 'param2',
                    'type': 'integer',
                    'inputType': 'number',
                    'default': '5'
                }
            ]
        }];

        //when
        var element = createElement();

        //then
        var menuItem = element.find('li[ng-click="menuCtrl.select(menu)"]');
        expect(menuItem.text().trim()).toBe('menu with param');
        expect(angular.element('body').find('.transformation-params').length).toBe(0);

        //when
        menuItem.click();

        //then
        var paramsElements = angular.element('body').find('.transformation-params');
        expect(paramsElements.length).toBe(1);
        expect(paramsElements.is(':visible')).toBe(true);
    });

    it('should render an action with simple choice', function() {
        //given
        scope.menu = [{
            name: 'menuWithParam',
            label: 'menu with param',
            items: [{
                name: 'my choice',
                values: [
                    {
                        name: 'noParamChoice1'
                    },
                    {
                        name: 'noParamChoice2'
                    }
                ]
            }]
        }];

        //when
        var element = createElement();

        //then
        var menuItem = element.find('li[ng-click="menuCtrl.select(menu)"]');
        expect(menuItem.text().trim()).toBe('menu with param');
        expect(angular.element('body').find('.transformation-params').length).toBe(0);

        //when
        menuItem.click();

        //then
        var paramsElements = angular.element('body').find('.transformation-params');
        expect(paramsElements.length).toBe(1);
        expect(paramsElements.is(':visible')).toBe(true);
    });

    it('should render multiple menu items', function() {
        //given
        scope.menu = [
            {label: 'uppercase'},
            {
                name: 'menuWithChoice',
                label: 'menu with choice',
                items: [{
                    name: 'my choice',
                    values: [
                        {
                            name: 'noParamChoice1'
                        },
                        {
                            name: 'noParamChoice2'
                        }
                    ]
                }]
            },
            {
                name: 'menuWithParam',
                label: 'menu with param',
                parameters: [
                    {
                        'name': 'param1',
                        'type': 'string',
                        'inputType': 'text',
                        'default': '.'
                    },
                    {
                        'name': 'param2',
                        'type': 'integer',
                        'inputType': 'number',
                        'default': '5'
                    }
                ]
            }
        ];

        //when
        var element = createElement();

        //then
        var menuItems = element.find('li[ng-click="menuCtrl.select(menu)"]');
        expect(menuItems.length).toBe(3);
        expect(menuItems.eq(0).text().trim()).toBe('uppercase');
        expect(menuItems.eq(1).text().trim()).toBe('menu with choice');
        expect(menuItems.eq(2).text().trim()).toBe('menu with param');
    });

    it('should display selected item parameters', function() {
        //given
        scope.menu = [
            {label: 'uppercase'},
            {
                name: 'menuWithChoice',
                label: 'menu with choice',
                items: [{
                    name: 'my choice',
                    values: [
                        {
                            name: 'noParamChoice1'
                        },
                        {
                            name: 'noParamChoice2'
                        }
                    ]
                }]
            },
            {
                name: 'menuWithParam',
                label: 'menu with param',
                parameters: [
                    {
                        'name': 'param1',
                        'type': 'string',
                        'inputType': 'text',
                        'default': '.'
                    },
                    {
                        'name': 'param2',
                        'type': 'integer',
                        'inputType': 'number',
                        'default': '5'
                    }
                ]
            }
        ];

        var element = createElement();
        var menuItems = element.find('li[ng-click="menuCtrl.select(menu)"]');
        var menuWithChoice = menuItems.eq(1);
        var menuWithParams = menuItems.eq(2);
        expect(angular.element('body').find('.transformation-params').length).toBe(0);

        //when
        menuWithChoice.click();

        //then : expect params to render choice
        var paramsElements = angular.element('body').find('.transformation-params');
        expect(paramsElements.length).toBe(1);
        expect(paramsElements.is(':visible')).toBe(true);
        expect(paramsElements.find('input').length).toBe(0);
        expect(paramsElements.find('select').length).toBe(1);

        //when
        menuWithParams.click();

        //then : expect params to render simple params
        paramsElements = angular.element('body').find('.transformation-params');
        expect(paramsElements.length).toBe(1);
        expect(paramsElements.is(':visible')).toBe(true);
        expect(paramsElements.find('input').length).toBe(2);
        expect(paramsElements.find('select').length).toBe(0);
    });
});
