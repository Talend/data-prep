/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Recipe knot', () => {

    let createElement;
    let element;
    let scope;

    beforeEach(angular.mock.module('data-prep.recipe-knot'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            ENABLE_DISABLE_STEP: 'Enable or disable step',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        scope.stepHoverStart = jasmine.createSpy('stepHoverStart');
        scope.stepHoverEnd = jasmine.createSpy('stepHoverEnd');
        scope.toggleStep = jasmine.createSpy('toggleStep');

        createElement = () => {
            element = angular.element(`
                <recipe-knot class="step-trigger"
                     inactive="inactive"
                     on-hover-start="stepHoverStart()"
                     on-hover-end="stepHoverEnd()"
                     show-bottom-line="showBottomLine"
                     show-top-line="showTopLine"
                     to-be-switched="toBeSwitched"
                     toggle-step="toggleStep()">
                 </recipe-knot>`);
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('inactive knot', () => {
        it('should render a single inactive knot', () => {
            //given
            scope.inactive = true;
            scope.showBottomLine = false;
            scope.showTopLine = false;
            scope.toBeSwitched = false;

            //when
            createElement();

            //then
            expect(element.find('.line.no-line').length).toBe(2);

            expect(element.find('.circle.inactive-knot').length).toBe(1);
            expect(element.find('.circle.inactive-knot.to-be-activated').length).toBe(0);
            expect(element.find('.circle.inactive-knot.to-be-deactivated').length).toBe(0);

            expect(element.find('.line.bottom-line.no-line').length).toBe(1);
        });

        it('should render: 1st knot among 2, inactive, and will be activated', () => {
            //given
            scope.inactive = true;
            scope.showBottomLine = true;
            scope.showTopLine = false;
            scope.toBeSwitched = true;

            //when
            createElement();

            //then
            expect(element.find('.line.no-line').length).toBe(1);
            expect(element.find('.line').eq(0).hasClass('no-line')).toBe(true);
            expect(element.find('.line').eq(1).hasClass('no-line')).toBe(false);

            expect(element.find('.circle.inactive-knot.to-be-activated').length).toBe(1);
            expect(element.find('.circle.inactive-knot.to-be-deactivated').length).toBe(0);

            expect(element.find('.line.bottom-line').length).toBe(1);
            expect(element.find('.line.bottom-line.no-line').length).toBe(0);
        });

        it('should render: middle knot, inactive, and will be activated', () => {
            //given
            scope.inactive = true;
            scope.showBottomLine = true;
            scope.showTopLine = true;
            scope.toBeSwitched = true;

            //when
            createElement();

            //then
            expect(element.find('.line.no-line').length).toBe(0);

            expect(element.find('.circle.inactive-knot.to-be-activated').length).toBe(1);
            expect(element.find('.circle.inactive-knot.to-be-deactivated').length).toBe(0);

            expect(element.find('.line.bottom-line').length).toBe(1);
            expect(element.find('.line.bottom-line.no-line').length).toBe(0);
        });

        it('should render: last knot among 2, inactive, and will be activated', () => {
            //given
            scope.inactive = true;
            scope.showBottomLine = false;
            scope.showTopLine = true;
            scope.toBeSwitched = true;

            //when
            createElement();

            //then
            expect(element.find('.line.no-line').length).toBe(1);
            expect(element.find('.line').eq(0).hasClass('no-line')).toBe(false);
            expect(element.find('.line').eq(1).hasClass('no-line')).toBe(true);

            expect(element.find('.circle.inactive-knot.to-be-activated').length).toBe(1);
            expect(element.find('.circle.inactive-knot.to-be-deactivated').length).toBe(0);

            expect(element.find('.line.bottom-line.no-line').length).toBe(1);
        });
    });

    describe('active knot', () => {
        it('should render a single active knot', () => {
            //given
            scope.inactive = false;
            scope.showBottomLine = false;
            scope.showTopLine = false;
            scope.toBeSwitched = false;

            //when
            createElement();

            //then
            expect(element.find('.line.no-line').length).toBe(2);

            expect(element.find('.circle').length).toBe(1);
            expect(element.find('.circle.inactive-knot').length).toBe(0);
            expect(element.find('.circle.inactive-knot.to-be-activated').length).toBe(0);
            expect(element.find('.circle.inactive-knot.to-be-deactivated').length).toBe(0);

            expect(element.find('.line.bottom-line.no-line').length).toBe(1);
        });

        it('should render: 1st knot among 2, active, and will be deactivated', () => {
            //given
            scope.inactive = false;
            scope.showBottomLine = true;
            scope.showTopLine = false;
            scope.toBeSwitched = true;

            //when
            createElement();

            //then
            expect(element.find('.line.no-line').length).toBe(1);
            expect(element.find('.line').eq(0).hasClass('no-line')).toBe(true);
            expect(element.find('.line').eq(1).hasClass('no-line')).toBe(false);

            expect(element.find('.circle.inactive-knot.to-be-activated').length).toBe(0);
            expect(element.find('.circle.to-be-deactivated').length).toBe(1);

            expect(element.find('.line.bottom-line').length).toBe(1);
            expect(element.find('.line.bottom-line.no-line').length).toBe(0);
        });

        it('should render: middle knot, active, and will be deactivated', () => {
            //given
            scope.inactive = false;
            scope.showBottomLine = true;
            scope.showTopLine = true;
            scope.toBeSwitched = true;

            //when
            createElement();

            //then
            expect(element.find('.line.no-line').length).toBe(0);

            expect(element.find('.circle.inactive-knot.to-be-activated').length).toBe(0);
            expect(element.find('.circle.to-be-deactivated').length).toBe(1);

            expect(element.find('.line.bottom-line').length).toBe(1);
            expect(element.find('.line.bottom-line.no-line').length).toBe(0);
        });

        it('should render: last knot among 2, active, and will be deactivated', () => {
            //given
            scope.inactive = false;
            scope.showBottomLine = false;
            scope.showTopLine = true;
            scope.toBeSwitched = true;

            //when
            createElement();

            //then
            expect(element.find('.line.no-line').length).toBe(1);
            expect(element.find('.line').eq(0).hasClass('no-line')).toBe(false);
            expect(element.find('.line').eq(1).hasClass('no-line')).toBe(true);

            expect(element.find('.circle.inactive-knot.to-be-activated').length).toBe(0);
            expect(element.find('.circle.to-be-deactivated').length).toBe(1);

            expect(element.find('.line.bottom-line.no-line').length).toBe(1);
        });
    });

    describe('Events', () => {
        it('should call toggle step callback on knot click', () => {
            //given
            scope.inactive = false;
            scope.showBottomLine = false;
            scope.showTopLine = false;
            scope.toBeSwitched = false;
            createElement();

            //when
            element.find('.knot').eq(0).click();

            //then
            expect(scope.toggleStep).toHaveBeenCalled();
        });

        it('should call toggle step callback on knot hover', () => {
            //given
            scope.inactive = false;
            scope.showBottomLine = false;
            scope.showTopLine = false;
            scope.toBeSwitched = false;
            createElement();

            //when
            element.find('.knot').eq(0).mouseover();

            //then
            expect(scope.stepHoverStart).toHaveBeenCalled();
        });

        it('should call toggle step callback on knot mouseleave', () => {
            //given
            scope.inactive = false;
            scope.showBottomLine = false;
            scope.showTopLine = false;
            scope.toBeSwitched = false;
            createElement();

            //when
            element.find('.knot').eq(0).mouseleave();

            //then
            expect(scope.stepHoverEnd).toHaveBeenCalled();
        });
    });
});
