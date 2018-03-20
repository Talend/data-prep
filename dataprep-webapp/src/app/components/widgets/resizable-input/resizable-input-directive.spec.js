/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Resizable input directive', () => {
    'use strict';

    let scope;
    let createElement;
    let element;

    beforeEach(angular.mock.module('talend.widget'));

    const defaultInput = {
        content: 'Lorem ipsum',
        width: (11 * 0.5) + 'em',
    };
    const newInput = {
        content: 'Lorem ipsum dolor sit amet',
        width: (26 * 0.5) + 'em',
    };

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        scope.ngModel = defaultInput.content;
        scope.offset = 0;
        createElement = () => {
            element = angular.element(`<input ng-model="ngModel"
                                              resizable-input
                                              resizable-input-offset="offset" >`);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render input', () => {
        //when
        createElement();

        //then
        expect(element.length).toBe(1);
        expect(element.css('width')).toBe(defaultInput.width);
    });

    it('should adjust input size on ngModel change', () => {
        //given
        createElement();
        expect(element.css('width')).toBe(defaultInput.width);

        //when
        scope.ngModel = newInput.content;
        scope.$digest();

        //then
        expect(element.css('width')).toBe(newInput.width);
    });
});
