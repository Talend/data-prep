/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Editable Text widget directive', () => {
    'use strict';

    let scope;
    let element;
    let createElement;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            MY_PLACEHOLDER: 'My translated placeholder',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        createElement = () => {
            element = angular.element(`
                <talend-editable-text placeholder="MY_PLACEHOLDER"
                                      text="text"
                                      text-class="my-text-class"
                                      edition-mode="editionMode"
                                      on-text-click="onTextClick()"
                                      on-validate="onValidate"
                                      on-cancel="onCancel"></talend-editable-text>
            `);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('render', () => {
        it('should render all elements (edition and non edition)', () => {
            //when
            createElement();

            //then
            const nonEditionGroup = element.find('.editable-text-container').eq(0);
            expect(nonEditionGroup.find('.editable-text-text').length).toBe(1);
            expect(nonEditionGroup.find('button.edit-btn').length).toBe(1);

            const editionGroup = element.find('.editable-text-container').eq(1);
            expect(editionGroup.find('input.edition-text-input').length).toBe(1);
        });

        it('should render with visible edition mode elements', () => {
            //given
            scope.editionMode = true;

            //when
            createElement();

            //then
            const nonEditionGroup = element.find('.editable-text-container').eq(0);
            const editionGroup = element.find('.editable-text-container').eq(1);
            expect(editionGroup.hasClass('ng-hide')).toBe(false);
            expect(nonEditionGroup.hasClass('ng-hide')).toBe(true);
        });

        it('should render with visible NON edition mode elements', () => {
            //given
            scope.editionMode = false;

            //when
            createElement();

            //then
            const nonEditionGroup = element.find('.editable-text-container').eq(0);
            const editionGroup = element.find('.editable-text-container').eq(1);
            expect(editionGroup.hasClass('ng-hide')).toBe(true);
            expect(nonEditionGroup.hasClass('ng-hide')).toBe(false);
        });

        it('should render text element with provided text class', () => {
            //when
            createElement();

            //then
            expect(element.find('span.editable-text-text').hasClass('my-text-class')).toBe(true);
        });

        it('should render text element with provided text content', () => {
            //given
            scope.text = 'Jimmy';

            //when
            createElement();

            //then
            expect(element.find('span.editable-text-text').text()).toBe('Jimmy');
        });

        it('should render input with translated placeholder', () => {
            //when
            createElement();

            //then
            expect(element.find('input.edition-text-input').attr('placeholder')).toBe('My translated placeholder');
        });
    });

    describe('actions', () => {
        it('should execute text click callback', () => {
            //given
            scope.onTextClick = jasmine.createSpy('onTextClick');
            createElement();

            //when
            element.find('span.editable-text-text').click();

            //then
            expect(scope.onTextClick).toHaveBeenCalled();
        });

        it('should edit on edit button click', () => {
            //given
            scope.editionMode = false;
            scope.text = 'Jimmy';
            createElement();

            //when
            element.find('button.edit-btn').click();

            //then
            expect(scope.editionMode).toBe(true);
        });

        it('should execute cancel on input ESC keydown', () => {
            //given
            scope.editionMode = true;
            createElement();

            const ctrl = element.controller('talendEditableText');
            ctrl.cancel = jasmine.createSpy('onCancel');

            const event = angular.element.Event('keydown');
            event.keyCode = 27;

            //when
            element.find('input.edition-text-input').eq(0).trigger(event);
            scope.$digest();

            //then
            expect(ctrl.cancel).toHaveBeenCalled();
        });

        it('should NOT execute cancel on input other (not ESC) keydown', () => {
            //given
            scope.editionMode = true;
            createElement();

            const ctrl = element.controller('talendEditableText');
            ctrl.cancel = jasmine.createSpy('onCancel');

            const event = angular.element.Event('keydown');
            event.keyCode = 8;

            //when
            element.find('input.edition-text-input').eq(0).trigger(event);
            scope.$digest();

            //then
            expect(ctrl.cancel).not.toHaveBeenCalled();
        });
    });
});
