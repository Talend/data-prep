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
		}, {
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
	];

	beforeEach(angular.mock.module('data-prep.services.import'));

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
				expect(StateService.setImportTypes).toHaveBeenCalledWith(importTypes);
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
});
