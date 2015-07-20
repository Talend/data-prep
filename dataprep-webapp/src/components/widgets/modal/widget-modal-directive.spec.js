describe('Dropdown directive', function () {
    'use strict';

    var scope, element, createElement, createFormElement, createNestedElement, createButtonElement, createBeforeCloseElement;

    beforeEach(module('talend.widget'));
    beforeEach(module('htmlTemplates'));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    beforeEach(inject(function ($rootScope, $compile, $timeout) {
        scope = $rootScope.$new();

        createElement = function () {
            var html = '<talend-modal fullscreen="fullscreen" state="state" on-close="onClose()" close-button="closeButton"></talend-modal>';
            scope.onClose = function() {
                scope.closeCallbackCalled = true;
            };
            element = $compile(html)(scope);
            scope.$digest();
            $timeout.flush();
        };

        createFormElement = function () {
            var html = '<talend-modal fullscreen="fullscreen" state="state" close-button="closeButton">' +
                '   <input type="text" id="firstInput" class="no-focus"/>' +
                '   <input type="text" id="secondInput" />' +
                '</talend-modal>';
            element = $compile(html)(scope);
            scope.$digest();
            $timeout.flush();
        };

        createNestedElement = function () {
            var html = '<talend-modal id="outerModal" fullscreen="fullscreen" state="state" close-button="closeButton">' +
                '   <talend-modal id="innerModal" fullscreen="innerfullscreen" state="innerState" close-button="innerCloseButton"></talend-modal>' +
                '</talend-modal>';
            element = $compile(html)(scope);
            scope.$digest();
            $timeout.flush();
        };

        createButtonElement = function () {
            var html = '<talend-modal fullscreen="fullscreen" state="state" close-button="closeButton" disable-enter="disableEnter">' +
                '   <button class="modal-primary-button" ng-click="click()"/>' +
                '</talend-modal>';
            scope.click = function() {
                scope.primaryButtonClicked = true;
            };
            element = $compile(html)(scope);
            scope.$digest();
            $timeout.flush();
        };

        createBeforeCloseElement = function (beforeCloseFn) {
            var html = '<talend-modal fullscreen="fullscreen" state="state" before-close="beforeClose()" close-button="closeButton"></talend-modal>';
            scope.beforeClose = beforeCloseFn;
            element = $compile(html)(scope);
            scope.$digest();
            $timeout.flush();
        };

        spyOn($rootScope, '$apply').and.callThrough();
    }));

    describe('display', function() {
        it('should show "normal" close button', function () {
            //given
            scope.fullscreen = false;
            scope.state = false;
            scope.closeButton = true;

            //when
            createElement();

            //then
            expect(element.find('.modal-close').length).toBe(1);
            expect(element.find('.modal-close').hasClass('ng-hide')).toBe(false);
        });

        it('should not show "normal" close button', function () {
            //given
            scope.fullscreen = false;
            scope.state = false;
            scope.closeButton = false;

            //when
            createElement();

            //then
            expect(element.find('.modal-close').length).toBe(1);
            expect(element.find('.modal-close').hasClass('ng-hide')).toBe(true);
        });

        it('should show "fullscreen" close button', function () {
            //given
            scope.fullscreen = true;
            scope.state = false;
            scope.closeButton = true;

            //when
            createElement();

            //then
            expect(element.find('.modal-header-close').length).toBe(1);
            expect(element.find('.modal-close').hasClass('ng-hide')).toBe(false);
        });

        it('should not show "fullscreen" close button', function () {
            //given
            scope.fullscreen = true;
            scope.state = false;
            scope.closeButton = false;

            //when
            createElement();

            //then
            expect(element.find('.modal-header-close').length).toBe(1);
            expect(element.find('.modal-header-close').hasClass('ng-hide')).toBe(true);
        });

        it('should add "modal-open" class to body when modal open state is true', function () {
            //given
            var body = angular.element('body');

            scope.fullscreen = false;
            scope.state = false;
            scope.closeButton = false;
            createElement();

            //when
            scope.state = true;
            scope.$apply();

            //then
            expect(body.hasClass('modal-open')).toBe(true);
        });

        it('should remove "modal-open" class to body when modal open state is false', function () {
            //given
            var body = angular.element('body');
            body.addClass('modal-open');

            scope.fullscreen = false;
            scope.state = true;
            scope.closeButton = false;
            createElement();

            //when
            scope.state = false;
            scope.$digest();

            //then
            expect(body.hasClass('modal-open')).toBe(false);
        });
    });

    describe('actions', function() {
        it('should hide modal on "modal-window" div click', inject(function ($timeout) {
            //given
            scope.fullscreen = false;
            scope.state = true;
            scope.closeButton = false;
            createElement();

            //when
            element.find('.modal-window').click();
            $timeout.flush();

            //then
            expect(scope.state).toBe(false);
        }));

        it('should hide modal on "modal-close" button click', inject(function ($timeout) {
            //given
            scope.fullscreen = false;
            scope.state = true;
            scope.closeButton = true;
            createElement();

            //when
            element.find('.modal-close').click();
            $timeout.flush();

            //then
            expect(scope.state).toBe(false);
        }));

        it('should hide modal on "modal-header-close" button click', inject(function ($timeout) {
            //given
            scope.fullscreen = true;
            scope.state = true;
            scope.closeButton = true;
            createElement();

            //when
            element.find('.modal-header-close').click();
            $timeout.flush();

            //then
            expect(scope.state).toBe(false);
        }));

        it('should not hide modal on "modal-inner" div click', inject(function ($rootScope, $timeout) {
            //given
            scope.fullscreen = false;
            scope.state = true;
            scope.closeButton = true;
            createElement();

            //when
            element.find('.modal-inner').click();
            try {
                $timeout.flush();
            }
            catch (error) {
                $rootScope.$apply();

                //then
                expect(scope.state).toBe(true);
                return;
            }
            throw new Error('Should have thrown error on timeout flush because hide should not be called on click in modal-inner div');
        }));

        it('should hide on ESC keydown', inject(function ($rootScope, $timeout) {
            //given
            scope.fullscreen = false;
            scope.state = true;
            scope.closeButton = true;
            createElement();

            var event = angular.element.Event('keydown');
            event.keyCode = 27;

            //when
            element.find('.modal-inner').trigger(event);
            $timeout.flush();

            //then
            expect(scope.state).toBe(false);
        }));

        it('should not hide on not ESC keydown', inject(function ($rootScope, $timeout) {
            //given
            scope.fullscreen = false;
            scope.state = true;
            scope.closeButton = true;
            createElement();

            var event = angular.element.Event('keydown');
            event.keyCode = 97;

            //when
            element.find('.modal-inner').trigger(event);
            try {
                $timeout.flush();
            }
                //then
            catch (error) {
                expect(scope.state).toBe(true);
                return;
            }

            //otherwise
            throw new Error('should have thrown error because no timeout is pending');
        }));

        it('should hit primary button on ENTER keydown', inject(function () {
            //given
            scope.fullscreen = false;
            scope.state = true;
            scope.closeButton = true;
            createButtonElement();

            expect(scope.primaryButtonClicked).toBeFalsy();
            var event = angular.element.Event('keydown');
            event.keyCode = 13;

            //when
            element.find('.modal-inner').trigger(event);
            scope.$digest();

            //then
            expect(scope.primaryButtonClicked).toBe(true);
        }));

        it('should not hit primary button on ENTER keydown when disable-enter attribute is true', inject(function () {
            //given
            scope.fullscreen = false;
            scope.state = true;
            scope.closeButton = true;
            scope.disableEnter = true;
            createButtonElement();

            expect(scope.primaryButtonClicked).toBeFalsy();
            var event = angular.element.Event('keydown');
            event.keyCode = 13;

            //when
            element.find('.modal-inner').trigger(event);
            scope.$digest();

            //then
            expect(scope.primaryButtonClicked).toBeFalsy();
        }));

        it('should call close callback', inject(function($rootScope) {
            //given
            scope.state = true;
            createElement();
            expect(scope.closeCallbackCalled).toBeFalsy();

            //when
            scope.state = false;
            $rootScope.$apply();

            //then
            expect(scope.closeCallbackCalled).toBe(true);
        }));

        it('should NOT close modal when beforeClose returns false', inject(function($timeout) {
            //given
            scope.state = true;
            createBeforeCloseElement(function() {
                return false;
            });

            //when
            element.find('.modal-close').click();
            $timeout.flush();

            //then
            expect(scope.state).toBe(true);
        }));

        it('should close modal when beforeClose returns true', inject(function($timeout) {
            //given
            scope.state = true;
            createBeforeCloseElement(function() {
                return true;
            });

            //when
            element.find('.modal-close').click();
            $timeout.flush();

            //then
            expect(scope.state).toBe(false);
        }));
    });

    describe('element', function() {
        it('should attach popup to body', function () {
            //when
            createElement();

            //then
            expect(angular.element('body').find('talend-modal').length).toBe(1);
        });

        it('should remove element on scope destroy', function () {
            //given
            createElement();

            //when
            scope.$destroy();
            scope.$digest();

            //then
            expect(angular.element('body').find('talend-modal').length).toBe(0);
        });
    });

    describe('multi modal management', function() {
        it('should focus on "modal-inner" on module open', function () {
            //given
            scope.fullscreen = false;
            scope.state = false;
            scope.closeButton = false;
            createElement();

            var body = angular.element('body');
            body.append(element);
            expect(document.activeElement).not.toBe(element);

            //when
            scope.state = true;
            scope.$digest();

            //then
            expect(document.activeElement.className).toBe('modal-inner');
        });

        it('should focus on second input on show and select the text coz first has "no-focus" class', inject(function($timeout) {
            //given
            scope.fullscreen = false;
            scope.state = false;
            scope.closeButton = false;
            createFormElement();

            //set a text in secondInput
            $('#secondInput').val('city');

            var body = angular.element('body');
            body.append(element);
            expect(document.activeElement).not.toBe(element);

            //when
            scope.state = true;
            scope.$digest();
            $timeout.flush();

            //then
            expect(document.activeElement.id).toBe('secondInput');
            expect(window.getSelection().toString()).toBe('city');
        }));

        it('should focus on next last shown modal on focused modal close', function () {
            //given : init
            scope.fullscreen = false;
            scope.state = false;
            scope.closeButton = false;
            scope.innerFullscreen = false;
            scope.innerState = false;
            scope.innerCloseButton = false;
            createNestedElement();

            var body = angular.element('body');
            body.append(element);
            expect(document.activeElement).not.toBe(element);

            //given : show outer modal
            scope.state = true;
            scope.$digest();
            var outerModal = body.find('#outerModal').eq(0).find('.modal-inner').eq(0)[0];
            expect(document.activeElement).toBe(outerModal);

            //given : show inner modal
            scope.innerState = true;
            scope.$digest();
            var innerModal = body.find('#innerModal').eq(0).find('.modal-inner').eq(0)[0];
            expect(document.activeElement).toBe(innerModal);

            //when
            scope.innerState = false;
            scope.$digest();

            //then
            expect(document.activeElement).toBe(outerModal);
        });
    });
});