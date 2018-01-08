/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Filter Item Value Component', () => {
    beforeEach(angular.mock.module('data-prep.filter-item-value'));

    let createElement;
    let scope;
    let element;

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        scope.filterValue = 'lorem ipsum dolor';
        scope.editable = false;
        scope.onEdit = jasmine.createSpy('onEdit');
        scope.removable = false;
        scope.onRemove = jasmine.createSpy('onRemove');

        createElement = () => {
            element = angular.element(
                `<filter-value value="filterValue"
                               editable="editable"
                               on-edit="onEdit()"
                               removable="removable"
                               on-remove="onRemove()"></filter-value>`
            );

            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('editable option', () => {
        it('should render editable value with input', () => {
            //given
            scope.editable = true;
            createElement();

            //then
            expect(element.find('input').size()).toBe(1);
            expect(element.find('span').size()).toBe(0);
        });

        it('should render non-editable value with span', () => {
            //given
            createElement();

            //then
            expect(element.find('span').size()).toBe(1);
            expect(element.find('input').size()).toBe(0);
        });

        it('should render empty value with span with specific class', () => {
            //given
            scope.filterValue = 'empty records';

            //when
            createElement();

            //then
            expect(element.find('span').size()).toBe(1);
            expect(element.find('span').eq(0).hasClass('empty')).toBeTruthy();
        });
    });

    describe('removable option', () => {
        it('should render remove button', () => {
            //given
            scope.removable = true;
            createElement();

            //then
            expect(element.find('a.filter-value-btn-remove').size()).toBe(1);
            expect(element.find('a.filter-value-btn-remove').eq(0).hasClass('ng-hide')).toBeFalsy();
        });

        it('should render hidden remove button', () => {
            //given
            createElement();

            //then
            expect(element.find('a.filter-value-btn-remove').size()).toBe(1);
            expect(element.find('a.filter-value-btn-remove').eq(0).hasClass('ng-hide')).toBeTruthy();
        });

        it('should render remove button when removable option has changed', () => {
            //given
            createElement();
            expect(element.find('a.filter-value-btn-remove').size()).toBe(1);
            expect(element.find('a.filter-value-btn-remove').eq(0).hasClass('ng-hide')).toBeTruthy();

            //when
            scope.removable = true;
            scope.$digest();

            //then
            expect(element.find('a.filter-value-btn-remove').size()).toBe(1);
            expect(element.find('a.filter-value-btn-remove').eq(0).hasClass('ng-hide')).toBeFalsy();
        });

        it('should call onRemove callback when button is clicked', () => {
            //given
            scope.removable = true;
            createElement();

            //when
            const $removeBtn = element.find('a.filter-value-btn-remove').eq(0);
            $removeBtn.click();

            //then
            expect(scope.onRemove).toHaveBeenCalled();
        });
    });
});
