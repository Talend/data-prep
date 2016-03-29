/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

'use strict';

describe('Typeahead directive', () => {
    var scope, element, ctrl;

    beforeEach(angular.mock.module('talend.widget'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(($rootScope, $compile) => {

        scope = $rootScope.$new();
        scope.search = jasmine.createSpy('search');
        scope.clickOnItem1 = jasmine.createSpy('clickOnItem1');
        var html = `
            <typeahead search="search"
                       placeholder="Type here"
                       searching-text="Searching ..."
                       custom-render="true">
                <ul id="inventory">
                    <li id="item1"><div ng-click="clickOnItem1()"></div></li>
                    <li id="item2"></li>
                    <li id="item3"><a href="http"></a></li>

                </ul>
            </typeahead>
        `;
        element = $compile(html)(scope);
        angular.element('body').append(element);
        scope.$digest();

        ctrl = element.controller('typeahead');
        ctrl.searchString = 'aze';
        ctrl.visible = true;


        scope.$digest();
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('render', () => {
        it('should render input', () => {
            //then
            expect(element.find('input[type="search"]').length).toBe(1);
        });

        it('should render searching message', () => {
            //given
            ctrl.searching = true;

            //when
            scope.$digest();

            //then
            expect(element.find('.searching').length).toBe(1);
        });

        it('should render searching message', () => {
            //given
            ctrl.searching = true;

            //when
            scope.$digest();

            //then
            expect(element.find('.searching').length).toBe(1);
            expect(element.find('.searching').eq(0).text()).toBe('Searching ...');
        });

        it('should render custom result', () => {
            //then
            expect(element.find('#inventory').length).toBe(1);
        });
    });

    describe('input keydown', () => {
        it('should hide results on ESC', () => {
            //given
            const input = element.find('.input-search');
            const event = angular.element.Event('keydown');
            event.keyCode = 27;

            expect(element.find('.typeahead-result').hasClass('ng-hide')).toBe(false);

            //when
            input.trigger(event);

            //then
            expect(element.find('.typeahead-result').hasClass('ng-hide')).toBe(true);
        });

        it('should NOT hide results on non ESC', () => {
            //given
            const input = element.find('.input-search');
            const event = angular.element.Event('keydown');
            event.keyCode = 13;

            expect(element.find('.typeahead-result').hasClass('ng-hide')).toBe(false);

            //when
            input.trigger(event);

            //then
            expect(element.find('.typeahead-result').hasClass('ng-hide')).toBe(false);
        });

        it('should select first item on a keydown arrow down', () => {
            //given
            const input = element.find('.input-search');
            const event = angular.element.Event('keydown');
            event.keyCode = 40;

            //when
            input.trigger(event);

            //then
            expect(element.find('#item1').hasClass('selected')).toBe(true);
            expect(element.find('#item2').hasClass('selected')).toBe(false);
            expect(element.find('#item3').hasClass('selected')).toBe(false);
        });

        it('should select item on keydown up/down', () => {
            //given
            const input = element.find('.input-search');
            const event = angular.element.Event('keydown');
            event.keyCode = 40;

            input.trigger(event);
            input.trigger(event);
            input.trigger(event);

            const event2 = angular.element.Event('keydown');
            event2.keyCode = 38;

            //when
            input.trigger(event2);

            //then
            expect(element.find('#item1').hasClass('selected')).toBe(false);
            expect(element.find('#item2').hasClass('selected')).toBe(true);
            expect(element.find('#item3').hasClass('selected')).toBe(false);
        });

        it('should select 1st item on keydown arrow down on last item', () => {
            //given
            const input = element.find('.input-search');
            const event = angular.element.Event('keydown');
            event.keyCode = 40;

            input.trigger(event);
            input.trigger(event);
            input.trigger(event);
            input.trigger(event);

            //then
            expect(element.find('#item1').hasClass('selected')).toBe(true);
            expect(element.find('#item2').hasClass('selected')).toBe(false);
            expect(element.find('#item3').hasClass('selected')).toBe(false);
        });

        it('should click on item on keydown enter', () => {
            //given
            const input = element.find('.input-search');
            const event = angular.element.Event('keydown');
            event.keyCode = 40;

            input.trigger(event);

            const event2 = angular.element.Event('keydown');
            event2.keyCode = 13;

            //when
            input.trigger(event2);
            scope.$digest();
            //then
            expect(scope.clickOnItem1).toHaveBeenCalled();
        });

        it('should open a href link on keydown enter', inject(($window) => {
            //given
            spyOn( $window, 'open' );

            const input = element.find('.input-search');
            const event = angular.element.Event('keydown');
            event.keyCode = 40;

            input.trigger(event);
            input.trigger(event);
            input.trigger(event);

            const event2 = angular.element.Event('keydown');
            event2.keyCode = 13;

            //when
            input.trigger(event2);

            //then
            expect($window.open).toHaveBeenCalledWith('http', '_blank');
        }));


        it('should show results on keydown enter', inject(($timeout)=> {
            //given
            spyOn( ctrl, 'showResults' );

            const input = element.find('.input-search');
            const event = angular.element.Event('keydown');
            event.keyCode = 13;

            ctrl.visible = false;

            //when
            input.trigger(event);
            $timeout.flush();

            //then
            expect(ctrl.showResults).toHaveBeenCalled();
        }));

        it('should show results on keydown arrow down', inject(($timeout)=> {
            //given
            spyOn( ctrl, 'showResults' );

            const input = element.find('.input-search');
            const event = angular.element.Event('keydown');
            event.keyCode = 40;

            ctrl.visible = false;

            //when
            input.trigger(event);
            $timeout.flush();

            //then
            expect(ctrl.showResults).toHaveBeenCalled();
        }));

    });

    describe('click', () => {
        it('should not hide on input click', () => {
            //given
            expect(element.find('.typeahead-result').hasClass('ng-hide')).toBe(false);

            //when
            element.find('input').eq(0).click();

            //then
            expect(element.find('.typeahead-result').hasClass('ng-hide')).toBe(false);
        });

        it('should hide on body click', inject(($timeout) => {
            //given
            expect(element.find('.typeahead-result').hasClass('ng-hide')).toBe(false);

            //when
            angular.element('body').click();
            $timeout.flush();

            //then
            expect(element.find('.typeahead-result').hasClass('ng-hide')).toBe(true);
        }));
    });
});