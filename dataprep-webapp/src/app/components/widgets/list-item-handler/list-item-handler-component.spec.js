/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const BTN_TOP_SELECTOR = '.btn.top';
const BTN_BOTTOM_SELECTOR = '.btn.bottom';

const I18N = {
    MOVE_UP: 'Move up',
    MOVE_DOWN: 'Move down',
};

describe('List Item Handler Component', () => {
    let scope;
    let createElement;
    let element;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', I18N);
        $translateProvider.preferredLanguage('en');
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        scope.showTopButton = true;
        scope.showBottomButton = true;
        scope.onTopClick = jasmine.createSpy('onTopClick');
        scope.onBottomClick = jasmine.createSpy('onBottomClick');

        createElement = () => {
            const template = `
                <list-item-handler show-top-button="showTopButton"
                                   show-bottom-button="showBottomButton"
                                   on-top-click="onTopClick()"
                                   on-bottom-click="onBottomClick()"></list-item-handler>
            `;
            element = $compile(template)(scope);
            scope.$digest();
        };
    }));

    describe('rendering', () => {
        it('should render two up and down buttons', () => {
            // given
            createElement();

            // then
            expect(element.find(BTN_TOP_SELECTOR).length).toBe(1);
            expect(element.find(BTN_BOTTOM_SELECTOR).length).toBe(1);
        });

        it('should render only up button', () => {
            // when
            scope.showBottomButton = false;

            // given
            createElement();

            // then
            expect(element.find(BTN_TOP_SELECTOR).length).toBe(1);
            expect(element.find(BTN_BOTTOM_SELECTOR).length).toBe(0);
        });

        it('should render aria-label for up button', () => {
            // given
            createElement();

            // then
            const btn = element.find(BTN_TOP_SELECTOR).eq(0);
            const btnAriaLabel = btn.attr('aria-label');
            expect(btnAriaLabel).toBe(I18N.MOVE_UP);
        });

        it('should render only down button', () => {
            // when
            scope.showTopButton = false;

            // given
            createElement();

            // then
            expect(element.find(BTN_TOP_SELECTOR).length).toBe(0);
            expect(element.find(BTN_BOTTOM_SELECTOR).length).toBe(1);
        });

        it('should render aria-label for down button', () => {
            // given
            createElement();

            // then
            const btn = element.find(BTN_BOTTOM_SELECTOR).eq(0);
            const btnAriaLabel = btn.attr('aria-label');
            expect(btnAriaLabel).toBe(I18N.MOVE_DOWN);
        });
    });

    describe('actions', () => {
        it('should call onTopClick method when top button is clicked', () => {
            // given
            createElement();

            // when
            element.find(BTN_TOP_SELECTOR).eq(0).click();

            // then
            expect(scope.onTopClick).toHaveBeenCalled();
        });

        it('should call onBottomClick method when bottom button is clicked', () => {
            // given
            createElement();

            // when
            element.find(BTN_BOTTOM_SELECTOR).eq(0).click();

            // then
            expect(scope.onBottomClick).toHaveBeenCalled();
        });
    });
});
