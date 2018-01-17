/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Filter Item Component', () => {
    let createElement;
    let scope;
    let element;

    beforeEach(angular.mock.module('data-prep.filter-item'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        scope.filterItem = {
            colName: '001',
            type: 'exact',
            value: [
                {
                    value: 'lorem ipsum dolor 1',
                },
                {
                    value: 'lorem ipsum dolor 2',
                },
                {
                    value: 'lorem ipsum dolor 3',
                },
            ],
        };
        scope.editable = false;
        scope.removable = false;
        scope.onEdit = jasmine.createSpy('onEdit');
        scope.onRemove = jasmine.createSpy('onRemove');

        createElement = () => {
            element = angular.element(
                `<filter-item value="filterItem"
                              editable="editable"
                              on-edit="onEdit()"
                              removable="removable"
                              on-remove="onRemove()"></filter-item>`
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
        it('should render editable values with inputs', () => {
            //given
            scope.editable = true;
            createElement();

            //then
            expect(element.find('.filter-value input').size()).toBe(3);
        });

        it('should submit editable value while pressing enter key', () => {
            //given
            scope.editable = true;
            createElement();

            //when
            const newValue = 'LOREM IPSUM DOLOR 1';
            const enterKeyEvent = angular.element.Event('keydown');
            enterKeyEvent.keyCode = 13;
            element.find('input').eq(1)
                .val(newValue)
                .trigger(enterKeyEvent);

            //then
            const inputValue = element.find('.filter-value input').eq(1).val();
            expect(inputValue).toBe(newValue);
        });

        it('should call onEdit when a value has changed', () => {
            //given
            scope.editable = true;
            createElement();

            //when
            const newValue = 'LOREM IPSUM DOLOR 1';
            const enterKeyEvent = angular.element.Event('keydown');
            enterKeyEvent.keyCode = 13;
            element.find('input').eq(1)
                .val(newValue)
                .trigger(enterKeyEvent);

            //then
            expect(scope.onEdit).toHaveBeenCalled();
        });

        it('should render non-editable values with spans', () => {
            //given
            createElement();

            //then
            expect(element.find('.filter-value span').size()).toBe(3);
        });
    });

    describe('removable option', () => {
        it('should render badge close button', () => {
            // given
            scope.removable = true;
            createElement();

            //then
            expect(element.find('a.badge-btn-close').size()).toBe(1);
        });

        it('should call onClose when badge close button is clicked', () => {
            // given
            scope.removable = true;
            createElement();

            //when
            const $removeBtn = element.find('a.badge-btn-close').eq(0);
            $removeBtn.click();

            //then
            expect(scope.onRemove).toHaveBeenCalled();
        });

        it('should not render badge close button', () => {
            // given
            createElement();

            //then
            expect(element.find('a.badge-btn-close').size()).toBe(0);
        });
    });
});
