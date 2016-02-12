/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Suggestions stats directive', function() {
    'use strict';

    var scope, createElement, element;

    beforeEach(angular.mock.module('ngSanitize'));
    beforeEach(angular.mock.module('data-prep.actions-list'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function() {
            element = angular.element('<actions-list ' +
                'actions="actions" ' +
                'should-render-action="shouldRenderAction" ' +
                'should-render-category="shouldRenderCategory" ' +
                'scope="scope"></actions-list>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    describe('render', function() {
        beforeEach(function() {
            scope.shouldRenderAction = jasmine.createSpy('shouldRenderAction').and.returnValue(true);
            scope.shouldRenderCategory = jasmine.createSpy('shouldRenderCategory').and.returnValue(true);
        });

        it('should render categories', inject(function() {
            //given
            scope.actions = [
                {
                    category: 'cat1',
                    categoryHtml: 'Category 1',
                    transformations: [
                        {name: '1', labelHtml: 'action 1'},
                        {name: '2', labelHtml: 'action 2'}
                    ]
                },
                {
                    category: 'cat2',
                    categoryHtml: 'Category 2',
                    transformations: [
                        {name: '3', labelHtml: 'action 3'},
                        {name: '4', labelHtml: 'action 4'},
                        {name: '5', labelHtml: 'action 5'}
                    ]
                }
            ];

            //when
            createElement();

            //then
            expect(element.find('.actions-category').length).toBe(2);
            expect(element.find('.actions-category').eq(0).text()).toBe('Category 1');
            expect(element.find('.actions-category').eq(1).text()).toBe('Category 2');
            expect(element.find('.actions-group').length).toBe(2);
        }));

        it('should render actions', inject(function() {
            //given
            scope.actions = [
                {
                    category: 'cat1',
                    categoryHtml: 'Category 1',
                    transformations: [
                        {name: '1', labelHtml: 'action 1'},
                        {name: '2', labelHtml: 'action 2'}
                    ]
                },
                {
                    category: 'cat2',
                    categoryHtml: 'Category 2',
                    transformations: [
                        {name: '3', labelHtml: 'action 3'},
                        {name: '4', labelHtml: 'action 4'},
                        {name: '5', labelHtml: 'action 5'}
                    ]
                }
            ];

            //when
            createElement();

            //then
            expect(element.find('.actions-group .trigger').length).toBe(5);
            expect(element.find('.actions-group .trigger').eq(0).text()).toBe('action 1');
            expect(element.find('.actions-group .trigger').eq(1).text()).toBe('action 2');
            expect(element.find('.actions-group .trigger').eq(2).text()).toBe('action 3');
            expect(element.find('.actions-group .trigger').eq(3).text()).toBe('action 4');
            expect(element.find('.actions-group .trigger').eq(4).text()).toBe('action 5');
        }));
    });

    describe('filter', function() {
        it('category', inject(function() {
            //given
            scope.shouldRenderAction = jasmine.createSpy('shouldRenderAction').and.returnValue(true);
            scope.shouldRenderCategory = function(cat) {
                return cat.category !== 'cat1';
            };
            scope.actions = [
                {
                    category: 'cat1',
                    categoryHtml: 'Category 1',
                    transformations: [
                        {name: '1', labelHtml: 'action 1'},
                        {name: '2', labelHtml: 'action 2'}
                    ]
                },
                {
                    category: 'cat2',
                    categoryHtml: 'Category 2',
                    transformations: [
                        {name: '3', labelHtml: 'action 3'},
                        {name: '4', labelHtml: 'action 4'},
                        {name: '5', labelHtml: 'action 5'}
                    ]
                }
            ];

            //when
            createElement();

            //then
            expect(element.find('.actions-category').length).toBe(1);
            expect(element.find('.actions-category').eq(0).text()).toBe('Category 2');
            expect(element.find('.actions-group').length).toBe(1);
            expect(element.find('.actions-group .trigger').length).toBe(3);
            expect(element.find('.actions-group .trigger').eq(0).text()).toBe('action 3');
            expect(element.find('.actions-group .trigger').eq(1).text()).toBe('action 4');
            expect(element.find('.actions-group .trigger').eq(2).text()).toBe('action 5');
        }));

        it('actions', inject(function() {
            //given
            scope.shouldRenderCategory = jasmine.createSpy('shouldRenderCategory').and.returnValue(true);
            scope.shouldRenderAction = function(action) {
                return action.name !== '2';
            };
            scope.actions = [
                {
                    category: 'cat1',
                    categoryHtml: 'Category 1',
                    transformations: [
                        {name: '1', labelHtml: 'action 1'},
                        {name: '2', labelHtml: 'action 2'}
                    ]
                },
                {
                    category: 'cat2',
                    categoryHtml: 'Category 2',
                    transformations: [
                        {name: '3', labelHtml: 'action 3'},
                        {name: '4', labelHtml: 'action 4'},
                        {name: '5', labelHtml: 'action 5'}
                    ]
                }
            ];

            //when
            createElement();

            //then
            expect(element.find('.actions-category').length).toBe(2);
            expect(element.find('.actions-category').eq(0).text()).toBe('Category 1');
            expect(element.find('.actions-category').eq(1).text()).toBe('Category 2');
            expect(element.find('.actions-group').length).toBe(2);
            expect(element.find('.actions-group .trigger').length).toBe(4);
            expect(element.find('.actions-group .trigger').eq(0).text()).toBe('action 1');
            expect(element.find('.actions-group .trigger').eq(1).text()).toBe('action 3');
            expect(element.find('.actions-group .trigger').eq(2).text()).toBe('action 4');
            expect(element.find('.actions-group .trigger').eq(3).text()).toBe('action 5');
        }));
    });
});