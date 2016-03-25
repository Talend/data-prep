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

        var html = `
            <typeahead search="search"
                       placeholder="Type here"
                       searching-text="Searching ..."
                       custom-render="true">
                <div id="inventory"></div>
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

            expect(element.find('.typeahead-result').length).toBe(1);

            //when
            input.trigger(event);

            //then
            expect(element.find('.typeahead-result').length).toBe(0);
        });

        it('should NOT hide results on non ESC', () => {
            //given
            const input = element.find('.input-search');
            const event = angular.element.Event('keydown');
            event.keyCode = 13;

            expect(element.find('.typeahead-result').length).toBe(1);

            //when
            input.trigger(event);

            //then
            expect(element.find('.typeahead-result').length).toBe(1);
        });
    });

    describe('click', () => {
        it('should not hide on input click', () => {
            //given
            expect(element.find('.typeahead-result').length).toBe(1);

            //when
            element.find('input').eq(0).click();

            //then
            expect(element.find('.typeahead-result').length).toBe(1);
        });

        it('should hide on body click', inject(($timeout) => {
            //given
            expect(element.find('.typeahead-result').length).toBe(1);

            //when
            angular.element('body').click();
            $timeout.flush();

            //then
            expect(element.find('.typeahead-result').length).toBe(0);
        }));
    });
});