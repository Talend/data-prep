/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Actions list controller', () => {
    'use strict';

    let createController;
    let scope;
    let stateMock;

    beforeEach(angular.mock.module('data-prep.actions-list', ($provide) => {
        stateMock = { playground: {
                grid: {},
                filter: {},
            }, };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $controller) => {
        scope = $rootScope.$new();

        createController = () => {
            return $controller('ActionsListCtrl', {
                $scope: scope,
            });
        };
    }));

    beforeEach(inject(($q, PlaygroundService, TransformationService, EarlyPreviewService, StateService) => {
        spyOn(PlaygroundService, 'completeParamsAndAppend').and.returnValue($q.when());
        spyOn(TransformationService, 'initDynamicParameters').and.returnValue($q.when());
        spyOn(StateService, 'setPreviewDisabled');
        spyOn(EarlyPreviewService, 'cancelPendingPreview').and.returnValue();
        spyOn(EarlyPreviewService, 'earlyPreview').and.returnValue();
    }));

    describe('init', () => {
        it('should init const s and flags', inject(() => {
            //when
            const ctrl = createController();

            //then
            expect(ctrl.dynamicTransformation).toBe(null);
            expect(ctrl.showModalContent).toBe(null);
            expect(ctrl.dynamicFetchInProgress).toBe(false);
            expect(ctrl.showDynamicModal).toBe(false);
        }));
    });

    describe('early preview', () => {
        it('should trigger early preview with current scope', inject((EarlyPreviewService) => {
            //given
            const transformation = { name: 'delete' };
            const ctrl = createController();
            ctrl.scope = 'column';

            //when
            ctrl.earlyPreview(transformation);

            //then
            expect(EarlyPreviewService.earlyPreview).toHaveBeenCalledWith(transformation, 'column');
        }));
    });

    describe('transform', () => {
        it('should call appendStep function on transform closure execution', inject((PlaygroundService) => {
            //given
            const transformation = { name: 'tolowercase' };
            const params = { param: 'value' };
            const ctrl = createController();
            ctrl.scope = 'column';

            //when
            const closure = ctrl.transform(transformation);
            closure(params);

            //then
            expect(PlaygroundService.completeParamsAndAppend).toHaveBeenCalledWith(transformation, 'column', params);
        }));

        it('should hide modal after step append', () => {
            //given
            const transformation = { name: 'tolowercase' };
            const params = { param: 'value' };
            const ctrl = createController();
            ctrl.scope = 'column';
            ctrl.showDynamicModal = true;

            //when
            const closure = ctrl.transform(transformation);
            closure(params);
            scope.$digest();

            //then
            expect(ctrl.showDynamicModal).toBe(false);
        });

        it('should append new step on static transformation selection', inject((PlaygroundService) => {
            //given
            const transformation = { name: 'tolowercase' };
            const ctrl = createController();
            ctrl.scope = 'column';

            //when
            ctrl.select(transformation);

            //then
            expect(PlaygroundService.completeParamsAndAppend).toHaveBeenCalledWith(transformation, 'column', undefined);
        }));

        it('should cancel pending preview and disable it', inject((EarlyPreviewService, StateService) => {
            //given
            const transformation = { name: 'tolowercase' };
            const params = { param: 'value' };
            const ctrl = createController();
            ctrl.scope = 'column';
            ctrl.showDynamicModal = true;

            //when
            const closure = ctrl.transform(transformation);
            closure(params);

            //then
            expect(StateService.setPreviewDisabled).toHaveBeenCalledWith(true);
            expect(EarlyPreviewService.cancelPendingPreview).toHaveBeenCalled();
        }));

        it('should re-enable early preview after 500ms', inject( ($timeout, StateService) => {
            //given
            const transformation = { name: 'tolowercase' };
            const params = { param: 'value' };
            const ctrl = createController();
            ctrl.scope = 'column';
            ctrl.showDynamicModal = true;

            //when
            const closure = ctrl.transform(transformation);
            closure(params);
            scope.$digest();

            expect(StateService.setPreviewDisabled).toHaveBeenCalledWith(true);
            $timeout.flush(500);

            //then
            expect(StateService.setPreviewDisabled).toHaveBeenCalledWith(false);
        }));
        it('should update transformationInProgress', inject(($timeout) => {
            //given
            const transformation = { name: 'tolowercase' };
            const params = { param: 'value' };
            const ctrl = createController();
            ctrl.scope = 'column';
            ctrl.showDynamicModal = true;

            //when
            const closure = ctrl.transform(transformation);
            closure(params);
            scope.$digest();

            expect(ctrl.transformationInProgress).toEqual(true);
            $timeout.flush(500);

            //then
            expect(ctrl.transformationInProgress).toEqual(false);
        }));
    });

    describe('dynamic parameters',() => {
        beforeEach(() => {
            stateMock.playground.grid.selectedColumns = [{ id: '0001' }];
        });

        it('should set current dynamic transformation on dynamic transformation selection', () => {
            //given
            stateMock.playground.dataset = { id: '41fa397a8239cd051b35' };

            const transformation = { name: 'cluster', dynamic: true };
            const ctrl = createController();
            ctrl.dynamicTransformation = null;

            //when
            ctrl.select(transformation);

            //then
            expect(ctrl.dynamicTransformation).toBe(transformation);
        });

        it('should init dynamic params on dynamic transformation selection for current dataset', inject((TransformationService) => {
            //given
            stateMock.playground.dataset = { id: '41fa397a8239cd051b35' };
            stateMock.playground.preparation = null;

            const transformation = { name: 'cluster', dynamic: true };

            const ctrl = createController();

            //when
            ctrl.select(transformation);

            //then
            expect(TransformationService.initDynamicParameters).toHaveBeenCalledWith(transformation, {
                columnId: '0001',
                datasetId: '41fa397a8239cd051b35',
                preparationId: null,
            });
        }));

        it('should init dynamic params on dynamic transformation selection for current preparation', inject((TransformationService) => {
            //given
            stateMock.playground.dataset = { id: '41fa397a8239cd051b35' };
            stateMock.playground.preparation = { id: '35da66fc454568f4a52' };

            const transformation = { name: 'cluster', dynamic: true };
            const ctrl = createController();

            //when
            ctrl.select(transformation);

            //then
            expect(TransformationService.initDynamicParameters).toHaveBeenCalledWith(transformation, {
                columnId: '0001',
                datasetId: '41fa397a8239cd051b35',
                preparationId: '35da66fc454568f4a52',
            });
        }));

        it('should update fetch progress flag during dynamic parameters init', inject(($rootScope) => {
            //given
            stateMock.playground.dataset = { id: '41fa397a8239cd051b35' };
            const transformation = { name: 'cluster', dynamic: true };

            const ctrl = createController();
            ctrl.dynamicFetchInProgress = false;

            //when
            ctrl.select(transformation);
            expect(ctrl.dynamicFetchInProgress).toBe(true);
            $rootScope.$digest();

            //then
            expect(ctrl.dynamicFetchInProgress).toBe(false);
        }));

        it('should show NO CLUSTERS WERE FOUND message', inject(($rootScope) => {
            //given
            stateMock.playground.dataset = { id: '41fa397a8239cd051b35' };
            const dynamicTransformation = { name: 'cluster', dynamic: true, cluster: { clusters: [] } };
            const ctrl = createController();

            //when
            ctrl.select(dynamicTransformation);
            $rootScope.$digest();

            //then
            expect(ctrl.showModalContent).toBe(false);
            expect(ctrl.emptyParamsMsg).toEqual('NO_CLUSTERS_ACTION_MSG');
        }));

        it('should show NO PARAMETERS WERE FOUND message', inject(($rootScope) => {
            //given
            stateMock.playground.dataset = { id: '41fa397a8239cd051b35' };
            const dynamicTransformation = { name: 'choices', dynamic: true, parameters: [] };
            const ctrl = createController();

            //when
            ctrl.select(dynamicTransformation);
            $rootScope.$digest();

            //then
            expect(ctrl.showModalContent).toBe(false);
            expect(ctrl.emptyParamsMsg).toEqual('NO_PARAMETERS_ACTION_MSG');
        }));

        it('should show dynamic cluster transformation in a modal', inject(($rootScope) => {
            //given
            stateMock.playground.dataset = { id: '41fa397a8239cd051b35' };
            const dynamicTransformation = {
                name: 'cluster',
                dynamic: true,
                cluster: { clusters: [{ parameters: [], replace: {} }] },
            };
            const ctrl = createController();

            //when
            ctrl.select(dynamicTransformation);
            $rootScope.$digest();

            //then
            expect(ctrl.showModalContent).toBe(true);
        }));

        it('should show dynamic parameters in a modal', inject(($rootScope) => {
            //given
            stateMock.playground.dataset = { id: '41fa397a8239cd051b35' };
            const ctrl = createController();
            const dynamicTransformation = { name: 'items', dynamic: true, parameters: [{}] };

            //when
            ctrl.select(dynamicTransformation);
            $rootScope.$digest();

            //then
            expect(ctrl.showModalContent).toBe(true);
        }));
    });
});
