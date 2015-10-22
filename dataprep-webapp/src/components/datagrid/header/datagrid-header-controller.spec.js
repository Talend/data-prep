describe('Datagrid header controller', function () {
    'use strict';

    var createController, scope;
    var column = {
        id: '0001',
        name: 'Original name',
        type: 'string'
    };

    var menusMock = function () {
        return [
            {
                'name': 'uppercase',
                'category': 'case',
                'items': null,
                'parameters': null
            },
            {
                'name': 'rename',
                'category': 'column_metadata',
                'items': null,
                'parameters': null
            },
            {
                'name': 'lowercase',
                'category': 'case',
                'items': null,
                'parameters': null
            },
            {
                'name': 'withParam',
                'category': 'case',
                'items': null,
                'parameters': [
                    {
                        'name': 'param',
                        'type': 'string',
                        'default': '.',
                        'inputType': 'text'
                    }
                ]
            },
            {
                'name': 'split',
                'category': 'column_metadata',
                'parameters': null,
                'items': [
                    {
                        'name': 'mode',
                        'values': [
                            {
                                'name': 'noparam'
                            },
                            {
                                'name': 'regex',
                                'parameters': [
                                    {
                                        'name': 'regexp',
                                        'type': 'string',
                                        'default': '.',
                                        'inputType': 'text'
                                    }
                                ]
                            },
                            {
                                'name': 'index',
                                'parameters': [
                                    {
                                        'name': 'index',
                                        'type': 'integer',
                                        'default': '5',
                                        'inputType': 'number'
                                    }
                                ]
                            },
                            {
                                'name': 'threeParams',
                                'parameters': [
                                    {
                                        'name': 'index',
                                        'type': 'numeric',
                                        'default': '5',
                                        'inputType': 'number'
                                    },
                                    {
                                        'name': 'index2',
                                        'type': 'float',
                                        'default': '5',
                                        'inputType': 'number'
                                    },
                                    {
                                        'name': 'index3',
                                        'type': 'double',
                                        'default': '5',
                                        'inputType': 'number'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];
    };

    beforeEach(module('data-prep.datagrid-header'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrlFn = $controller('DatagridHeaderCtrl', {
                $scope: scope
            }, true);

            ctrlFn.instance.column = column;
            return ctrlFn();
        };
    }));

    describe('with transformation list success', function () {
        beforeEach(inject(function ($q, TransformationCacheService) {
            spyOn(TransformationCacheService, 'getTransformations').and.returnValue($q.when(menusMock()));
        }));

        it('should filter and init only "column_metadata" category', inject(function ($rootScope, TransformationCacheService) {
            //given
            var ctrl = createController();

            //when
            ctrl.initTransformations();
            $rootScope.$digest();

            //then
            expect(TransformationCacheService.getTransformations).toHaveBeenCalledWith(column, true);
            expect(ctrl.transformations.length).toBe(2);
            expect(ctrl.transformations[0].name).toBe('rename');
            expect(ctrl.transformations[1].name).toBe('split');
        }));

        it('should not get transformations if transformations are already initiated', inject(function ($rootScope, TransformationCacheService) {
            //given
            var ctrl = createController();
            ctrl.initTransformations();
            $rootScope.$digest();

            //when
            ctrl.initTransformations();
            $rootScope.$digest();

            //then
            expect(TransformationCacheService.getTransformations.calls.count()).toBe(1);
        }));
    });

    describe('with transformation list error', function () {
        beforeEach(inject(function ($q, TransformationCacheService) {
            spyOn(TransformationCacheService, 'getTransformations').and.returnValue($q.reject('server error'));
        }));

        it('should change inProgress and error flags', inject(function ($rootScope) {
            //given
            var ctrl = createController();
            expect(ctrl.transformationsRetrieveError).toBeFalsy();
            expect(ctrl.initTransformationsInProgress).toBeFalsy();

            ctrl.transformationsRetrieveError = true;

            //when
            ctrl.initTransformations();
            expect(ctrl.initTransformationsInProgress).toBeTruthy();
            expect(ctrl.transformationsRetrieveError).toBeFalsy();
            $rootScope.$digest();

            //then
            expect(ctrl.transformationsRetrieveError).toBeTruthy();
            expect(ctrl.initTransformationsInProgress).toBeFalsy();
        }));
    });

    describe('update column name', function () {

        beforeEach(inject(function ($q, PlaygroundService) {
            spyOn(PlaygroundService, 'appendStep').and.returnValue($q.when(true));
        }));

        it('should update column name', inject(function (PlaygroundService) {
            //given
            var ctrl = createController();
            ctrl.newName = 'new name';

            //when
            ctrl.updateColumnName();

            //then
            expect(PlaygroundService.appendStep).toHaveBeenCalledWith('rename_column', {
                /*jshint camelcase: false */
                new_column_name: 'new name',
                scope: 'column',
                column_id: '0001',
                column_name: 'Original name'
            });
        }));

        it('should turn off edition mode after name update', function () {
            //given
            var ctrl = createController();
            ctrl.newName = 'new name';
            ctrl.isEditMode = true;

            //when
            ctrl.updateColumnName();
            scope.$apply();

            //then
            expect(ctrl.isEditMode).toBe(false);
        });

        it('should return true when name has changed and is not empty', function () {
            //given
            var ctrl = createController();
            ctrl.newName = 'new name';

            //when
            var hasChanged = ctrl.nameHasChanged();

            //then
            expect(hasChanged).toBe(true);
        });

        it('should return false when name is unchanged', function () {
            //given
            var ctrl = createController();
            ctrl.setEditMode(true);
            ctrl.newName = 'Original name';

            //when
            var hasChanged = ctrl.nameHasChanged();

            //then
            expect(hasChanged).toBeFalsy();
        });

        it('should return false when name is falsy', function () {
            //given
            var ctrl = createController();
            ctrl.setEditMode(true);
            ctrl.newName = '';

            //when
            var hasChanged = ctrl.nameHasChanged();

            //then
            expect(hasChanged).toBeFalsy();
        });

        it('should update edition mode to true', function () {
            //given
            var ctrl = createController();
            expect(ctrl.isEditMode).toBeFalsy();

            //when
            ctrl.setEditMode(true);

            //then
            expect(ctrl.isEditMode).toBeTruthy();
        });

        it('should update edition mode to false', function () {
            //given
            var ctrl = createController();
            ctrl.isEditMode = true;

            //when
            ctrl.setEditMode(false);

            //then
            expect(ctrl.isEditMode).toBe(false);
        });

        it('should reset name when edition mode is set to true', function () {
            //given
            var ctrl = createController();
            ctrl.newName = 'new name';

            //when
            ctrl.setEditMode(true);

            //then
            expect(ctrl.newName).toBe('Original name');
        });
    });

});
