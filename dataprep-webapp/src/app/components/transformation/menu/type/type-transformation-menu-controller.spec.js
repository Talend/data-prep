/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Type transform menu controller', function () {
    'use strict';

    var createController, scope;
    var currentMetadata = {id: '719b84635c436ef245'};

    var types = [
        {'id': 'ANY', 'name': 'any', 'labelKey': 'ANY'},
        {'id': 'STRING', 'name': 'string', 'labelKey': 'STRING'},
        {'id': 'NUMERIC', 'name': 'numeric', 'labelKey': 'NUMERIC'},
        {'id': 'INTEGER', 'name': 'integer', 'labelKey': 'INTEGER'},
        {'id': 'DOUBLE', 'name': 'double', 'labelKey': 'DOUBLE'},
        {'id': 'FLOAT', 'name': 'float', 'labelKey': 'FLOAT'},
        {'id': 'BOOLEAN', 'name': 'boolean', 'labelKey': 'BOOLEAN'},
        {'id': 'DATE', 'name': 'date', 'labelKey': 'DATE'}
    ];

    beforeEach(angular.mock.module('data-prep.type-transformation-menu'));

    beforeEach(inject(function ($rootScope, $controller, $q, state, ColumnTypesService) {
        scope = $rootScope.$new();
        createController = function () {
            var ctrl = $controller('TypeTransformMenuCtrl', {
                $scope: scope
            });
            ctrl.column = {
                id: '0001',
                name: 'awesome cities',
                domain: 'CITY',
                domainLabel: 'CITY',
                domainFrequency: 18,
                type: 'string',
                semanticDomains: [
                    {id: '', label: '', frequency: 15},
                    {id: 'CITY', label: 'CITY', frequency: 18},
                    {id: 'REGION', label: 'REGION', frequency: 6},
                    {id: 'COUNTRY', label: 'COUNTRY', frequency: 17}
                ]
            };
            return ctrl;
        };

        state.playground.dataset = currentMetadata;
        spyOn(ColumnTypesService, 'getTypes').and.returnValue($q.when(types));
    }));

    it('should get column primitive types', inject(function (ColumnTypesService) {
        //given
        var expectedTypes = [
            {'id': 'STRING', 'name': 'string', 'labelKey': 'STRING'},
            {'id': 'INTEGER', 'name': 'integer', 'labelKey': 'INTEGER'},
            {'id': 'FLOAT', 'name': 'float', 'labelKey': 'FLOAT'},
            {'id': 'BOOLEAN', 'name': 'boolean', 'labelKey': 'BOOLEAN'},
            {'id': 'DATE', 'name': 'date', 'labelKey': 'DATE'}
        ];

        //when
        var ctrl = createController();
        scope.$digest();

        //then
        expect(ColumnTypesService.getTypes).toHaveBeenCalled();
        expect(ctrl.types).toEqual(expectedTypes);
    }));

    it('should change domain locally and call backend to add a step', inject(function ($q, PlaygroundService) {
        //given
        spyOn(PlaygroundService, 'appendStep').and.returnValue($q.when());
        var ctrl = createController();
        var newDomain = {
            id: 'COUNTRY',
            label: 'COUNTRY',
            frequency: 17
        };

        //when
        ctrl.changeDomain(newDomain);

        //then
        expect(ctrl.column.domain).toBe('COUNTRY');
        expect(ctrl.column.domainLabel).toBe('COUNTRY');
        expect(ctrl.column.domainFrequency).toBe(17);
        expect(ctrl.currentDomain).toBe('COUNTRY');
        expect(ctrl.currentSimplifiedDomain).toBe('COUNTRY');

        expect(PlaygroundService.appendStep).toHaveBeenCalledWith('domain_change', {
            scope: 'column',
            column_id: '0001',
            column_name: 'awesome cities',
            new_domain_id: 'COUNTRY',
            new_domain_label: 'COUNTRY',
            new_domain_frequency: 17
        });
    }));

    it('should revert domain when backend return error', inject(function ($q, PlaygroundService) {
        //given
        spyOn(PlaygroundService, 'appendStep').and.returnValue($q.reject());
        var ctrl = createController();
        var newDomain = {
            id: 'COUNTRY',
            label: 'COUNTRY',
            frequency: 17
        };

        //when
        ctrl.changeDomain(newDomain);
        scope.$digest();

        //then
        expect(ctrl.column.domain).toBe('CITY');
        expect(ctrl.column.domainLabel).toBe('CITY');
        expect(ctrl.column.domainFrequency).toBe(18);
        expect(ctrl.currentDomain).toBe('CITY');
        expect(ctrl.currentSimplifiedDomain).toBe('CITY');
    }));

    it('should change type and clear domain locally and call backend', inject(function ($q, PlaygroundService) {
        //given
        spyOn(PlaygroundService, 'appendStep').and.returnValue($q.when());
        var ctrl = createController();
        var newType = {
            id: 'integer'
        };

        //when
        ctrl.changeType(newType);

        //then
        expect(ctrl.column.type).toBe('integer');
        expect(ctrl.column.domain).toBe('');
        expect(ctrl.column.domainLabel).toBe('');
        expect(ctrl.column.domainFrequency).toBe(0);
        expect(ctrl.currentDomain).toBe('INTEGER');
        expect(ctrl.currentSimplifiedDomain).toBe('integer');
        expect(PlaygroundService.appendStep).toHaveBeenCalledWith('type_change', {
            scope: 'column',
            column_id: '0001',
            column_name: 'awesome cities',
            new_type: 'integer'
        });
    }));

    it('should revert type and domain when backend return error', inject(function ($q, PlaygroundService) {
        //given
        spyOn(PlaygroundService, 'appendStep').and.returnValue($q.reject());
        var ctrl = createController();
        var newType = {
            id: 'integer'
        };

        //when
        ctrl.changeType(newType);
        scope.$digest();

        //then
        expect(ctrl.column.type).toBe('string');
        expect(ctrl.column.domain).toBe('CITY');
        expect(ctrl.column.domainLabel).toBe('CITY');
        expect(ctrl.column.domainFrequency).toBe(18);
        expect(ctrl.currentDomain).toBe('CITY');
        expect(ctrl.currentSimplifiedDomain).toBe('CITY');
    }));

    it('should filter concrete domain and order them', function () {
        //given
        var ctrl = createController();

        //when
        ctrl.adaptDomains();

        //then
        expect(ctrl.domains).toEqual([
            {id: 'CITY', label: 'CITY', frequency: 18},
            {id: 'COUNTRY', label: 'COUNTRY', frequency: 17},
            {id: 'REGION', label: 'REGION', frequency: 6}
        ]);
        expect(ctrl.currentDomain).toBe('CITY');
        expect(ctrl.currentSimplifiedDomain).toBe('CITY');
    });

    it('should check decimal (float) type when current type is double', function () {
        //given
        var ctrl = createController();
        ctrl.currentDomain = 'double';
        var type = {'id': 'FLOAT', 'name': 'float', 'labelKey': 'FLOAT'};

        //when
        var result = ctrl.shouldBeChecked(type);

        //then
        expect(result).toBe(true);
    });

    it('should check decimal (float) type when current type is float', function () {
        //given
        var ctrl = createController();
        ctrl.currentDomain = 'float';
        var type = {'id': 'FLOAT', 'name': 'float', 'labelKey': 'FLOAT'};

        //when
        var result = ctrl.shouldBeChecked(type);

        //then
        expect(result).toBe(true);
    });

    it('should not check type when it does NOT match current domain', function () {
        //given
        var ctrl = createController();
        ctrl.currentDomain = 'beer_name'; // maybe dq library could detect beers names?
        var type = {'id': 'STRING', 'name': 'string', 'labelKey': 'STRING'};

        //when
        var result = ctrl.shouldBeChecked(type);

        //then
        expect(result).toBe(false);
    });

    it('should check integer type', function () {
        //given
        var ctrl = createController();
        ctrl.currentDomain = 'integer';
        var type = {'id': 'INTEGER', 'name': 'integer', 'labelKey': 'INTEGER'};

        //when
        var result = ctrl.shouldBeChecked(type);

        //then
        expect(result).toBe(true);
    });


    it('should call playground service to apply a force date pattern action', inject(function ($q, PlaygroundService) {
        //given
        spyOn(PlaygroundService, 'appendStep').and.returnValue($q.when());
        var ctrl = createController();
        var pattern = 'yyyy MMM dd';

        //when
        ctrl.chooseDatePattern(pattern);
        scope.$digest();

        //then
        var actionDef =
            {
                scope: 'dataset',
                column_id: ctrl.column.id,
                column_name: ctrl.column.name,
                new_pattern: pattern
            };

        expect(PlaygroundService.appendStep).toHaveBeenCalledWith('force_date_pattern', actionDef);
    }));
});