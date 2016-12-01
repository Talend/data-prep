/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Import REST Service', () => {
	let $httpBackend;
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

	beforeEach(inject(($injector) => {
		$httpBackend = $injector.get('$httpBackend');
	}));

	it('should get all import types', inject(($rootScope, RestURLs, ImportRestService) => {
		// given
		let types = null;
		$httpBackend
			.expectGET(`${RestURLs.exportUrl}/imports`)
			.respond(200, importTypes);

		// when
		ImportRestService.importTypes()
			.then((response) => {
				types = response.data;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		// then
		expect(types).toEqual(importTypes);
	}));

	it('should get all import parameters', inject(($rootScope, RestURLs, ImportRestService) => {
		// given
		let params = null;
		$httpBackend
			.expectGET(`${RestURLs.exportUrl}/imports/http/parameters`)
			.respond(200, { name: 'url' });

		// when
		ImportRestService.importParameters('http')
			.then((response) => {
				params = response.data;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		// then
		expect(params).toEqual({ name: 'url' });
	}));

	it('should refresh import parameters', inject(($rootScope, RestURLs, ImportRestService) => {
		// given
		let params = null;

		const formId = 'formId';
		const propertyName = 'propertyName';
		const formData = { propertyName: 'abc' };
		const expectedResult = { jsonSchema: {}, uiSchema: {} };

		$httpBackend
			.expectPOST(`${RestURLs.tcompUrl}/properties/${formId}/after/${propertyName}`, formData)
			.respond(200, expectedResult);

		// when
		ImportRestService.refreshParameters(formId, propertyName, formData)
			.then((response) => {
				params = response.data;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		// then
		expect(params).toEqual(expectedResult);
	}));

	it('should test connection', inject(($rootScope, RestURLs, ImportRestService) => {
		// given
		const formId = 'formId';
		const formData = { propertyName: 'abc' };
		const expectedResult = 'abc-123-def';

		$httpBackend
			.expectPOST(`${RestURLs.tcompUrl}/datastores/${formId}`, formData)
			.respond(200, { dataStoreId: expectedResult });

		// when
		let dataStoreId = null;
		ImportRestService.testConnection(formId, formData)
			.then((response) => {
				dataStoreId = response.data && response.data.dataStoreId;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		// then
		expect(dataStoreId).toBe(expectedResult);
	}));

	it('should get dataset form properties', inject(($rootScope, RestURLs, ImportRestService) => {
		// given
		const datastoreId = 'abc-123-def';
		const expectedResult = { jsonSchema: {}, uiSchema: {} };

		$httpBackend
			.expectGET(`${RestURLs.tcompUrl}/datastores/${datastoreId}/dataset/properties`)
			.respond(200, expectedResult);

		// when
		let datasetForm = null;
		ImportRestService.getDatasetForm(datastoreId)
			.then((response) => {
				datasetForm = response.data;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		// then
		expect(datasetForm).toEqual(expectedResult);
	}));

	it('should refresh dataset form properties', inject(($rootScope, RestURLs, ImportRestService) => {
		// given
		const datastoreId = 'abc-123-def';
		const propertyName = 'propertyName';
		const formData = { propertyName: 'abc' };
		const expectedResult = { jsonSchema: {}, uiSchema: {} };

		$httpBackend
			.expectPOST(`${RestURLs.tcompUrl}/datastores/${datastoreId}/after/${propertyName}`, formData)
			.respond(200, expectedResult);

		// when
		let datasetForm = null;
		ImportRestService.refreshDatasetForm(datastoreId, propertyName, formData)
			.then((response) => {
				datasetForm = response.data;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		// then
		expect(datasetForm).toEqual(expectedResult);
	}));

	it('should create dataset', inject(($rootScope, RestURLs, ImportRestService) => {
		// given
		const dataStoreId = 'formId';
		const formData = { propertyName: 'abc' };
		const expectedResult = 'abc-123-def';

		$httpBackend
			.expectPOST(`${RestURLs.tcompUrl}/datastores/${dataStoreId}/dataset`, formData)
			.respond(200, { dataSetId: expectedResult });

		// when
		let dataSetId = null;
		ImportRestService.createDataset(dataStoreId, formData)
			.then((response) => {
				dataSetId = response.data && response.data.dataSetId;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		// then
		expect(dataSetId).toBe(expectedResult);
	}));
});
