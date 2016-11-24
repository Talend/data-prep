/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Import controller', () => {
	'use strict';

	let ctrl;
	let createController;
	let scope;
	let StateMock;
	const dataset = { id: 'ec4834d9bc2af8', name: 'Customers (50 lines)', draft: false };

	beforeEach(angular.mock.module('data-prep.import', ($provide) => {
		StateMock = {
			inventory: {
				currentFolder: { id: '', path: '', name: 'Home' },
				currentFolderContent: {
					folders: [],
					datasets: [],
				},
			}, import: {
				importTypes: [
					{
						locationType: 'hdfs',
						contentType: 'application/vnd.remote-ds.hdfs',
						parameters: [
							{
								name: 'name',
								type: 'string',
								implicit: false,
								canBeBlank: false,
								format: '',
								default: '',
								description: 'Name',
								label: 'Enter the dataset name:',
							},
							{
								name: 'url',
								type: 'string',
								implicit: false,
								canBeBlank: false,
								format: 'hdfs://host:port/file',
								default: '',
								description: 'URL',
								label: 'Enter the dataset URL:',
							},
						],
						defaultImport: false,
						label: 'From HDFS',
						title: 'Add HDFS dataset',
					},
					{
						locationType: 'http',
						contentType: 'application/vnd.remote-ds.http',
						parameters: [
							{
								name: 'name',
								type: 'string',
								implicit: false,
								canBeBlank: false,
								format: '',
								default: '',
								description: 'Name',
								label: 'Enter the dataset name:',
							},
							{
								name: 'url',
								type: 'string',
								implicit: false,
								canBeBlank: false,
								format: 'http://',
								default: '',
								description: 'URL',
								label: 'Enter the dataset URL:',
							},
						],
						defaultImport: false,
						label: 'From HTTP',
						title: 'Add HTTP dataset',
					},
					{
						locationType: 'local',
						contentType: 'text/plain',
						parameters: [
							{
								name: 'datasetFile',
								type: 'file',
								implicit: false,
								canBeBlank: false,
								format: '*.csv',
								default: '',
								description: 'File',
								label: 'File',
							},
						],
						defaultImport: true,
						label: 'Local File',
						title: 'Add local file dataset',
					},
					{
						locationType: 'job',
						contentType: 'application/vnd.remote-ds.job',
						parameters: [
							{
								name: 'name',
								type: 'string',
								implicit: false,
								canBeBlank: false,
								format: '',
								description: 'Name',
								label: 'Enter the dataset name:',
								default: '',
							},
							{
								name: 'jobId',
								type: 'select',
								implicit: false,
								canBeBlank: false,
								format: '',
								configuration: {
									values: [
										{
											value: '1',
											label: 'TestInput',
										},
									],
									multiple: false,
								},
								description: 'Talend Job',
								label: 'Select the Talend Job:',
								default: '',
							},
						],
						defaultImport: false,
						label: 'From Talend Job',
						title: 'Add Talend Job dataset',
					},
					{
						contentType: 'application/vnd.tcomp-ds.FullExampleDatastore',
						defaultImport: false,
						dynamic: true,
						label: 'From TCOMP example',
						locationType: 'tcomp-FullExampleDatastore',
						parameters: [],
						title: 'Add a TCOMP dataset',
					},
				],
			},
		};
		$provide.constant('state', StateMock);
	}));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();

		createController = () => {
			return $componentController(
				'import',
				{ $scope: scope }
			);
		};
	}));

	afterEach(inject(() => {
		dataset.error = false;
		dataset.progress = false;
	}));

	describe('startDefaultImport', () => {
		it('should call the first import type if no defaultImportType', () => {
			// given
			StateMock.import.importTypes[2].defaultImport = false;

			// when
			ctrl = createController();
			spyOn(ctrl, 'startImport');
			ctrl.startDefaultImport();

			// then
			expect(ctrl.startImport).toHaveBeenCalledWith(StateMock.import.importTypes[0]);
		});

		it('should call the default import type', inject(() => {

			// given
			StateMock.import.importTypes[2].defaultImport = true;

			// when
			ctrl = createController();
			spyOn(ctrl, 'startImport');
			ctrl.startDefaultImport();

			// then
			expect(ctrl.startImport).toHaveBeenCalledWith(StateMock.import.importTypes[2]);
		}));
	});

	describe('startImport', () => {
		it('should start import from local file', () => {
			// given
			ctrl = createController();

			// when
			ctrl.startImport(StateMock.import.importTypes[2]);

			// then
			expect(ctrl.currentInputType).toEqual(StateMock.import.importTypes[2]);
			expect(ctrl.showModal).toBe(false);
		});

		it('should start import from remote', inject(() => {
			// given
			ctrl = createController();

			// when
			ctrl.startImport(StateMock.import.importTypes[0]);

			// then
			expect(ctrl.currentInputType).toEqual(StateMock.import.importTypes[0]);
			expect(ctrl.showModal).toBe(true);
		}));

		it('should start import from remote with dynamic parameters', inject((ImportRestService, $q) => {
			// given
			ctrl = createController();
			StateMock.import.importTypes[0].dynamic = true;
			spyOn(ImportRestService, 'importParameters').and.returnValue($q.when({ data: { name: 'url' } }));

			// when
			ctrl.startImport(StateMock.import.importTypes[0]);

			// then
			expect(ctrl.isFetchingParameters).toEqual(true);
			expect(ImportRestService.importParameters).toHaveBeenCalledWith('hdfs');

			scope.$digest();
			expect(ctrl.currentInputType.parameters).toEqual({ name: 'url' });
			expect(ctrl.isFetchingParameters).toEqual(false);
		}));

		it('should start import from tcomp', inject((ImportRestService, $q) => {
			// given
			ctrl = createController();
			spyOn(ImportRestService, 'importParameters').and.returnValue($q.when({ data: { name: 'tcomp' } }));

			// when
			ctrl.startImport(StateMock.import.importTypes[4]);

			// then
			expect(ctrl.isFetchingParameters).toEqual(true);
			expect(ImportRestService.importParameters).toHaveBeenCalledWith('tcomp-FullExampleDatastore');

			scope.$digest();
			expect(ctrl.currentInputType.datastoreForm).toEqual({ name: 'tcomp' });
			expect(ctrl.isFetchingParameters).toEqual(false);
		}));
	});

	describe('import', () => {
		let uploadDefer;
		beforeEach(inject((StateService, $q, DatasetService, UploadWorkflowService) => {
			ctrl = createController();
			ctrl.datasetFile = [{ name: 'my dataset.csv' }];
			ctrl.datasetName = 'my cool dataset';

			uploadDefer = $q.defer();
			uploadDefer.promise.progress = (callback) => {
				uploadDefer.progressCb = callback;
				return uploadDefer.promise;
			};

			spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when(dataset));
			spyOn(UploadWorkflowService, 'openDataset').and.returnValue();
			spyOn(DatasetService, 'createDatasetInfo').and.callFake(() => {
				return dataset;
			});
			spyOn(DatasetService, 'create').and.returnValue(uploadDefer.promise);
			spyOn(DatasetService, 'update').and.returnValue(uploadDefer.promise);

			spyOn(StateService, 'startUploadingDataset').and.returnValue();
			spyOn(StateService, 'finishUploadingDataset').and.returnValue();

			ctrl.currentInputType = StateMock.import.importTypes[0];
		}));

		it('should show dataset name popup when name already exists', inject(($q, DatasetService) => {
			// given
			const dataset = {
				name: 'my dataset',
			};
			spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.reject());
			expect(ctrl.datasetNameModal).toBeFalsy();

			// when
			ctrl.import(StateMock.import.importTypes[0]);
			scope.$digest();

			// then
			expect(ctrl.datasetNameModal).toBe(true);
		}));

		it('should create dataset if unique', inject(($q, DatasetService) => {
			// given
			spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.when());
			expect(ctrl.datasetNameModal).toBeFalsy();

			// when
			ctrl.import(StateMock.import.importTypes[0]);
			scope.$digest();

			// then
			expect(ctrl.datasetNameModal).toBeFalsy();
			const paramsExpected = { name: 'my dataset', url: '', type: 'hdfs' };
			expect(DatasetService.create).toHaveBeenCalledWith(paramsExpected, 'application/vnd.remote-ds.hdfs', { name: 'my dataset.csv' });
		}));

		it('should close modal if import is successful', inject(($q, DatasetService) => {
			// given
			spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.when());
			expect(ctrl.datasetNameModal).toBeFalsy();

			// when
			ctrl.import(StateMock.import.importTypes[0]);
			scope.$digest();

			// then
			expect(ctrl.showModal).toBe(false);
		}));
	});

	describe('onImportNameValidation', () => {
		let uploadDefer;

		beforeEach(inject((StateService, $q, DatasetService, UploadWorkflowService) => {
			ctrl = createController();
			ctrl.datasetFile = [{ name: 'my dataset.csv' }];
			ctrl.datasetName = 'my cool dataset';

			uploadDefer = $q.defer();
			uploadDefer.promise.progress = (callback) => {
				uploadDefer.progressCb = callback;
				return uploadDefer.promise;
			};

			spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when(dataset));
			spyOn(UploadWorkflowService, 'openDataset').and.returnValue();
			spyOn(DatasetService, 'createDatasetInfo').and.callFake(() => {
				return dataset;
			});
			spyOn(DatasetService, 'create').and.returnValue(uploadDefer.promise);
			spyOn(DatasetService, 'update').and.returnValue(uploadDefer.promise);

			spyOn(StateService, 'startUploadingDataset').and.returnValue();
			spyOn(StateService, 'finishUploadingDataset').and.returnValue();
		}));

		describe('with unique name', () => {
			beforeEach(inject(($q, $rootScope, DatasetService) => {
				spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.when());
				ctrl.currentInputType = StateMock.import.importTypes[0];
			}));

			it('should create dataset if name is unique', inject((StateService, $q, $rootScope, DatasetService, UploadWorkflowService) => {
				// given
				const paramsExpected = { name: 'my cool dataset', url: '', type: 'hdfs' };

				// when
				ctrl.onImportNameValidation();
				uploadDefer.resolve({ data: dataset.id });
				scope.$digest();

				// then
				expect(StateService.startUploadingDataset).toHaveBeenCalled();
				expect(DatasetService.create).toHaveBeenCalledWith(paramsExpected, 'application/vnd.remote-ds.hdfs', { name: 'my dataset.csv' });
				expect(DatasetService.getDatasetById).toHaveBeenCalledWith(dataset.id);
				expect(UploadWorkflowService.openDataset).toHaveBeenCalled();
				expect(StateService.finishUploadingDataset).toHaveBeenCalled();
			}));

			it('should update progress on create', inject((state, StateService, DatasetService) => {
				// given
				ctrl.onImportNameValidation();
				scope.$digest();
				expect(dataset.progress).toBeFalsy();

				const event = {
					loaded: 140,
					total: 200,
				};

				// when
				uploadDefer.progressCb(event);
				scope.$digest();

				// then
				expect(DatasetService.create).toHaveBeenCalled();
				expect(dataset.progress).toBe(70);
			}));

			it('should set error flag and show error toast', inject((StateService, DatasetService) => {
				// given
				ctrl.onImportNameValidation();
				scope.$digest();
				expect(dataset.error).toBeFalsy();

				// when
				uploadDefer.reject();
				scope.$digest();

				// then
				expect(DatasetService.create).toHaveBeenCalled();
				expect(dataset.error).toBe(true);
			}));
		});

		describe('with existing name', () => {
			const dataset = {
				name: 'my cool dataset',
			};
			const existingDataset = { id: '2', name: 'my cool dataset' };
			let confirmDefer;

			beforeEach(inject(($rootScope, $q, StateService, DatasetService, UpdateWorkflowService, TalendConfirmService) => {
				confirmDefer = $q.defer();

				spyOn(StateService, 'resetPlayground').and.returnValue();
				spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.reject(existingDataset));
				spyOn(TalendConfirmService, 'confirm').and.returnValue(confirmDefer.promise);
				spyOn(UpdateWorkflowService, 'updateDataset').and.returnValue($q.when());

				ctrl.currentInputType = StateMock.import.importTypes[0];
				ctrl.datasetName = dataset.name;
			}));

			it('should do nothing on confirm modal dismiss', inject(($q, TalendConfirmService, DatasetService) => {
				// given
				spyOn(DatasetService, 'getUniqueName').and.returnValue($q.when('my cool dataset (1)'));
				ctrl.onImportNameValidation();
				scope.$digest();

				// when
				confirmDefer.reject('dismiss');
				scope.$digest();

				// then
				expect(DatasetService.checkNameAvailability).toHaveBeenCalledWith(ctrl.datasetName);
				expect(TalendConfirmService.confirm).toHaveBeenCalledWith(null, ['UPDATE_EXISTING_DATASET'], { dataset: 'my cool dataset' });
				expect(DatasetService.create).not.toHaveBeenCalled();
				expect(DatasetService.update).not.toHaveBeenCalled();
			}));

			it('should create dataset with modified name', inject(($q, TalendConfirmService, DatasetService) => {
				// given
				spyOn(DatasetService, 'getUniqueName').and.returnValue($q.when('my cool dataset (1)'));
				ctrl.onImportNameValidation();
				scope.$digest();

				// when
				confirmDefer.reject();
				scope.$digest();
				uploadDefer.resolve({ data: 'dataset_id_XYZ' });
				scope.$digest();

				// then
				expect(DatasetService.createDatasetInfo).toHaveBeenCalledWith({ name: 'my dataset.csv' }, 'my cool dataset (1)');
			}));

			it('should update existing dataset', inject(($q, DatasetService, UpdateWorkflowService) => {
				// given
				spyOn(DatasetService, 'getUniqueName').and.returnValue($q.reject('my cool dataset (1)'));
				ctrl.onImportNameValidation();
				scope.$digest();

				// when
				confirmDefer.resolve();
				scope.$digest();
				uploadDefer.resolve();
				scope.$digest();

				// then
				expect(UpdateWorkflowService.updateDataset).toHaveBeenCalledWith({ name: 'my dataset.csv' }, existingDataset);
			}));
		});
	});
});
