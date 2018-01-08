/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('UploadWorkflow Service', () => {
	'use strict';

	let uploadDefer;
	let file = {};
	let existingDataset = {};
	let dataset = { id: 'ec4834d9bc2af8', name: 'Customers (50 lines)', draft: false };
	let stateMock;

	beforeEach(angular.mock.module('data-prep.services.datasetWorkflowService', ($provide) => {
		stateMock = {
			progress: {
				types: {
					progression: 'PROGRESSION',
					infinite: 'INFINITE',
				},
				states: {
					inProgress: 'IN_PROGRESS',
					future: 'FUTURE',
					complete: 'COMPLETE',
				},
				schemas: {
					dataset: {
						title: 'ADD_NEW_DATASET',
						steps: [
							{
								type: 'PROGRESSION',
								state: 'IN_PROGRESS',
								label: 'UPLOADING_FILE',
							},
							{
								type: 'INFINITE',
								state: 'FUTURE',
								label: 'PROFILING_DATA',
							},
						],
					},
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($q) => {
		uploadDefer = $q.defer();
		uploadDefer.promise.progress = (callback) => {
			uploadDefer.progressCb = callback;
			return uploadDefer.promise;
		};
	}));

	beforeEach(inject(($q, StateService, MessageService, DatasetService, UploadWorkflowService) => {
		spyOn(StateService, 'startUploadingDataset').and.returnValue();
		spyOn(StateService, 'resetPlayground').and.returnValue();
		spyOn(StateService, 'finishUploadingDataset').and.returnValue();

		spyOn(MessageService, 'success').and.returnValue();

		spyOn(UploadWorkflowService, 'openDataset').and.returnValue(uploadDefer.promise);

		spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when(dataset));
		spyOn(DatasetService, 'update').and.returnValue(uploadDefer.promise);
		spyOn(DatasetService, 'createDatasetInfo').and.callFake(() => dataset);
	}));

	afterEach(() => {
		dataset.error = false;
		dataset.progress = false;
	});

	it('should update the progress on update', inject(($rootScope, UpdateWorkflowService, StateService, DatasetService) => {
		//given
		UpdateWorkflowService.updateDataset(file, existingDataset);
		expect(DatasetService.createDatasetInfo).toHaveBeenCalled();
		expect(StateService.startUploadingDataset).toHaveBeenCalled();
		expect(dataset.progress).toBeFalsy();

		var event = {
			loaded: 140,
			total: 200,
		};

		//when
		uploadDefer.progressCb(event);
		$rootScope.$digest();

		//then
		expect(dataset.progress).toBe(70);
	}));

	it('should update the file and reset the playground', inject(($rootScope, UpdateWorkflowService, MessageService, UploadWorkflowService, StateService, DatasetService) => {
		//given
		UpdateWorkflowService.updateDataset(file, existingDataset);

		//when
		uploadDefer.resolve({ data: dataset.id });
		$rootScope.$digest();

		//then
		expect(MessageService.success).toHaveBeenCalled();
		expect(DatasetService.getDatasetById).toHaveBeenCalledWith(dataset.id);
		expect(StateService.resetPlayground).toHaveBeenCalled();
		expect(UploadWorkflowService.openDataset).toHaveBeenCalledWith(dataset);
		expect(StateService.finishUploadingDataset).toHaveBeenCalled();
	}));

	it('should set error flag and show error toast when update fails', inject(($rootScope, UpdateWorkflowService, StateService, DatasetService) => {
		//given
		UpdateWorkflowService.updateDataset(file, existingDataset);
		expect(dataset.error).toBeFalsy();

		//when
		uploadDefer.reject();
		$rootScope.$digest();

		//then
		expect(StateService.resetPlayground).not.toHaveBeenCalled();
		expect(DatasetService.update).toHaveBeenCalled();
		expect(dataset.error).toBe(true);
		expect(StateService.finishUploadingDataset).toHaveBeenCalled();
	}));
});
