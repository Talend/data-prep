/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Talend Badge Component', () => {
    'use strict';

    let scope;
    let createElement;
    let element;

    beforeEach(angular.mock.module('talend.widget'));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        scope.onRemove = jasmine.createSpy('onRemove');
        scope.removable = false;

        createElement = () => {
            const template = `<talend-badge removable="removable" 
                                            on-remove="onRemove()" ></talend-badge>`;
            element = $compile(template)(scope);
            scope.$digest();
        };
    }));

    describe('not removable', () => {
        it('should not render a close button', () => {
            //given
            createElement();

            //then
            expect(element.find('.badge-btn-close').length).toBe(0);
        });
    });

    describe('removable', () => {
        beforeEach(() => {
            scope.removable = true;
        });

        it('should render a close button', () => {
            //given
            createElement();

            //then
            expect(element.find('.badge-btn-close').length).toBe(1);
        });

        it('should call onRemove method when badge close button is clicked', () => {
            //given
            createElement();

            //when
            element.find('.badge-btn-close').eq(0).click();

            //then
            expect(scope.onRemove).toHaveBeenCalled();
        });
    });
});
