describe('Editable Text widget directive', function () {
    'use strict';

    var scope, element, createElement;

    beforeEach(module('talend.widget'));
    beforeEach(module('htmlTemplates'));

    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'MY_PLACEHOLDER': 'My translated placeholder'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($rootScope, $compile, $timeout) {
        scope = $rootScope.$new();

        createElement = function () {
            element = angular.element('<talend-editable-text placeholder="MY_PLACEHOLDER" text="text" text-class="my-text-class" edition-mode="editionMode" on-text-click="onTextClick()" on-validate="onValidate" on-cancel="onCancel"></talend-editable-text>');
            angular.element('body').append(element);
            $compile(element)(scope);
            scope.$digest();
            $timeout.flush();
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    describe('render', function () {
        it('should render with visible edition mode elements', function () {
            //given
            scope.editionMode = true;
            scope.text = 'Jimmy';

            //when
            createElement();

            //then
            expect(element.find('input.edition-text-input').is(':visible')).toBe(true);
            expect(element.find('button.valid-btn').is(':visible')).toBe(true);
            expect(element.find('button.cancel-btn').is(':visible')).toBe(true);

            expect(element.find('span.editable-text-text').is(':visible')).toBe(false);
            expect(element.find('button.edit-btn').is(':visible')).toBe(false);
        });

        it('should render with visible NON edition mode elements', function () {
            //given
            scope.editionMode = false;
            scope.text = 'Jimmy';

            //when
            createElement();

            //then
            expect(element.find('input.edition-text-input').is(':visible')).toBe(false);
            expect(element.find('button.valid-btn').is(':visible')).toBe(false);
            expect(element.find('button.cancel-btn').is(':visible')).toBe(false);

            expect(element.find('span.editable-text-text').is(':visible')).toBe(true);
            expect(element.find('button.edit-btn').is(':visible')).toBe(true);
        });

        it('should render text element with provided text class', function () {
            //given
            scope.editionMode = false;
            scope.text = 'Jimmy';

            //when
            createElement();

            //then
            expect(element.find('span.editable-text-text').hasClass('my-text-class')).toBe(true);
        });

        it('should render text element with provided text content', function () {
            //given
            scope.editionMode = false;
            scope.text = 'Jimmy';

            //when
            createElement();

            //then
            expect(element.find('span.editable-text-text').text()).toBe('Jimmy');
        });

        it('should render input with translated placeholder', function () {
            //given
            scope.editionMode = false;
            scope.text = 'Jimmy';

            //when
            createElement();

            //then
            expect(element.find('input.edition-text-input').attr('placeholder')).toBe('My translated placeholder');
        });
    });

    describe('actions', function() {
        it('should execute text click callback', function() {
            //given
            scope.onTextClick = jasmine.createSpy('onTextClick');
            createElement();

            //when
            element.find('span.editable-text-text').click();

            //then
            expect(scope.onTextClick).toHaveBeenCalled();
        });

        it('should edit on edit button click', function() {
            //given
            scope.editionMode = false;
            scope.text = 'Jimmy';
            createElement();

            //when
            element.find('button.edit-btn').click();

            //then
            expect(scope.editionMode).toBe(true);
        });

        it('should validate on validation button click', function() {
            //given
            scope.editionMode = true;
            scope.text = 'Jimmy';
            createElement();

            var ctrl = element.controller('talendEditableText');
            ctrl.editionText = 'no Jimmy';
            ctrl.onValidate = jasmine.createSpy('onValidate');

            //when
            element.find('button.valid-btn').click();

            //then
            expect(ctrl.onValidate).toHaveBeenCalled();
        });

        it('should switch to NON edition mode on validation button click', function() {
            //given
            scope.editionMode = true;
            createElement();

            //when
            element.find('button.valid-btn').click();

            //then
            expect(scope.editionMode).toBe(false);
        });

        it('should execute cancel callback on cancel button click', function() {
            //given
            scope.editionMode = true;
            createElement();

            var ctrl = element.controller('talendEditableText');
            ctrl.onCancel = jasmine.createSpy('onCancel');

            //when
            element.find('button.cancel-btn').click();
            scope.$digest();

            //then
            expect(ctrl.onCancel).toHaveBeenCalled();
        });

        it('should switch to NON edition mode on cancel button click', function() {
            //given
            scope.editionMode = true;
            createElement();

            //when
            element.find('button.cancel-btn').click();

            //then
            expect(scope.editionMode).toBe(false);
        });

        it('should execute cancel on input ESC keydown', function() {
            //given
            scope.editionMode = true;
            createElement();

            var ctrl = element.controller('talendEditableText');
            ctrl.cancel = jasmine.createSpy('onCancel');

            var event = angular.element.Event('keydown');
            event.keyCode = 27;

            //when
            element.find('input.edition-text-input').eq(0).trigger(event);
            scope.$digest();

            //then
            expect(ctrl.cancel).toHaveBeenCalled();
        });

        it('should NOT execute cancel on input not ESC keydown', function() {
            //given
            scope.editionMode = true;
            createElement();

            var ctrl = element.controller('talendEditableText');
            ctrl.cancel = jasmine.createSpy('onCancel');

            var event = angular.element.Event('keydown');
            event.keyCode = 8;

            //when
            element.find('input.edition-text-input').eq(0).trigger(event);
            scope.$digest();

            //then
            expect(ctrl.cancel).not.toHaveBeenCalled();
        });
    });
});
