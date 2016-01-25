describe('Lookup controller', function () {
    'use strict';

    var createController, scope, stateMock;
    var dsActions = [
        {
            'category': 'data_blending',
            'name': 'lookup',
            'parameters': [
                {
                    'name': 'column_id',
                    'type': 'string',
                    'default': ''
                },
                {
                    'name': 'filter',
                    'type': 'filter',
                    'default': ''
                },
                {
                    'name': 'lookup_ds_name',
                    'type': 'string',
                    'default': 'lookup_2'
                },
                {
                    'name': 'lookup_ds_id',
                    'type': 'string',
                    'default': '9e739b88-5ec9-4b58-84b5-2127a7e2eac7'
                },
                {
                    'name': 'lookup_ds_url',
                    'type': 'string',
                    'default': 'http://172.17.0.211:8080/datasets/9ee2eac7/content?metadata=true'
                },
                {
                    'name': 'lookup_join_on',
                    'type': 'string',
                    'default': ''
                },
                {
                    'name': 'lookup_join_on_name',
                    'type': 'string',
                    'default': ''
                },
                {
                    'name': 'lookup_selected_cols',
                    'type': 'list',
                    'default': ''
                }
            ]
        }
    ];

    var params = {
        'column_id': 'mainGridColId',
        'column_name': 'mainGridColName',
        'filter': '',
        'lookup_ds_name': 'lookup_2',
        'lookup_ds_id': '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
        'lookup_ds_url': 'http://172.17.0.211:8080/datasets/9ee2eac7/content?metadata=true',
        'lookup_join_on': 'lookupGridColId',
        'lookup_join_on_name': 'lookupGridColName',
        'lookup_selected_cols': ['0002', '0003']
    };

    /*jshint camelcase: false */
    var step = {
        column: {
            id: '0000',
            name: 'id'
        },
        row: {
            id: '11'
        },
        transformation: {
            stepId: '72fe267d489b06890da69368f4760530b076ec59',
            name: 'lookup',
            label: 'Lookup',
            description: 'Blends columns from another dataset into this one',
            parameters: [],
            dynamic: false
        },
        actionParameters: {
            action: 'lookup',
            parameters: {
                column_id: '0000',
                filter: '',
                lookup_ds_id: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
                lookup_join_on: '0000',
                scope: 'dataset',
                column_name: 'id',
                lookup_selected_cols: [
                    {
                        id: '0001',
                        name: 'uglystate',
                    }
                ],
                row_id: '11',
                lookup_ds_url: 'dsLookupUrl',
                lookup_join_on_name: 'id',
                lookup_ds_name: 'cluster_dataset'
            }
        },
        diff: {
            createdColumns: [
                '0009'
            ]
        },
        filters: (void 0)
    };

    beforeEach(module('data-prep.lookup', function ($provide) {
        stateMock = {
            playground: {
                preparation: {
                    id: '132da49ef87694ab64e6'
                },
                grid: {
                    selectedColumn: {
                        id: 'mainGridColId',
                        name: 'mainGridColName'
                    }
                },
                lookup: {
                    selectedColumn: {
                        id: 'lookupGridColId',
                        name: 'lookupGridColName'
                    },
                    columnsToAdd: ['0002', '0003'],
                    datasets: dsActions,
                    dataset: dsActions[0]
                }
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($q, $rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('LookupCtrl', {
                $scope: scope
            });
        };
    }));

    beforeEach(function () {
        jasmine.clock().install();
    });

    afterEach(function () {
        jasmine.clock().uninstall();
    });

    describe('preview', function () {
        it('should trigger lookup preview', inject(function (EarlyPreviewService) {
            //given
            var ctrl = createController();
            var previewClosure = jasmine.createSpy('preview');
            spyOn(EarlyPreviewService, 'earlyPreview').and.returnValue(previewClosure);

            //when
            ctrl.hoverSubmitBtn();

            //then
            expect(EarlyPreviewService.earlyPreview).toHaveBeenCalledWith(stateMock.playground.lookup.dataset, 'dataset');
            expect(previewClosure).toHaveBeenCalledWith(params);
        }));

        it('should trigger lookup preview update', inject(function (PlaygroundService) {
            //given
            var ctrl = createController();
            spyOn(PlaygroundService, 'updatePreview').and.returnValue();
            stateMock.playground.lookup.step = step;
            //when
            ctrl.hoverSubmitBtn();

            //then
            expect(PlaygroundService.updatePreview).toHaveBeenCalledWith(step, params);
        }));
    });

    describe('validation', function () {
        beforeEach(inject(function ($q, TransformationApplicationService, PlaygroundService, EarlyPreviewService) {
            spyOn(TransformationApplicationService, 'append').and.returnValue($q.when(true));
            spyOn(PlaygroundService, 'updateStep').and.returnValue($q.when(true));
            spyOn(EarlyPreviewService, 'activatePreview').and.returnValue();
            spyOn(EarlyPreviewService, 'deactivatePreview').and.returnValue();
            spyOn(EarlyPreviewService, 'cancelPendingPreview').and.returnValue();
        }));

        it('should cancel and deactivate preview', inject(function (EarlyPreviewService) {
            //given
            var ctrl = createController();

            //when
            ctrl.submit();

            //then
            expect(EarlyPreviewService.deactivatePreview).toHaveBeenCalled();
            expect(EarlyPreviewService.cancelPendingPreview).toHaveBeenCalled();
        }));

        it('should reactivate preview after the operation with a delay of 500ms', inject(function ($q, TransformationApplicationService, EarlyPreviewService) {
            //given
            var ctrl = createController();

            //when
            ctrl.submit();
            expect(EarlyPreviewService.activatePreview).not.toHaveBeenCalled();
            scope.$digest();
            jasmine.clock().tick(500);

            //then
            expect(EarlyPreviewService.activatePreview).toHaveBeenCalled();
        }));

        it('should add new lookup action', inject(function ($q, TransformationApplicationService) {
            //given
            var ctrl = createController();

            //when
            ctrl.submit();

            //then
            expect(TransformationApplicationService.append).toHaveBeenCalledWith(stateMock.playground.lookup.dataset, 'dataset', params);
        }));

        it('should update lookup action', inject(function (PlaygroundService) {
            //given
            var ctrl = createController();
            stateMock.playground.lookup.step = step;

            //when
            ctrl.submit();

            //then
            expect(PlaygroundService.updateStep).toHaveBeenCalledWith(step, params);
        }));
    });

    describe('utils', function () {
        it('should return the action name', inject(function () {
            //given
            var ctrl = createController();

            //when
            var label = ctrl.getDsName(dsActions[0]);
            //then
            expect(label).toBe('lookup_2');
        }));
    });
});