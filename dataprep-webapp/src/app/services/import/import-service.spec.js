/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Import service', () => {
	let importTypes;
	let stateMock;
	const dataset = { id: 'ec4834d9bc2af8', name: 'Customers (50 lines)', draft: false };

	beforeEach(angular.mock.module('data-prep.services.import', ($provide) => {
		stateMock = {
			inventory: {
				currentFolder: { id: '', path: '', name: 'Home' },
				currentFolderContent: {
					folders: [],
					datasets: [],
				},
			},
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

	beforeEach(() => {
		importTypes = [
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
						name: 'importDatasetFile',
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
		];
	});

	describe('importParameters', () => {
		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'importParameters').and.returnValue($q.when());

		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			// given
			const locationType = 'toto';

			// when
			ImportService.importParameters(locationType);

			// then
			expect(ImportRestService.importParameters).toHaveBeenCalledWith(locationType);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			// given
			const locationType = 'toto';
			spyOn($rootScope, '$emit').and.returnValue();

			// when
			ImportService.importParameters(locationType);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			// then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
		}));
	});

	describe('refreshForm', () => {
		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'refreshForm').and.returnValue($q.when());
		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			// given
			const propertyName = 'tata';
			const formData = {};

			// when
			ImportService.refreshForm(propertyName, formData);

			// then
			expect(ImportRestService.refreshForm).toHaveBeenCalledWith(propertyName, formData);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			// given
			const propertyName = 'tata';
			const formData = {};
			spyOn($rootScope, '$emit').and.returnValue();

			// when
			ImportService.refreshForm(propertyName, formData);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			// then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
		}));
	});

	describe('refreshForms', () => {
		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'refreshForms').and.returnValue($q.when());
		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			// given
			const propertyName = 'tata';
			const formsData = {};

			// when
			ImportService.refreshForms(propertyName, formsData);

			// then
			expect(ImportRestService.refreshForms).toHaveBeenCalledWith(propertyName, formsData);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			// given
			const propertyName = 'tata';
			const formsData = {};
			spyOn($rootScope, '$emit').and.returnValue();

			// when
			ImportService.refreshForms(propertyName, formsData);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			// then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
		}));
	});

	describe('testConnection', () => {
		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'testConnection').and.returnValue($q.when());
		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			// given
			const formId = 'toto';
			const formData = {};

			// when
			ImportService.testConnection(formId, formData);

			// then
			expect(ImportRestService.testConnection).toHaveBeenCalledWith(formId, formData);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			// given
			const formId = 'toto';
			const formData = {};
			spyOn($rootScope, '$emit').and.returnValue();

			// when
			ImportService.testConnection(formId, formData);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			// then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');

		}));
	});

	describe('createDataset', () => {
		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'createDataset').and.returnValue($q.when());

		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			// given
			const definitionName = 'toto';
			const formsData = {};

			// when
			ImportService.createDataset(definitionName, formsData);

			// then
			expect(ImportRestService.createDataset).toHaveBeenCalledWith(definitionName, formsData);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			// given
			const definitionName = 'toto';
			const formsData = {};
			spyOn($rootScope, '$emit').and.returnValue();

			// when
			ImportService.createDataset(definitionName, formsData);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			// then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
		}));
	});

	describe('getFormsByDatasetId', () => {
		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'getFormsByDatasetId').and.returnValue($q.when());
		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			// given
			const datasetId = '123-abc-456';

			// when
			ImportService.getFormsByDatasetId(datasetId);

			// then
			expect(ImportRestService.getFormsByDatasetId).toHaveBeenCalledWith(datasetId);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			// given
			const datasetId = '123-abc-456';
			spyOn($rootScope, '$emit').and.returnValue();

			// when
			ImportService.getFormsByDatasetId(datasetId);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			// then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
		}));
	});

	describe('editDataset', () => {
		const datasetId = '123-abc-456';
		const formsData = {
			dataStoreProperties: {},
			dataSetProperties: {},
		};

		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'editDataset').and.returnValue($q.when());
		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			// when
			ImportService.editDataset(datasetId, formsData);

			// then
			expect(ImportRestService.editDataset).toHaveBeenCalledWith(datasetId, formsData);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			// given
			spyOn($rootScope, '$emit').and.returnValue();

			// when
			ImportService.editDataset(datasetId);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			// then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
		}));
	});

	describe('import', () => {
		let uploadDefer;
		beforeEach(inject((StateService, $q, DatasetService, ImportService, UploadWorkflowService) => {
			ImportService.importDatasetFile = [{ name: 'my dataset.csv' }];
			ImportService.datasetName = 'my cool dataset';

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

			ImportService.currentInputType = importTypes[0];
		}));

		it('should show dataset name popup when name already exists', inject(($rootScope, $q, DatasetService, ImportService) => {
			// given
			spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.reject());
			expect(ImportService.datasetNameModal).toBeFalsy();

			// when
			ImportService.import(importTypes[0]);
			uploadDefer.resolve({ data: dataset.id });
			$rootScope.$apply();

			// then
			expect(ImportService.datasetNameModal).toBe(true);
		}));

		it('should create dataset if unique', inject(($rootScope, $q, DatasetService, ImportService) => {
			// given
			spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.when());
			expect(ImportService.datasetNameModal).toBeFalsy();

			// when
			ImportService.import(importTypes[0]);
			uploadDefer.resolve({ data: dataset.id });
			$rootScope.$apply();

			// then
			expect(ImportService.datasetNameModal).toBeFalsy();
			const paramsExpected = { url: '', type: 'hdfs', name: 'my dataset', size: undefined };
			expect(DatasetService.create).toHaveBeenCalledWith(paramsExpected, 'application/vnd.remote-ds.hdfs', { name: 'my dataset.csv' });
		}));

		it('should close modal if import is successful', inject(($rootScope, $q, DatasetService, ImportService, StateService) => {
			// given
			spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.when());
			spyOn(StateService, 'hideImport');
			expect(ImportService.datasetNameModal).toBeFalsy();

			// when
			ImportService.import(importTypes[0]);
			uploadDefer.resolve({ data: dataset.id });
			$rootScope.$apply();

			// then
			expect(StateService.hideImport).toHaveBeenCalled();
		}));

		it('should call a custom action after import', inject(($rootScope, $q, DatasetService, ImportService, StateService) => {
			// given
			spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.when());
			const customAction = jasmine.createSpy('customAction');

			// when
			ImportService.import(importTypes[0], customAction);
			uploadDefer.resolve({ data: dataset.id });
			$rootScope.$apply();

			// then
			expect(customAction).toHaveBeenCalled();
		}));
	});

	describe('onImportNameValidation', () => {
		let uploadDefer;

		beforeEach(inject((StateService, $q, DatasetService, ImportService, UploadWorkflowService) => {
			ImportService.importDatasetFile = [{ name: 'my dataset.csv' }];
			ImportService.datasetName = 'my cool dataset';

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
			beforeEach(inject(($q, DatasetService, ImportService) => {
				spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.when());
				ImportService.currentInputType = importTypes[0];
			}));

			it('should create dataset if name is unique', inject((StateService, $q, $rootScope, DatasetService, ImportService, UploadWorkflowService) => {
				// given
				const paramsExpected = { url: '', type: 'hdfs', name: 'my cool dataset', size: undefined };

				// when
				ImportService.onImportNameValidation();
				uploadDefer.resolve({ data: dataset.id });
				$rootScope.$apply();

				// then
				expect(StateService.startUploadingDataset).toHaveBeenCalled();
				expect(DatasetService.create).toHaveBeenCalledWith(paramsExpected, 'application/vnd.remote-ds.hdfs', { name: 'my dataset.csv' });
				expect(DatasetService.getDatasetById).toHaveBeenCalledWith(dataset.id);
				expect(UploadWorkflowService.openDataset).toHaveBeenCalled();
				expect(StateService.finishUploadingDataset).toHaveBeenCalled();
			}));

			it('should update progress on create', inject(($rootScope, state, StateService, DatasetService, ImportService) => {
				// given
				ImportService.onImportNameValidation();
				$rootScope.$apply();
				expect(dataset.progress).toBeFalsy();

				const event = {
					loaded: 140,
					total: 200,
				};

				// when
				uploadDefer.progressCb(event);
				$rootScope.$apply();

				// then
				expect(DatasetService.create).toHaveBeenCalled();
				expect(dataset.progress).toBe(70);
			}));

			it('should set error flag and show error toast', inject(($rootScope, StateService, DatasetService, ImportService) => {
				// given
				ImportService.onImportNameValidation();
				$rootScope.$apply();
				expect(dataset.error).toBeFalsy();

				// when
				uploadDefer.reject();
				$rootScope.$apply();

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

			beforeEach(inject(($q, StateService, DatasetService, ImportService, UpdateWorkflowService, TalendConfirmService) => {
				confirmDefer = $q.defer();

				spyOn(StateService, 'resetPlayground').and.returnValue();
				spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.reject(existingDataset));
				spyOn(TalendConfirmService, 'confirm').and.returnValue(confirmDefer.promise);
				spyOn(UpdateWorkflowService, 'updateDataset').and.returnValue($q.when());

				ImportService.currentInputType = importTypes[0];
				ImportService.datasetName = dataset.name;
			}));

			it('should do nothing on confirm modal dismiss', inject(($rootScope, $q, TalendConfirmService, DatasetService, ImportService) => {
				// given
				spyOn(DatasetService, 'getUniqueName').and.returnValue($q.when('my cool dataset (1)'));
				ImportService.onImportNameValidation();
				$rootScope.$apply();

				// when
				confirmDefer.reject('dismiss');
				$rootScope.$apply();

				// then
				expect(DatasetService.checkNameAvailability).toHaveBeenCalledWith(ImportService.datasetName);
				expect(TalendConfirmService.confirm).toHaveBeenCalledWith(['UPDATE_EXISTING_DATASET'], { dataset: 'my cool dataset' });
				expect(DatasetService.create).not.toHaveBeenCalled();
				expect(DatasetService.update).not.toHaveBeenCalled();
			}));

			it('should create dataset with modified name', inject(($rootScope, $q, TalendConfirmService, DatasetService, ImportService) => {
				// given
				spyOn(DatasetService, 'getUniqueName').and.returnValue($q.when('my cool dataset (1)'));
				ImportService.onImportNameValidation();
				$rootScope.$apply();

				// when
				confirmDefer.reject();
				$rootScope.$apply();
				uploadDefer.resolve({ data: 'dataset_id_XYZ' });
				$rootScope.$apply();

				// then
				expect(DatasetService.createDatasetInfo).toHaveBeenCalledWith({ name: 'my dataset.csv' }, 'my cool dataset (1)');
			}));

			it('should update existing dataset', inject(($rootScope, $q, DatasetService, ImportService, UpdateWorkflowService) => {
				// given
				spyOn(DatasetService, 'getUniqueName').and.returnValue($q.reject('my cool dataset (1)'));
				ImportService.onImportNameValidation();
				$rootScope.$apply();

				// when
				confirmDefer.resolve();
				$rootScope.$apply();
				uploadDefer.resolve();
				$rootScope.$apply();

				// then
				expect(UpdateWorkflowService.updateDataset).toHaveBeenCalledWith({ name: 'my dataset.csv' }, existingDataset);
			}));
		});
	});
});
