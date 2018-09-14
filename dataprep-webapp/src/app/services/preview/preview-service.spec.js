import DataViewMock from '../../../mocks/DataView.mock';

describe('Preview Service', () => {
    'use strict';

    let stateMock;
    let dataViewMock;
    let shouldPreviewReturnError;
    const gridRangeIndex = { top: 1, bottom: 5 };
    const displayedTdpIds = [1, 3, 6, 7, 8];
    const originalData = {
        records: [
            { tdpId: 0, firstname: 'Tata' },
            { tdpId: 1, firstname: 'Tete' },
            { tdpId: 2, firstname: 'Titi' },
            { tdpId: 3, firstname: 'Toto' },
            { tdpId: 4, firstname: 'Tutu' },
            { tdpId: 5, firstname: 'Tyty' },
            { tdpId: 6, firstname: 'Papa' },
            { tdpId: 7, firstname: 'Pepe' },
            { tdpId: 8, firstname: 'Pipi' },
            { tdpId: 9, firstname: 'Popo' },
            { tdpId: 10, firstname: 'Pupu' },
            { tdpId: 11, firstname: 'Pypy' },
        ],
        metadata: { columns: [{ id: '0000', name: 'lastname' }, { id: '0001', name: 'firstname' }] },
    };

    // diff result corresponding to gridRangeIndex
    const diff = {
        data: {
            records: [
                { tdpId: 1, firstname: 'Tete' },
                { tdpId: 2, firstname: 'Titi Bis', __tdpRowDiff: 'new' }, //insert new row
                { tdpId: 3, firstname: 'Toto', __tdpRowDiff: 'delete' }, //row is deleted in preview
                { tdpId: 6, firstname: 'Papa' },
                { tdpId: 7, firstname: 'Pepe 2', __tdpDiff: { firstname: 'update' } }, //firstname is updated in preview
                { tdpId: 8, firstname: 'Pipi' },
            ],
            metadata: { columns: [{ id: '0000', name: 'lastname' }, { id: '0001', name: 'firstname' }] },
        },
    };

    const previewExecutor = {};
    const reverterExecutor = {};

    beforeEach(angular.mock.module('data-prep.services.preview', ($provide) => {
        stateMock = { playground: { grid: { nbLines : 1000}, sampleType: 'HEAD' } };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, PreviewService, DatagridService, PreparationService, StateService) => {
        stateMock.playground.data = originalData;
        dataViewMock = new DataViewMock();
        stateMock.playground.grid.dataView = dataViewMock;
        PreviewService.gridRangeIndex = gridRangeIndex;

        shouldPreviewReturnError = false;
        const previewMock = () => shouldPreviewReturnError ? $q.reject() : $q.when(diff);

        // simulate datagrid get item to have displayedTdpIds = [1,3,6,7,8]
        spyOn(stateMock.playground.grid.dataView, 'getItem').and.callFake((id) => {
            switch (id) {
                case 1:
                    return originalData.records[1];
                case 2:
                    return originalData.records[3];
                case 3:
                    return originalData.records[6];
                case 4:
                    return originalData.records[7];
                case 5:
                    return originalData.records[8];
            }
            return null;
        });

        // simulate datagrid get array index by (tdp) id
        spyOn(stateMock.playground.grid.dataView, 'getIdxById').and.callFake((id) => {
            switch (id) {
                case 1:
                    return 1;
                case 8:
                    return 8;
            }
            return null;
        });

        spyOn(DatagridService, 'previewDataExecutor').and.returnValue(previewExecutor);
        spyOn(DatagridService, 'execute').and.returnValue(reverterExecutor);

        spyOn(PreparationService, 'getPreviewDiff').and.callFake(previewMock);
        spyOn(PreparationService, 'getPreviewUpdate').and.callFake(previewMock);
        spyOn(PreparationService, 'getPreviewAdd').and.callFake(previewMock);

	    spyOn(StateService, 'setPreviewIsLoading').and.callFake(previewMock);
    }));

    describe('Disable preview when the datagrid is empty', () => {
        it('should not call getPreviewDiff', inject(($rootScope, PreviewService, PreparationService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const currentStep = {
                column: { id: '0001' },
                transformation: { stepId: '1' },
            };
            const previewStep = {
                column: { id: '0000' },
                transformation: { stepId: '2' },
            };
            stateMock.playground.grid.nbLines = 0;
            // when
            PreviewService.getPreviewDiffRecords(preparationId, currentStep, previewStep, null);

            // then
            expect(PreparationService.getPreviewDiff).not.toHaveBeenCalled();
        }));

        it('should not call getPreviewAdd', inject(($rootScope, PreviewService, PreparationService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const datasetId = '46c541b683ef5151';
            const action = 'fillEmptyWithValue';
            const actionParams = [
                { scope: 'column', column_id: '0001', value: '--' },
                { scope: 'column', column_id: '0002', value: '--' },
            ];
            stateMock.playground.grid.nbLines = 0;
            // when
            PreviewService.getPreviewAddRecords(preparationId, datasetId, action, actionParams);

            // then
            expect(PreparationService.getPreviewAdd).not.toHaveBeenCalled();
        }));

        it('should not call getPreviewUpdate', inject(($rootScope, PreviewService, PreparationService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const currentStep = {
                column: { id: '0001' },
                transformation: { stepId: '1' },
                actionParameters: { action: 'fillEmptyWithValue' },
            };
            const updateStep = {
                column: { id: '0000' },
                transformation: { stepId: '2' },
                actionParameters: { action: 'fillEmptyWithValue' },
            };
            const newParams = { value: '--' };
            stateMock.playground.grid.nbLines = 0;
            // when
            PreviewService.getPreviewUpdateRecords(preparationId, currentStep, updateStep, newParams);

            // then
            expect(PreparationService.getPreviewUpdate).not.toHaveBeenCalled();
        }));

        it('should not call getPreviewUpdateRecords', inject(($rootScope, PreviewService) => {
            // given
            $rootScope.$digest();
            const step = {
                column: { id: '0', name: 'state' },
                transformation: {
                    stepId: 'a598bc83fc894578a8b823',
                    name: 'cut',
                },
                actionParameters: {
                    action: 'cut',
                    parameters: { pattern: '.', column_id: '0', column_name: 'state', scope: 'column' },
                },
            };
            const parameters = { pattern: '--' };
            stateMock.playground.grid.nbLines = 0;

            spyOn(PreviewService, 'getPreviewUpdateRecords');

            // when
            PreviewService.updatePreview(step, parameters);

            // then
            expect(PreviewService.getPreviewUpdateRecords).not.toHaveBeenCalled();
        }));

    });

    describe('diff preview', () => {
	    it('should set preview loading to true', inject(($rootScope, PreviewService, StateService) => {
		    // given
		    const preparationId = '86c4135ab218646f54';
		    const currentStep = {
			    column: { id: '0001' },
			    transformation: { stepId: '1' },
		    };
		    const previewStep = {
			    column: { id: '0000' },
			    transformation: { stepId: '2' },
		    };

		    // when
		    PreviewService.getPreviewDiffRecords(preparationId, currentStep, previewStep, null);
		    $rootScope.$digest();

		    // then
		    expect(StateService.setPreviewIsLoading).toHaveBeenCalledWith(true);
	    }));

        it('should call and display preview', inject(($rootScope, PreviewService, PreparationService, DatagridService, StateService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const currentStep = {
                column: { id: '0001' },
                transformation: { stepId: '1' },
            };
            const previewStep = {
                column: { id: '0000' },
                transformation: { stepId: '2' },
            };

            // when
            PreviewService.getPreviewDiffRecords(preparationId, currentStep, previewStep, null);
            $rootScope.$digest();

            // then
            expect(PreparationService.getPreviewDiff).toHaveBeenCalled();

            const previewArgs = PreparationService.getPreviewDiff.calls.mostRecent().args;
            expect(previewArgs[0]).toEqual({
                preparationId: preparationId,
                currentStepId: currentStep.transformation.stepId,
                previewStepId: previewStep.transformation.stepId,
                tdpIds: displayedTdpIds,
                sourceType: 'HEAD',
            });

            expect(DatagridService.execute).toHaveBeenCalledWith(undefined); //reverter but no preview to revert
            expect(DatagridService.execute).toHaveBeenCalledWith(previewExecutor); //preview diff
	        expect(StateService.setPreviewIsLoading).toHaveBeenCalledWith(false);
        }));

        it('should cancel preview on error', inject(($rootScope, PreviewService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const currentStep = {
                column: { id: '0001' },
                transformation: { stepId: '1' },
            };
            const previewStep = {
                column: { id: '0000' },
                transformation: { stepId: '2' },
            };
            shouldPreviewReturnError = true;
            let rejected = false;

            // when
            PreviewService.getPreviewDiffRecords(preparationId, currentStep, previewStep, null)
                .catch(() => rejected = true);
            expect(PreviewService.previewInProgress()).toBe(true);
            $rootScope.$digest();

            // then
            expect(rejected).toBe(true);
            expect(PreviewService.previewInProgress()).toBe(false);
        }));

        it('should focus on provided column', inject(($rootScope, PreviewService, DatagridService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const currentStep = {
                column: { id: '0001' },
                transformation: { stepId: '1' },
            };
            const previewStep = {
                column: { id: '0000' },
                transformation: { stepId: '2' },
            };
            const focusColumnId = '0000';

            // when
            PreviewService.getPreviewDiffRecords(preparationId, currentStep, previewStep, focusColumnId);
            $rootScope.$digest();

            // then
            expect(DatagridService.focusedColumn).toBe(focusColumnId);
        }));

        it('should cancel current pending preview', inject(($rootScope, PreviewService, PreparationService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const currentStep = {
                column: { id: '0001' },
                transformation: { stepId: '1' },
            };
            const previewStep = {
                column: { id: '0000' },
                transformation: { stepId: '2' },
            };

            PreviewService.getPreviewDiffRecords(preparationId, currentStep, previewStep, null);
            const previewArgs = PreparationService.getPreviewDiff.calls.mostRecent().args;
            const previewCanceler = previewArgs[1];

            expect(previewCanceler.promise.$$state.status).toBe(0); //eslint-disable-line angular/no-private-call

            // when
            PreviewService.getPreviewDiffRecords(preparationId, currentStep, previewStep, null);

            // then
            expect(previewCanceler.promise.$$state.status).toBe(1); //eslint-disable-line angular/no-private-call
        }));
    });

    describe('update preview', () => {
	    it('should set preview loading to true', inject(($rootScope, PreviewService, StateService) => {
		    // given
		    const preparationId = '86c4135ab218646f54';
		    const currentStep = {
			    column: { id: '0001' },
			    transformation: { stepId: '1' },
			    actionParameters: { action: 'fillEmptyWithValue' },
		    };
		    const updateStep = {
			    column: { id: '0000' },
			    transformation: { stepId: '2' },
			    actionParameters: { action: 'fillEmptyWithValue' },
		    };
		    const newParams = { value: '--' };

		    // when
		    PreviewService.getPreviewUpdateRecords(preparationId, currentStep, updateStep, newParams);

		    // then
		    expect(StateService.setPreviewIsLoading).toHaveBeenCalledWith(true);
	    }));
        it('should call and display preview', inject(($rootScope, PreviewService, PreparationService, DatagridService, StateService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const currentStep = {
                column: { id: '0001' },
                transformation: { stepId: '1' },
                actionParameters: { action: 'fillEmptyWithValue' },
            };
            const updateStep = {
                column: { id: '0000' },
                transformation: { stepId: '2' },
                actionParameters: { action: 'fillEmptyWithValue' },
            };
            const newParams = { value: '--' };

            // when
            PreviewService.getPreviewUpdateRecords(preparationId, currentStep, updateStep, newParams);
            $rootScope.$digest();

            // then
            expect(PreparationService.getPreviewUpdate).toHaveBeenCalled();

            const previewArgs = PreparationService.getPreviewUpdate.calls.mostRecent().args;
            expect(previewArgs[0]).toEqual({
                preparationId: preparationId,
                tdpIds: displayedTdpIds,
                currentStepId: currentStep.transformation.stepId,
                updateStepId: updateStep.transformation.stepId,
                action: {
                    action: updateStep.actionParameters.action,
                    parameters: newParams,
                },
                sourceType: 'HEAD',
            });

            expect(DatagridService.execute).toHaveBeenCalledWith(undefined); //reverter but no preview to revert
            expect(DatagridService.execute).toHaveBeenCalledWith(previewExecutor); //preview diff
	        expect(StateService.setPreviewIsLoading).toHaveBeenCalledWith(false);
        }));

        it('should cancel preview on error', inject(($rootScope, PreviewService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const currentStep = {
                column: { id: '0001' },
                transformation: { stepId: '1' },
                actionParameters: { action: 'fillEmptyWithValue' },
            };
            const updateStep = {
                column: { id: '0000' },
                transformation: { stepId: '2' },
                actionParameters: { action: 'fillEmptyWithValue' },
            };
            const newParams = { value: '--' };

            shouldPreviewReturnError = true;
            let rejected = false;

            // when
            PreviewService.getPreviewUpdateRecords(preparationId, currentStep, updateStep, newParams)
                .catch(() => rejected = true);
            expect(PreviewService.previewInProgress()).toBe(true);
            $rootScope.$digest();

            // then
            expect(rejected).toBe(true);
            expect(PreviewService.previewInProgress()).toBe(false);
        }));

        it('should focus on update step column', inject(($rootScope, PreviewService, DatagridService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const currentStep = {
                column: { id: '0001' },
                transformation: { stepId: '1' },
                actionParameters: { action: 'fillEmptyWithValue' },
            };
            const updateStep = {
                column: { id: '0000' },
                transformation: { stepId: '2' },
                actionParameters: { action: 'fillEmptyWithValue' },
            };
            const newParams = { value: '--' };

            // when
            PreviewService.getPreviewUpdateRecords(preparationId, currentStep, updateStep, newParams);
            $rootScope.$digest();

            // then
            expect(DatagridService.focusedColumn).toBe(updateStep.column.id);
        }));

        it('should cancel current pending preview', inject(($rootScope, PreviewService, PreparationService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const currentStep = {
                column: { id: '0001' },
                transformation: { stepId: '1' },
                actionParameters: { action: 'fillEmptyWithValue' },
            };
            const updateStep = {
                column: { id: '0000' },
                transformation: { stepId: '2' },
                actionParameters: { action: 'fillEmptyWithValue' },
            };
            const newParams = { value: '--' };

            PreviewService.getPreviewUpdateRecords(preparationId, currentStep, updateStep, newParams);
            const previewArgs = PreparationService.getPreviewUpdate.calls.mostRecent().args;
            const previewCanceler = previewArgs[1];

            expect(previewCanceler.promise.$$state.status).toBe(0); //eslint-disable-line angular/no-private-call

            // when
            PreviewService.getPreviewUpdateRecords(preparationId, currentStep, updateStep, newParams);

            // then
            expect(previewCanceler.promise.$$state.status).toBe(1); //eslint-disable-line angular/no-private-call
        }));
    });

    describe('add preview', () => {
	    it('should set preview loading to true', inject(($rootScope, PreviewService, StateService) => {
		    // given
		    const preparationId = '86c4135ab218646f54';
		    const datasetId = '46c541b683ef5151';
		    const action = 'fillEmptyWithValue';
		    const actionParams = [
			    { scope: 'column', column_id: '0001', value: '--' },
			    { scope: 'column', column_id: '0002', value: '--' },
		    ];

		    // when
		    PreviewService.getPreviewAddRecords(preparationId, datasetId, action, actionParams);

		    // then
		    expect(StateService.setPreviewIsLoading).toHaveBeenCalledWith(true);
	    }));

        it('should call and display preview', inject(($rootScope, PreviewService, PreparationService, DatagridService, StateService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const datasetId = '46c541b683ef5151';
            const action = 'fillEmptyWithValue';
            const actionParams = [
                { scope: 'column', column_id: '0001', value: '--' },
                { scope: 'column', column_id: '0002', value: '--' },
            ];

            // when
            PreviewService.getPreviewAddRecords(preparationId, datasetId, action, actionParams);
            $rootScope.$digest();

            // then
            expect(PreparationService.getPreviewAdd).toHaveBeenCalled();

            const previewArgs = PreparationService.getPreviewAdd.calls.mostRecent().args;
            expect(previewArgs[0]).toEqual({
                actions: [
                    { action: action, parameters: actionParams[0] },
                    { action: action, parameters: actionParams[1] },
                ],
                tdpIds: displayedTdpIds,
                datasetId: datasetId,
                preparationId: preparationId,
                sourceType: 'HEAD',
            });

            expect(DatagridService.execute).toHaveBeenCalledWith(undefined); //reverter but no preview to revert
            expect(DatagridService.execute).toHaveBeenCalledWith(previewExecutor); //preview diff
	        expect(StateService.setPreviewIsLoading).toHaveBeenCalledWith(false);
        }));

        it('should cancel preview on error', inject(($rootScope, PreviewService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const datasetId = '46c541b683ef5151';
            const action = 'fillEmptyWithValue';
            const actionParams = [{
                scope: 'column',
                column_id: '0001',
                value: '--',
            }];

            shouldPreviewReturnError = true;
            let rejected = false;

            // when
            PreviewService.getPreviewAddRecords(preparationId, datasetId, action, actionParams)
                .catch(() => rejected = true);
            expect(PreviewService.previewInProgress()).toBe(true);
            $rootScope.$digest();

            // then
            expect(rejected).toBe(true);
            expect(PreviewService.previewInProgress()).toBe(false);
        }));

        it('should focus on add step column', inject(($rootScope, PreviewService, DatagridService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const datasetId = '46c541b683ef5151';
            const action = 'fillEmptyWithValue';
            const params = [{
                scope: 'column',
                column_id: '0001',
                value: '--',
            }];

            // when
            PreviewService.getPreviewAddRecords(preparationId, datasetId, action, params);
            $rootScope.$digest();

            // then
            expect(DatagridService.focusedColumn).toBe('0001');
        }));

        it('should cancel current pending preview', inject(($rootScope, PreviewService, PreparationService) => {
            // given
            const preparationId = '86c4135ab218646f54';
            const datasetId = '46c541b683ef5151';
            const action = 'fillEmptyWithValue';
            const params = [{
                scope: 'column',
                column_id: '0001',
                value: '--',
            }];

            PreviewService.getPreviewAddRecords(preparationId, datasetId, action, params);
            const previewArgs = PreparationService.getPreviewAdd.calls.mostRecent().args;
            const previewCanceler = previewArgs[1];

            expect(previewCanceler.promise.$$state.status).toBe(0); //eslint-disable-line angular/no-private-call

            // when
            PreviewService.getPreviewAddRecords(datasetId, action, params);

            // then
            expect(previewCanceler.promise.$$state.status).toBe(1); //eslint-disable-line angular/no-private-call
        }));
    });

    describe('reset/cancel/stop preview', () => {
        const preparationId = '86c4135ab218646f54';

        beforeEach(inject(($rootScope, PreviewService) => {
            const currentStep = {
                column: { id: '0001' },
                transformation: { stepId: '1' },
            };
            const previewStep = {
                column: { id: '0000' },
                transformation: { stepId: '2' },
            };

            PreviewService.getPreviewDiffRecords(preparationId, currentStep, previewStep, null);
        }));

        it('should stop pending preview', inject((PreviewService, PreparationService) => {
            // given
            const previewArgs = PreparationService.getPreviewDiff.calls.mostRecent().args;
            const previewCanceler = previewArgs[1];

            expect(previewCanceler.promise.$$state.status).toBe(0); //eslint-disable-line angular/no-private-call

            // when
            PreviewService.stopPendingPreview();

            // then
            expect(previewCanceler.promise.$$state.status).toBe(1); //eslint-disable-line angular/no-private-call
        }));

        it('should restore original data on reset', inject(($rootScope, PreviewService, DatagridService) => {
            // given
            $rootScope.$digest();
            expect(DatagridService.execute.calls.count()).toBe(2);
            expect(DatagridService.execute).toHaveBeenCalledWith(undefined);
            expect(DatagridService.execute).toHaveBeenCalledWith(previewExecutor);

            // when
            PreviewService.reset(true);

            // then
            expect(DatagridService.execute.calls.count()).toBe(3);
            expect(DatagridService.execute).toHaveBeenCalledWith(reverterExecutor);
        }));

        it('should NOT restore original data on reset', inject((PreviewService, DatagridService) => {
            // given
            expect(DatagridService.execute).not.toHaveBeenCalled();

            // when
            PreviewService.reset(false);

            // then
            expect(DatagridService.execute).not.toHaveBeenCalled();
        }));

        it('should stop pending preview on cancel call', inject((PreviewService, PreparationService) => {
            // given
            const previewArgs = PreparationService.getPreviewDiff.calls.mostRecent().args;
            const previewCanceler = previewArgs[1];

            expect(previewCanceler.promise.$$state.status).toBe(0); //eslint-disable-line angular/no-private-call

            // when
            PreviewService.cancelPreview();

            // then
            expect(previewCanceler.promise.$$state.status).toBe(1); //eslint-disable-line angular/no-private-call
        }));

        it('should set focused column and restore original data on cancel call', inject(($rootScope, PreviewService, DatagridService) => {
            // given
            $rootScope.$digest();
            expect(DatagridService.execute.calls.count()).toBe(2);
            expect(DatagridService.execute).toHaveBeenCalledWith(undefined);
            expect(DatagridService.execute).toHaveBeenCalledWith(previewExecutor);
            expect(DatagridService.focusedColumn).toBeFalsy();

            const focusedColId = '0001';

            // when
            PreviewService.cancelPreview(focusedColId);

            // then
            expect(DatagridService.execute.calls.count()).toBe(3);
            expect(DatagridService.execute).toHaveBeenCalledWith(reverterExecutor);
            expect(DatagridService.focusedColumn).toBe(focusedColId);
        }));
    });

    describe('update preview', () => {
        const lastActiveStep = { inactive: false };

        beforeEach(inject(($q, StepUtilsService, PreviewService) => {
            spyOn(PreviewService, 'getPreviewUpdateRecords').and.returnValue($q.when());
            spyOn(StepUtilsService, 'getLastActiveStep').and.returnValue(lastActiveStep);
            const preparationId = '64f3543cd466f545';
            stateMock.playground.preparation = { id: preparationId };
        }));

        it('should call update preview', inject(($rootScope, PreviewService) => {
            // given
            $rootScope.$digest();
            const step = {
                column: { id: '0', name: 'state' },
                transformation: {
                    stepId: 'a598bc83fc894578a8b823',
                    name: 'cut',
                },
                actionParameters: {
                    action: 'cut',
                    parameters: { pattern: '.', column_id: '0', column_name: 'state', scope: 'column' },
                },
            };
            const parameters = { pattern: '--' };

            // when
            PreviewService.updatePreview(step, parameters);
            $rootScope.$digest();

            // then
            expect(PreviewService.getPreviewUpdateRecords).toHaveBeenCalledWith(
                stateMock.playground.preparation.id,
                lastActiveStep,
                step,
                { pattern: '--', column_id: '0', column_name: 'state', scope: 'column' });
        }));

        it('should do nothing on update preview if the step is inactive', inject(($rootScope, PreviewService) => {
            // given
            const step = {
                column: { id: 'state' },
                transformation: {
                    stepId: 'a598bc83fc894578a8b823',
                    name: 'cut',
                },
                actionParameters: {
                    action: 'cut',
                    parameters: { pattern: '.', column_name: 'state' },
                },
                inactive: true,
            };
            const parameters = { pattern: '--' };

            // when
            PreviewService.updatePreview(step, parameters);
            $rootScope.$digest();

            // then
            expect(PreviewService.getPreviewUpdateRecords).not.toHaveBeenCalled();
        }));

        it('should do nothing on update preview if the params have not changed', inject(($rootScope, PreviewService) => {
            // given
            const step = {
                column: { id: '0', name: 'state' },
                transformation: {
                    stepId: 'a598bc83fc894578a8b823',
                    name: 'cut',
                },
                actionParameters: {
                    action: 'cut',
                    parameters: { pattern: '.', column_id: '0', column_name: 'state' },
                },
            };
            const parameters = { pattern: '.' };

            // when
            PreviewService.updatePreview(step, parameters);
            $rootScope.$digest();

            // then
            expect(PreviewService.getPreviewUpdateRecords).not.toHaveBeenCalled();
        }));
    });
});
