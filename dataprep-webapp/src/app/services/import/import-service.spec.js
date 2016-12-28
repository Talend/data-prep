/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Import service', () => {
	'use strict';

	const importTypes = [
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

	const adaptImportTypes = [
		{
			defaultImport: false,
			label: 'From HDFS',
			model: {
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
			}
		},
		{
			defaultImport: false,
			label: 'From HTTP',
			model: {
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
			}
		},
		{
			defaultImport: false,
			label: 'Local File',
			model: {
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
			}
		},
		{
			defaultImport: false,
			label: 'From Talend Job',
			model: {
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
			}
		},
		{
			defaultImport: false,
			label: 'From TCOMP example',
			model: {
				contentType: 'application/vnd.tcomp-ds.FullExampleDatastore',
				defaultImport: false,
				dynamic: true,
				label: 'From TCOMP example',
				locationType: 'tcomp-FullExampleDatastore',
				parameters: [],
				title: 'Add a TCOMP dataset',
			}
		},
	];
	let StateMock;
	const dataset = { id: 'ec4834d9bc2af8', name: 'Customers (50 lines)', draft: false };

	beforeEach(angular.mock.module('data-prep.services.import', ($provide) => {
		StateMock = {
			inventory: {
				currentFolder: { id: '', path: '', name: 'Home' },
				currentFolderContent: {
					folders: [],
					datasets: [],
				},
			},
			import: {
				importTypes: adaptImportTypes,
			},
		};
		$provide.constant('state', StateMock);
	}));
	describe('initImport', () => {
		it('should fetch import types list from REST call',
			inject(($rootScope, $q, ImportService, ImportRestService, StateService) => {
				//given
				spyOn(ImportRestService, 'importTypes').and.returnValue($q.when({ data: importTypes }));
				spyOn(StateService, 'setImportTypes').and.returnValue();

				//when
				ImportService.initImport();
				$rootScope.$digest();

				//then
				expect(StateService.setImportTypes).toHaveBeenCalledWith(adaptImportTypes);
			})
		);
	});

	describe('importParameters', () => {
		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'importParameters').and.returnValue($q.when());

		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			//given
			const locationType = 'toto';

			//when
			ImportService.importParameters(locationType);

			//then
			expect(ImportRestService.importParameters).toHaveBeenCalledWith(locationType);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			//given
			const locationType = 'toto';
			spyOn($rootScope, '$emit').and.returnValue();

			//when
			ImportService.importParameters(locationType);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			//then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');

		}));
	});

	describe('refreshParameters', () => {
		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'refreshParameters').and.returnValue($q.when());

		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			//given
			const formId = 'toto';
			const propertyName = 'tata';
			const formData = {};

			//when
			ImportService.refreshParameters(formId, propertyName, formData);

			//then
			expect(ImportRestService.refreshParameters).toHaveBeenCalledWith(formId, propertyName, formData);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			//given
			const formId = 'toto';
			const propertyName = 'tata';
			const formData = {};
			spyOn($rootScope, '$emit').and.returnValue();

			//when
			ImportService.refreshParameters(formId, propertyName, formData);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			//then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');

		}));
	});

	describe('testConnection', () => {
		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'testConnection').and.returnValue($q.when());

		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			//given
			const formId = 'toto';
			const formData = {};

			//when
			ImportService.testConnection(formId, formData);

			//then
			expect(ImportRestService.testConnection).toHaveBeenCalledWith(formId, formData);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			//given
			const formId = 'toto';
			const formData = {};
			spyOn($rootScope, '$emit').and.returnValue();

			//when
			ImportService.testConnection(formId, formData);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			//then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');

		}));
	});

	describe('getDatasetForm', () => {
		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'getDatasetForm').and.returnValue($q.when());

		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			//given
			const datastoreId = 'toto';

			//when
			ImportService.getDatasetForm(datastoreId);

			//then
			expect(ImportRestService.getDatasetForm).toHaveBeenCalledWith(datastoreId);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			//given
			const datastoreId = 'toto';
			spyOn($rootScope, '$emit').and.returnValue();

			//when
			ImportService.getDatasetForm(datastoreId);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			//then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');

		}));
	});

	describe('refreshDatasetForm', () => {
		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'refreshDatasetForm').and.returnValue($q.when());

		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			//given
			const datastoreId = 'toto';
			const propertyName = 'tata';
			const formData = {};

			//when
			ImportService.refreshDatasetForm(datastoreId, propertyName, formData);

			//then
			expect(ImportRestService.refreshDatasetForm).toHaveBeenCalledWith(datastoreId, propertyName, formData);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			//given
			const datastoreId = 'toto';
			const propertyName = 'tata';
			const formData = {};
			spyOn($rootScope, '$emit').and.returnValue();

			//when
			ImportService.refreshDatasetForm(datastoreId, propertyName, formData);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			//then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');

		}));
	});

	describe('createDataset', () => {
		beforeEach(inject(($q, ImportRestService) => {
			spyOn(ImportRestService, 'createDataset').and.returnValue($q.when());

		}));

		it('should call REST service', inject((ImportService, ImportRestService) => {
			//given
			const datastoreId = 'toto';
			const formData = {};

			//when
			ImportService.createDataset(datastoreId, formData);

			//then
			expect(ImportRestService.createDataset).toHaveBeenCalledWith(datastoreId, formData);
		}));

		it('should manage loader', inject(($rootScope, ImportService) => {
			//given
			const datastoreId = 'toto';
			const formData = {};
			spyOn($rootScope, '$emit').and.returnValue();

			//when
			ImportService.createDataset(datastoreId, formData);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$rootScope.$digest();

			//then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');

		}));
	});


	describe('startDefaultImport', inject(() => {
		it('should call the first import type if no defaultImportType', inject(() => {
			// given
			StateMock.import.importTypes[2].defaultImport = false;

			// when
			spyOn(ImportService, 'startImport');
			ImportService.startDefaultImport();

			// then
			expect(ImportService.startImport).toHaveBeenCalledWith(StateMock.import.importTypes[0].model);
		}));

		it('should call the default import type', inject((ImportService) => {

			// given
			StateMock.import.importTypes[2].defaultImport = true;

			// when
			spyOn(ImportService, 'startImport');
			ImportService.startDefaultImport();

			// then
			expect(ImportService.startImport).toHaveBeenCalledWith(StateMock.import.importTypes[2].model);
		}));
	}));

	describe('startImport', () => {
		it('should start import from local file', inject((ImportService) => {
			// when
			ImportService.startImport(StateMock.import.importTypes[2].model);

			// then
			expect(ImportService.currentInputType).toEqual(StateMock.import.importTypes[2].model);
		}));

		it('should start import from remote', inject((ImportService, StateService) => {
			// when
			spyOn(StateService, 'setShowImportModal');
			ImportService.startImport(StateMock.import.importTypes[0].model);

			// then
			expect(ImportService.currentInputType).toEqual(StateMock.import.importTypes[0].model);
			expect(StateService.setShowImportModal).toBe(true);
		}));

		it('should start import from remote with dynamic parameters', inject((ImportService, $q, $rootScope) => {
			// given
			StateMock.import.importTypes[0].model.dynamic = true;
			spyOn(ImportService, 'importParameters').and.returnValue($q.when({ data: { name: 'url' } }));

			// when
			ImportService.startImport(StateMock.import.importTypes[0].model);
			$rootScope.$apply();

			// then
			expect(ImportService.importParameters).toHaveBeenCalledWith('hdfs');
			expect(ImportService.currentInputType.parameters).toEqual({ name: 'url' });
		}));

		it('should start import from tcomp', inject(($rootScope, ImportService, $q) => {
			// given
			const fakeData = { jsonSchema: {} };
			spyOn(ImportService, 'importParameters').and.returnValue($q.when({ data: fakeData }));

			// when
			ImportService.startImport(StateMock.import.importTypes[4].model);
			$rootScope.$apply();

			// then
			expect(ImportService.importParameters).toHaveBeenCalledWith('tcomp-FullExampleDatastore');
			expect(ImportService.datastoreForm).toEqual(fakeData);
		}));
	});

	describe('onDatastoreFormChange', () => {
		let formData;
		let formId;
		let propertyName;
		let fakeData;

		beforeEach(inject(() => {
			formId = 'formId';
			propertyName = 'propertyNameWithTrigger';
			formData = {
				propertyName: 'propertyValue1',
			};
			fakeData = {
				jsonSchema: {},
				uiSchema: {},
				properties: {
					propertyName: 'propertyValue2',
				},
			};
		}));

		it('should refresh parameters', inject(($rootScope, ImportService, $q) => {
			// given
			spyOn(ImportService, 'refreshParameters').and.returnValue($q.when({ data: fakeData }));

			// when
			ImportService.onDatastoreFormChange(formData, formId, propertyName);
			$rootScope.$apply();

			// then
			expect(ImportService.refreshParameters).toHaveBeenCalledWith(formId, propertyName, formData);
			expect(ImportService.datastoreForm).toEqual(fakeData);
		}));

		it('should not refresh parameters if promise fails', inject(($rootScope, ImportService, $q) => {
			// given
			spyOn(ImportService, 'refreshParameters').and.returnValue($q.reject());

			// when
			ImportService.onDatastoreFormChange(formData, formId, propertyName);
			$rootScope.$apply();

			// then
			expect(ImportService.refreshParameters).toHaveBeenCalledWith(formId, propertyName, formData);
			expect(ImportService.datastoreForm).not.toEqual(fakeData);
		}));
	});

	describe('onDatastoreFormSubmit', () => {
		let definitionName;
		let uiSpecs;
		let fakeDatastoreId;
		let fakeDatasetForm;

		beforeEach(inject(() => {
			definitionName = 'formId';
			uiSpecs = {
				formData: {
					propertyName: 'propertyValue1',
				},
			};
			fakeDatastoreId = 'abc-123-def';
			fakeDatasetForm = {
				jsonSchema: {},
				uiSchema: {},
			};
		}));

		it('should get datastore id while testing connection', inject(($rootScope, $q, ImportService) => {
			// given
			spyOn(ImportService, 'testConnection').and.returnValue($q.when({ data: { dataStoreId: fakeDatastoreId } }));
			spyOn(ImportService, 'getDatasetForm').and.returnValue($q.when({ data: fakeDatasetForm }));

			// when
			ImportService.onDatastoreFormSubmit(uiSpecs, definitionName);
			$rootScope.$apply();

			// then
			expect(ImportService.testConnection).toHaveBeenCalledWith(definitionName, uiSpecs.formData);
			expect(ImportService.dataStoreId).toEqual(fakeDatastoreId);
			expect(ImportService.getDatasetForm).toHaveBeenCalledWith(fakeDatastoreId);
			expect(ImportService.datasetForm).toEqual(fakeDatasetForm);
		}));

		it('should not get datastore id if promise fails', inject(($rootScope, $q, ImportService) => {
			// given
			spyOn(ImportService, 'testConnection').and.returnValue($q.reject());

			// when
			ImportService.onDatastoreFormSubmit(uiSpecs, definitionName);
			$rootScope.$apply();

			// then
			expect(ImportService.testConnection).toHaveBeenCalledWith(definitionName, uiSpecs.formData);
			expect(ImportService.dataStoreId).toBeFalsy();
		}));
	});

	describe('onDatasetFormChange', () => {
		let formData;
		let datastoreFormId;
		let propertyName;
		let fakeData;

		beforeEach(inject(() => {
			datastoreFormId = 'datastoreFormId';
			propertyName = 'propertyNameWithTrigger';
			formData = {
				propertyName: 'propertyValue1',
			};
			fakeData = {
				jsonSchema: {},
				uiSchema: {},
				properties: {
					propertyName: 'propertyValue2',
				},
			};
			ImportService.dataStoreId = datastoreFormId;
		}));

		it('should refresh dataset form', inject(($rootScope, ImportService, $q) => {
			// given
			spyOn(ImportService, 'refreshDatasetForm').and.returnValue($q.when({ data: fakeData }));

			// when
			ImportService.onDatasetFormChange(formData, null, propertyName);
			$rootScope.$apply();

			// then
			expect(ImportService.refreshDatasetForm).toHaveBeenCalledWith(datastoreFormId, propertyName, formData);
			expect(ImportService.datasetForm).toEqual(fakeData);
		}));

		it('should not refresh dataset form if promise fails', inject(($rootScope, ImportService, $q) => {
			// given
			spyOn(ImportService, 'refreshDatasetForm').and.returnValue($q.reject());

			// when
			ImportService.onDatasetFormChange(formData, null, propertyName);
			$rootScope.$apply();

			// then
			expect(ImportService.refreshDatasetForm).toHaveBeenCalledWith(datastoreFormId, propertyName, formData);
			expect(ImportService.datasetForm).not.toEqual(fakeData);
		}));
	});

	describe('onDatasetFormCancel', () => {

		it('should reset modal display flag and datastore creation form', inject(($rootScope, ImportService) => {
			// given
			ImportService.state.import.showImportModal = true;
			ImportService.datastoreForm = {};
			ImportService.dataStoreId = '';
			ImportService.datasetForm = {};

			// when
			ImportService.onDatasetFormCancel();
			$rootScope.$apply();

			// then
			expect(ImportService.state.import.showImportModal).toBeFalsy();
			expect(ImportService.datastoreForm).toBeNull();
			expect(ImportService.dataStoreId).toBeNull();
			expect(ImportService.datasetForm).toBeNull();
		}));
	});

	describe('onDatasetFormSubmit', () => {
		let dataStoreId;
		let uiSpecs;
		let fakeDatasetId;

		beforeEach(inject((ImportService) => {
			dataStoreId = 'datastoreId';
			uiSpecs = {
				formData: {
					propertyName: 'propertyValue1',
				},
			};
			fakeDatasetId = 'abc-123-def';
			ImportService.dataStoreId = dataStoreId;
		}));

		it('should open dataset', inject(($rootScope, $q, ImportService, DatasetService, UploadWorkflowService) => {
			// given
			spyOn(ImportService, 'createDataset').and.returnValue($q.when({ data: { dataSetId: fakeDatasetId } }));
			spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when());
			spyOn(UploadWorkflowService, 'openDataset');

			// when
			ImportService.onDatasetFormSubmit(uiSpecs, dataStoreId);
			$rootScope.$apply();

			// then
			expect(ImportService.createDataset).toHaveBeenCalledWith(dataStoreId, uiSpecs.formData);
			expect(DatasetService.getDatasetById).toHaveBeenCalledWith(fakeDatasetId);
		}));

		it('should not open dataset if promise fails', inject(($rootScope, $q, ImportService, DatasetService) => {
			// given
			spyOn(ImportService, 'createDataset').and.returnValue($q.reject());
			spyOn(DatasetService, 'getDatasetById').and.returnValue();

			// when
			ImportService.onDatasetFormSubmit(uiSpecs, dataStoreId);
			$rootScope.$apply();

			// then
			expect(ImportService.createDataset).toHaveBeenCalledWith(dataStoreId, uiSpecs.formData);
			expect(DatasetService.getDatasetById).not.toHaveBeenCalled();
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

			ImportService.currentInputType = StateMock.import.importTypes[0];
		}));

		it('should show dataset name popup when name already exists', inject(($rootScope, $q, DatasetService, ImportService) => {
			// given
			const dataset = {
				name: 'my dataset',
			};
			spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.reject());
			expect(ImportService.datasetNameModal).toBeFalsy();

			// when
			ImportService.import(StateMock.import.importTypes[0].model);
			$rootScope.$apply();

			// then
			expect(ImportService.datasetNameModal).toBe(true);
		}));

		it('should create dataset if unique', inject(($rootScope, $q, DatasetService, ImportService) => {
			// given
			spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.when());
			expect(ImportService.datasetNameModal).toBeFalsy();

			// when
			ImportService.import(StateMock.import.importTypes[0].model);
			$rootScope.$apply();

			// then
			expect(ImportService.datasetNameModal).toBeFalsy();
			const paramsExpected = { name: 'my dataset', url: '', type: 'hdfs' };
			expect(DatasetService.create).toHaveBeenCalledWith(paramsExpected, 'application/vnd.remote-ds.hdfs', { name: 'my dataset.csv' });
		}));

		it('should close modal if import is successful', inject(($rootScope, $q, DatasetService, ImportService, StateService) => {
			// given
			spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.when());
			spyOn(StateService, 'setShowImportModal');
			expect(ImportService.datasetNameModal).toBeFalsy();

			// when
			ImportService.import(StateMock.import.importTypes[0].model);
			$rootScope.$apply();

			// then
			expect(StateService.setShowImportModal).toHaveBeenCalledWith(false);
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
				ImportService.currentInputType = StateMock.import.importTypes[0].model;
			}));

			it('should create dataset if name is unique', inject((StateService, $q, $rootScope, DatasetService, ImportService, UploadWorkflowService) => {
				// given
				const paramsExpected = { name: 'my cool dataset', url: '', type: 'hdfs' };

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

				ImportService.currentInputType = StateMock.import.importTypes[0].model;
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
				expect(TalendConfirmService.confirm).toHaveBeenCalledWith(null, ['UPDATE_EXISTING_DATASET'], { dataset: 'my cool dataset' });
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
