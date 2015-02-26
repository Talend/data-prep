/*jshint camelcase: false */

describe('Dataset transform menu controller', function () {
    'use strict';

    var createController, scope;
    var result = {
        'records': [{
            'firstname': 'Grover',
            'avgAmount': '82.4',
            'city': 'BOSTON',
            'birth': '01-09-1973',
            'registration': '17-02-2008',
            'id': '1',
            'state': 'AR',
            'nbCommands': '41',
            'lastname': 'Quincy'
        }, {
            'firstname': 'Warren',
            'avgAmount': '87.6',
            'city': 'NASHVILLE',
            'birth': '11-02-1960',
            'registration': '18-08-2007',
            'id': '2',
            'state': 'WA',
            'nbCommands': '17',
            'lastname': 'Johnson'
        }]
    };

    var metadata = {
        'id': '44f5e4ef-96e9-4041-b86a-0bee3d50b18b',
        'name': 'customers_jso_light',
        'author': 'anonymousUser',
        'records': 15,
        'nbLinesHeader': 1,
        'nbLinesFooter': 0,
        'created': '02-16-2015 08:52'
    };
    var column = {
        'id': 'MostPopulousCity',
        'quality': {
            'empty': 5,
            'invalid': 10,
            'valid': 72
        },
        'type': 'string'
    };
    var columnIndex = 2;

    beforeEach(module('data-prep-dataset'));

    beforeEach(inject(function ($rootScope, $controller, $q, TransformationService, DatasetGridService) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrl = $controller('DatasetTransformMenuCtrl', {
                $scope: scope
            });
            ctrl.metadata = metadata;
            ctrl.column = column;
            ctrl.columnIndex = columnIndex;
            return ctrl;
        };

        DatasetGridService.setDataset({}, {});

        spyOn(TransformationService, 'transform').and.returnValue($q.when({data: result}));
        spyOn($rootScope, '$emit').and.callThrough();
        spyOn(DatasetGridService, 'updateRecords').and.callThrough();
    }));

    it('should do nothing on select if the menu is a divider', inject(function (TransformationService) {
        //given
        var ctrl = createController();
        ctrl.menu = {isDivider: true};

        //when
        ctrl.select();

        //then
        expect(ctrl.showModal).toBeFalsy();
        expect(TransformationService.transform).not.toHaveBeenCalled();
    }));

    it('should only open modal on select if item has parameters', inject(function (TransformationService) {
        //given
        var ctrl = createController();
        ctrl.menu = {parameters: [{name: 'param1', type: 'text', default: '.'}]};

        //when
        ctrl.select();

        //then
        expect(ctrl.showModal).toBeTruthy();
        expect(TransformationService.transform).not.toHaveBeenCalled();
    }));

    it('should only open modal on select if item has choice', inject(function (TransformationService) {
        //given
        var ctrl = createController();
        ctrl.menu = {items: {name: 'choice', values: [{name: 'choice1'}, {name: 'choice2'}]}};

        //when
        ctrl.select();

        //then
        expect(ctrl.showModal).toBeTruthy();
        expect(TransformationService.transform).not.toHaveBeenCalled();
    }));

    it('should call transform on simple menu select', inject(function ($rootScope, TransformationService, DatasetGridService) {
        //given
        var ctrl = createController();
        ctrl.menu = {name: 'uppercase', category: 'case'};

        //when
        ctrl.select();
        expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.datagrid.start', {col: columnIndex});
        $rootScope.$digest();

        //then
        expect(ctrl.showModal).toBeFalsy();
        expect(TransformationService.transform).toHaveBeenCalledWith('44f5e4ef-96e9-4041-b86a-0bee3d50b18b', 'uppercase', { column_name: 'MostPopulousCity' });
        expect(DatasetGridService.updateRecords).toHaveBeenCalledWith(result.records);
        expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.datagrid.stop', {col: columnIndex});
    }));

    it('should call transform on parameterized menu select', inject(function ($rootScope, TransformationService, DatasetGridService) {
        //given
        var ctrl = createController();
        ctrl.menu = {
            name: 'uppercase',
            category: 'case',
            parameters: [
                {name: 'param1', type: 'text', default: '', value: 'param1Value'},
                {name: 'param2', type: 'int', default: '5', value: 4}
            ]
        };

        //when
        ctrl.transformWithParam();
        expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.datagrid.start', {col: columnIndex});
        $rootScope.$digest();

        //then
        expect(ctrl.showModal).toBeFalsy();
        expect(TransformationService.transform).toHaveBeenCalledWith(
            '44f5e4ef-96e9-4041-b86a-0bee3d50b18b',
            'uppercase',
            { column_name: 'MostPopulousCity', param1: 'param1Value', param2: 4 });
        expect(DatasetGridService.updateRecords).toHaveBeenCalledWith(result.records);
        expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.datagrid.stop', {col: columnIndex});
    }));

    it('should call transform on simple choice param menu select', inject(function ($rootScope, TransformationService, DatasetGridService) {
        //given
        var ctrl = createController();
        ctrl.menu = {
            name: 'split',
            category: 'split',
            items: [{
                name: 'mode',
                values: [
                    {name: 'regex'},
                    {name: 'index'}
                ]
            }]
        };
        ctrl.menu.items[0].selectedValue = ctrl.menu.items[0].values[1];

        //when
        ctrl.transformWithParam();
        expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.datagrid.start', {col: columnIndex});
        $rootScope.$digest();

        //then
        expect(ctrl.showModal).toBeFalsy();
        expect(TransformationService.transform).toHaveBeenCalledWith(
            '44f5e4ef-96e9-4041-b86a-0bee3d50b18b',
            'split',
            { column_name: 'MostPopulousCity', mode: 'index'});
        expect(DatasetGridService.updateRecords).toHaveBeenCalledWith(result.records);
        expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.datagrid.stop', {col: columnIndex});
    }));

    it('should call transform on parameterized choice menu select', inject(function ($rootScope, TransformationService, DatasetGridService) {
        //given
        var ctrl = createController();
        ctrl.menu = {
            name: 'split',
            category: 'split',
            items: [{
                name: 'mode',
                values: [
                    {
                        name: 'regex',
                        parameters : [
                            {name: 'regex', type: 'text', default: '', value: 'param1Value'},
                            {name: 'comment', type: 'text', default: '', value: 'my comment'}
                        ]
                    },
                    {name: 'index'}
                ]
            }]
        };
        ctrl.menu.items[0].selectedValue = ctrl.menu.items[0].values[0];

        //when
        ctrl.transformWithParam();
        expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.datagrid.start', {col: columnIndex});
        $rootScope.$digest();

        //then
        expect(ctrl.showModal).toBeFalsy();
        expect(TransformationService.transform).toHaveBeenCalledWith(
            '44f5e4ef-96e9-4041-b86a-0bee3d50b18b',
            'split',
            { column_name: 'MostPopulousCity', mode: 'regex', regex: 'param1Value', comment: 'my comment'});
        expect(DatasetGridService.updateRecords).toHaveBeenCalledWith(result.records);
        expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.datagrid.stop', {col: columnIndex});
    }));

    it('should call transform with parameters and parameterized choice menu select', inject(function ($rootScope, TransformationService, DatasetGridService) {
        //given
        var ctrl = createController();
        ctrl.menu = {
            name: 'split',
            category: 'split',
            parameters: [
                {name: 'param1', type: 'text', default: '', value: 'param1Value'},
                {name: 'param2', type: 'int', default: '5', value: 4}
            ],
            items: [{
                name: 'mode',
                values: [
                    {
                        name: 'regex',
                        parameters : [
                            {name: 'regex', type: 'text', default: '', value: 'param1Value'},
                            {name: 'comment', type: 'text', default: '', value: 'my comment'}
                        ]
                    },
                    {name: 'index'}
                ]
            }]
        };
        ctrl.menu.items[0].selectedValue = ctrl.menu.items[0].values[0];

        //when
        ctrl.transformWithParam();
        expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.datagrid.start', {col: columnIndex});
        $rootScope.$digest();

        //then
        expect(ctrl.showModal).toBeFalsy();
        expect(TransformationService.transform).toHaveBeenCalledWith(
            '44f5e4ef-96e9-4041-b86a-0bee3d50b18b',
            'split',
            { column_name: 'MostPopulousCity', mode: 'regex', regex: 'param1Value', comment: 'my comment', param1: 'param1Value', param2: 4});
        expect(DatasetGridService.updateRecords).toHaveBeenCalledWith(result.records);
        expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.datagrid.stop', {col: columnIndex});
    }));
});