/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Accordion directive', function () {
    'use strict';

    var scope, createElement;

    var clickOnTriggerElement = function(element, index) {
        element.find('.accordion').eq(index).find('.trigger').click();
    };

    var expectElementToBeVisible = function(element) {
        expect(element.css('display')).toBe('block');
    };
    var expectElementToBeHidden = function(element) {
        expect(element.is(':visible')).toBe(false);
    };

    beforeEach(angular.mock.module('talend.widget'));
    beforeEach(angular.mock.module('htmlTemplates'));

    afterEach(function () {
        scope.$destroy();
    });

    describe('without open callback', function() {

        beforeEach(inject(function ($rootScope, $compile, $timeout) {
            scope = $rootScope.$new();

            createElement = function () {
                var template = '<talend-accordions>' +
                    '   <talend-accordions-item>' +
                    '       <a class="trigger">1. Uppercase on column LASTNAME</a>' +
                    '   </talend-accordions-item>' +
                    '   <talend-accordions-item>' +
                    '       <a class="trigger">2. Replace empty in column STATE</a>' +
                    '       <div class="content">content1</div>' +
                    '   </talend-accordions-item>' +
                    '   <talend-accordions-item>' +
                    '       <a class="trigger">2bis. Replace empty in column STATE</a>' +
                    '       <div class="content">content2</div>' +
                    '   </talend-accordions-item>' +
                    '   <talend-accordions-item>' +
                    '       <a class="trigger">Lowercase on column Firstname</a>' +
                    '   </talend-accordions-item>' +
                    '</talend-accordions>';
                var element = $compile(template)(scope);
                scope.$digest();
                $timeout.flush();
                return element;
            };
        }));


        it('should open the first sub-menu on click on the corresponding trigger', function () {
            //given
            var element = createElement(scope);
            var firstSubmenu = element.find('.accordion').eq(1).find('.content');
            var secondSubmenu = element.find('.accordion').eq(2).find('.content');

            expectElementToBeHidden(firstSubmenu);
            expectElementToBeHidden(secondSubmenu);

            //when
            clickOnTriggerElement(element, 1);

            //then
            expectElementToBeVisible(firstSubmenu);
            expectElementToBeHidden(secondSubmenu);
        });

        it('should close the first sub-menu on click on the corresponding trigger', function () {
            //given
            var element = createElement(scope);
            var firstSubmenu = element.find('.accordion').eq(1).find('.content');
            var secondSubmenu = element.find('.accordion').eq(2).find('.content');

            clickOnTriggerElement(element, 1);
            expectElementToBeVisible(firstSubmenu);
            expectElementToBeHidden(secondSubmenu);

            //when
            clickOnTriggerElement(element, 1);

            //then
            expectElementToBeHidden(firstSubmenu);
            expectElementToBeHidden(secondSubmenu);
        });

        it('should close the other sub-menus on click on a trigger', function () {
            //given
            var element = createElement(scope);
            var firstSubmenu = element.find('.accordion').eq(1).find('.content');
            var secondSubmenu = element.find('.accordion').eq(2).find('.content');

            clickOnTriggerElement(element, 1);
            expectElementToBeVisible(firstSubmenu);
            expectElementToBeHidden(secondSubmenu);

            //when
            clickOnTriggerElement(element, 0);

            //then
            expectElementToBeHidden(firstSubmenu);
            expectElementToBeHidden(secondSubmenu);
        });

        it('should unregister accrodions on scope destroy', function () {
            //given
            var element = createElement();
            expect(element.controller('talendAccordions').accordions.length).toBe(4);

            //when
            scope.$destroy();

            //then
            expect(element.controller('talendAccordions').accordions.length).toBe(0);
        });
    });

    describe('with open callback', function() {
        beforeEach(inject(function ($rootScope, $compile, $timeout) {
            scope = $rootScope.$new();
            scope.onOpen = function() {};

            createElement = function () {
                var template = '<talend-accordions>' +
                    '   <talend-accordions-item>' +
                    '       <a class="trigger">1. Uppercase on column LASTNAME</a>' +
                    '   </talend-accordions-item>' +
                    '   <talend-accordions-item on-open="onOpen()">' +
                    '       <a class="trigger">2. Replace empty in column STATE</a>' +
                    '       <div class="content">content1</div>' +
                    '   </talend-accordions-item>' +
                    '   <talend-accordions-item>' +
                    '       <a class="trigger">2bis. Replace empty in column STATE</a>' +
                    '       <div class="content">content2</div>' +
                    '   </talend-accordions-item>' +
                    '   <talend-accordions-item>' +
                    '       <a class="trigger">3. Lowercase on column Firstname</a>' +
                    '   </talend-accordions-item>' +
                    '</talend-accordions>';

                var element = $compile(template)(scope);
                scope.$digest();
                $timeout.flush();
                return element;
            };

            spyOn(scope, 'onOpen').and.returnValue(true);
        }));


        it('should open the second sub-menu on click on the corresponding trigger', function() {
            //given
            var element = createElement(scope);

            //when
            clickOnTriggerElement(element, 1);

            //then
            expect(scope.onOpen).toHaveBeenCalled();
        });
    });

    describe('with default accordion', function() {
        beforeEach(inject(function ($rootScope, $compile, $timeout) {
            scope = $rootScope.$new();

            createElement = function () {
                var template = '<talend-accordions>' +
                    '   <talend-accordions-item>' +
                    '       <a class="trigger">1. Uppercase on column LASTNAME</a>' +
                    '   </talend-accordions-item>' +
                    '   <talend-accordions-item default="true">' +
                    '       <a class="trigger">2. Replace empty in column STATE</a>' +
                    '       <div class="content">content1</div>' +
                    '   </talend-accordions-item>' +
                    '   <talend-accordions-item>' +
                    '       <a class="trigger">2bis. Replace empty in column STATE</a>' +
                    '       <div class="content">content2</div>' +
                    '   </talend-accordions-item>' +
                    '   <talend-accordions-item>' +
                    '       <a class="trigger">3. Lowercase on column Firstname</a>' +
                    '   </talend-accordions-item>' +
                    '</talend-accordions>';

                var element = $compile(template)(scope);
                scope.$digest();
                $timeout.flush();
                return element;
            };
        }));


        it('should open default accordion', function() {
            //when
            var element = createElement(scope);

            //then
            var submenu = element.find('.accordion').eq(1).find('.content');
            expectElementToBeVisible(submenu);
        });
    });
});