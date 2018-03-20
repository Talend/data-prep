/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Filter item value controller', () => {
    'use strict';

    let createController;
    let scope;
    let filterValue;
    let editable;
    let onEditFn;
    let removable;
    let onRemoveFn;

    const originalFilterValue = 'lorem ipsum';
    const newFilterValue = 'LOREM ISPUM DOLOR';

    beforeEach(angular.mock.module('data-prep.filter-item-value'));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        filterValue = originalFilterValue;
        editable = false;
        onEditFn = jasmine.createSpy('onEditFn');
        removable = false;
        onRemoveFn = jasmine.createSpy('onRemoveFn');

        createController = () => {
            const ctrl = $componentController('filterValue', {
                $scope: scope,
            }, {
                value: filterValue,
                editable: editable,
                onEdit: onEditFn,
                removable: removable,
                onRemove: onRemoveFn,
            });
            ctrl.$onInit();
            return ctrl;
        };
    }));

    it('should reset value if ESC key is pressed', () => {
        //given
        const ctrl = createController();

        //when
        ctrl.valueToDisplay = newFilterValue;

        let escEvent = new angular.element.Event('keydown');
        escEvent.keyCode = 27;
        ctrl.onKeydown(escEvent);

        //then
        expect(onEditFn).toHaveBeenCalledWith({
            value: originalFilterValue,
        });
    });

    it('should execute edit callback if ENTER key is pressed', () => {
        //given
        const ctrl = createController();

        //when
        ctrl.valueToDisplay = newFilterValue;

        let enterEvent = new angular.element.Event('keydown');
        enterEvent.keyCode = 13;
        ctrl.onKeydown(enterEvent);

        //then
        expect(onEditFn).toHaveBeenCalledWith({
            value: newFilterValue,
        });
    });

    it('should do nothing if key is pressed and it is not ESC or ENTER keys', () => {
        //given
        const ctrl = createController();

        //when
        let aEvent = new angular.element.Event('keydown');
        aEvent.keyCode = 65;
        ctrl.onKeydown(aEvent);

        //then
        expect(onEditFn).not.toHaveBeenCalled();
    });

    it('should return value with grid constraints', () => {
        //given
        const ctrl = createController();

        //when
        ctrl.valueToDisplay = ' TEST  ';

        //then
        expect(ctrl.valueWithInvisibleChars).toBe(
            '<span class="hiddenChars"> </span>TEST<span class="hiddenChars">  </span>'
        );
    });
});
