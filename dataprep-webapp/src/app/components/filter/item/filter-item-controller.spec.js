/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Filter item controller', () => {
    'use strict';

    let createController, scope;

    let filter,
        editable, onEditFn,
        removable, onRemoveFn;

    beforeEach(angular.mock.module('data-prep.filter-item'));
    beforeEach(angular.mock.module('pascalprecht.translate', $translateProvider => {
        $translateProvider.translations('en', {
            'COLON': ': '
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        filter = {
            type: 'exact',
            value: [
                {
                    value: 'lorem ipsum'
                }
            ]
        };
        editable = false;
        onEditFn = jasmine.createSpy('onEditFn');
        removable = false;
        onRemoveFn = jasmine.createSpy('onRemoveFn');

        createController = () => {
            const ctrl = $componentController('filterItem', {
                $scope: scope
            }, {
                value: filter,
                editable: editable,
                onEdit: onEditFn,
                removable: removable,
                onRemove: onRemoveFn
            });
            ctrl.$onInit();
            return ctrl;
        };
    }));

    it('should set the sign character to : in', () => {
        //given
        filter = {
            type: 'inside_range'
        };
        const ctrl = createController();

        //then
        expect(ctrl.sign).toEqual(' in ');
    });

    it('should set the sign character to : ":"', () => {
        //given
        filter ={
            type: 'valid_records'
        };
        const ctrl = createController();

        //then
        expect(ctrl.sign).toEqual(': ');
    });

    it('should set the sign character to : "≅"', () => {
        //given
        filter = {
            type: 'contains'
        };
        const ctrl = createController();

        //then
        expect(ctrl.sign).toEqual(' ≅ ');
    });

    it('should set the sign character to : "=" ', () => {
        //given
        filter ={
            type: 'exact'
        };
        const ctrl = createController();

        //then
        expect(ctrl.sign).toEqual(' = ');
    });

    it('should execute edit callback when submit is called', () => {
        //given
        const ctrl = createController();

        //when
        ctrl.submit();

        //then
        expect(onEditFn).toHaveBeenCalledWith({
            filter: filter,
            value: filter.value
        });
    });

    it('should execute edit callback when remove is called', () => {
        //given
        const ctrl = createController();

        //when
        ctrl.remove(0);

        //then
        expect(onEditFn).toHaveBeenCalledWith({
            filter: filter,
            value: []
        });
    });

    it('should execute remove callback when close is called', () => {
        //given
        const ctrl = createController();

        //when
        ctrl.close();

        //then
        expect(onRemoveFn).toHaveBeenCalledWith({
            filter: filter
        });
    });
});