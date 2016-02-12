/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Badge directive', function () {
    'use strict';

    var scope, createElement, element;
    var obj = {value: 'toto', editable: true};
    var fns = {
        change: function() {},
        close: function() {}
    };

    beforeEach(angular.mock.module('talend.widget'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'COLON': ': '
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(function() {
        spyOn(fns, 'change').and.returnValue();
        spyOn(fns, 'close').and.returnValue();
    });

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    describe('simple badge', function() {
        beforeEach(inject(function ($rootScope, $compile, $timeout) {
            scope = $rootScope.$new();
            scope.close = fns.close;

            createElement = function () {
                var template = '<talend-badge on-close="close()" text="Displayed text"></talend-badge>';
                element = $compile(template)(scope);
                $timeout.flush();
                scope.$digest();
            };
        }));

        it('should only render static text', function () {
            //when
            createElement();

            //then
            expect(element.find('span.badge-item').length).toBe(1);
            expect(element.find('.text').eq(0).text()).toBe('Displayed text');
            expect(element.find('.editable-input').length).toBe(0);
        });

        it('should call onClose method on badge close button clicked', function () {
            //given
            createElement();

            //when
            element.find('.badge-close').eq(0).click();

            //then
            expect(fns.close).toHaveBeenCalled();
        });
    });

    describe('complex badge', function() {
        beforeEach(inject(function ($rootScope, $compile, $timeout) {
            scope = $rootScope.$new();
            scope.change = fns.change;
            scope.close = fns.close;
            scope.obj = obj;

            createElement = function () {
                var template = '<talend-badge on-close="close()" on-change="change()" obj="obj" text="Displayed text" editable="true"></talend-badge>';
                element = $compile(template)(scope);
                $timeout.flush();
                scope.$digest();
            };
        }));

        it('should only render static text and input', function () {
            //when
            createElement();

            //then
            expect(element.find('span.badge-item').length).toBe(2);
            expect(element.find('span.badge-item').eq(0).text()).toBe('Displayed text: ');
            expect(element.find('.editable-input').eq(0).val()).toBe('toto');
        });

        it('should adjust input size on obj.value change', function () {
            //given
            createElement();
            var input = element.find('.editable-input').eq(0);
            expect(input.css('width')).toBe('35px');

            //when
            scope.obj = {value: 'tatatetetititototutu'};
            scope.$digest();

            //then
            expect(input.css('width')).toBe('147px');
        });

        it('should adjust input size on keyboard down press', function () {
            //given
            createElement();
            var input = element.find('.editable-input').eq(0);
            expect(input.css('width')).toBe('35px');

            var event = angular.element.Event('keydown');
            event.keyCode = 65;

            //when
            element.controller('talendBadge').value = 'totoa';
            input.trigger(event);
            scope.$digest();

            //then
            expect(input.css('width')).toBe('42px');
        });

        it('should perform change on ENTER key down', function () {
            //given
            createElement();
            var input = element.find('.editable-input').eq(0);
            element.controller('talendBadge').value = 'titi';

            var event = angular.element.Event('keydown');
            event.keyCode = 13;

            //when
            input.trigger(event);
            scope.$digest();

            //then
            expect(fns.change).toHaveBeenCalled();
        });

        it('should reset input value on ESC key down', function () {
            //given
            createElement();
            var input = element.find('.editable-input').eq(0);
            var ctrl = element.controller('talendBadge');
            ctrl.value = 'titi';

            var event = angular.element.Event('keydown');
            event.keyCode = 27;

            //when
            input.trigger(event);
            scope.$digest();

            //then
            expect(ctrl.value).toBe('toto');
            expect(fns.change).not.toHaveBeenCalled();
        });

        it('should stop propagation on ESC key down', function () {
            //given
            createElement();

            var bodyEscEvent = false;
            var escEventListener = function(event) {
                if(event.keyCode === 27) {
                    bodyEscEvent = true;
                }
            };
            var body = angular.element('body');
            body.append(element);
            body.keydown(escEventListener);

            var event = angular.element.Event('keydown');
            event.keyCode = 27;

            //when
            element.find('.editable-input').eq(0).trigger(event);
            scope.$digest();

            //then
            expect(bodyEscEvent).toBe(false);

            //finally
            body.off('keydown', escEventListener);
        });
    });
});