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

	beforeEach(angular.mock.module('data-prep.services.import'));

	beforeEach(inject(($injector) => {
		$httpBackend = $injector.get('$httpBackend');
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

	it('should get dataset form properties', inject(($rootScope, RestURLs, ImportRestService) => {
		// given
		const datastoreFormData = {};
		const expectedResult = { jsonSchema: {}, uiSchema: {}, properties: {} };

		$httpBackend
			.expectPOST(`${RestURLs.tcompUrl}/datastores/dataset/properties`)
			.respond(200, expectedResult);

		// when
		let forms = null;
		ImportRestService.getDatasetForm(datastoreFormData)
			.then((response) => {
				forms = response.data;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		// then
		expect(forms).toEqual(expectedResult);
	}));

	it('should refresh datastore form properties', inject(($rootScope, RestURLs, ImportRestService) => {
		// given
		let params = null;

		const definitionName = 'definitionName';
		const propertyName = 'propertyName';
		const formData = { propertyName: 'abc' };
		const expectedResult = { jsonSchema: {}, uiSchema: {} };

		$httpBackend
			.expectPOST(`${RestURLs.tcompUrl}/properties/${definitionName}/after/${propertyName}`, formData)
			.respond(200, expectedResult);

		// when
		ImportRestService.refreshForm(definitionName, propertyName, formData)
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
		const definitionName = 'definitionName';
		const datastoreFormData = { propertyName: 'abc' };

		$httpBackend
			.expectPOST(`${RestURLs.tcompUrl}/datastores/${definitionName}/test`, datastoreFormData)
			.respond(200);

		// when
		ImportRestService.testConnection(definitionName, datastoreFormData);
		$httpBackend.flush();
		$rootScope.$digest();

		// then
		// Expect http call to be performed. If not an error is thrown by $httpBackend
	}));

	it('should refresh dataset form properties', inject(($rootScope, RestURLs, ImportRestService) => {
		// given
		const propertyName = 'propertyName';
		const definitionName = 'definitionName';
		const formData = { propertyName: 'abc' };
		const expectedResult = { jsonSchema: {}, uiSchema: {}, properties: {} };

		$httpBackend
			.expectPOST(`${RestURLs.tcompUrl}/properties/${definitionName}/after/${propertyName}`, formData)
			.respond(200, expectedResult);

		// when
		let datasetForm = null;
		ImportRestService.refreshForm(definitionName, propertyName, formData)
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
		const definitionName = 'definitionName';
		const formsData = {
			dataStoreProperties: {},
			dataSetProperties: {},
		};

		$httpBackend
			.expectPOST(`${RestURLs.tcompUrl}/datastores/${definitionName}/dataset`, formsData)
			.respond(200);

		// when
		ImportRestService.createDataset(definitionName, formsData);
		$httpBackend.flush();
		$rootScope.$digest();

		// then
		// Expect http call to be performed. If not an error is thrown by $httpBackend
	}));

	// TODO
	// it('should get datastore form by dataset id', inject(($rootScope, RestURLs, ImportRestService) => {
	// 	// given
	// 	const datasetId = '123-abc-456';
	// 	const expectedResult = {
	// 		datastoreForm: { jsonSchema: {}, uiSchema: {}, properties: {} },
	// 		datasetForm: { jsonSchema: {}, uiSchema: {}, properties: {} },
	// 	};
	//
	// 	$httpBackend
	// 		.expectGET(`${RestURLs.tcompUrl}/datasets/${datasetId}/datastore/properties`)
	// 		.respond(200, expectedResult);
	//
	// 	// when
	// 	let datastoreForm = null;
	// 	ImportRestService.getDatastoreFormByDatasetId(datasetId)
	// 		.then((response) => {
	// 			datastoreForm = response.data;
	// 		});
	// 	$httpBackend.flush();
	// 	$rootScope.$digest();
	//
	// 	// then
	// 	expect(datastoreForm).toEqual(expectedResult);
	// }));

	// TODO
	// it('should get dataset form by dataset id', inject(($rootScope, RestURLs, ImportRestService) => {
	// 	// given
	// 	const datasetId = '123-abc-456';
	// 	const expectedResult = {
	// 		datastoreForm: { jsonSchema: {}, uiSchema: {}, properties: {} },
	// 		datasetForm: { jsonSchema: {}, uiSchema: {}, properties: {} },
	// 	};
	//
	// 	$httpBackend
	// 		.expectGET(`${RestURLs.tcompUrl}/datasets/${datasetId}/properties`)
	// 		.respond(200, expectedResult);
	//
	// 	// when
	// 	let datastoreForm = null;
	// 	ImportRestService.getDatasetFormByDatasetId(datasetId)
	// 		.then((response) => {
	// 			datastoreForm = response.data;
	// 		});
	// 	$httpBackend.flush();
	// 	$rootScope.$digest();
	//
	// 	// then
	// 	expect(datastoreForm).toEqual(expectedResult);
	// }));

	// TODO
	// it('should edit dataset properties', inject(($rootScope, RestURLs, ImportRestService) => {
	// 	// given
	// 	const datasetId = '123-abc-456';
	// 	const formData = {
	// 		datastoreForm: { jsonSchema: {}, uiSchema: {}, properties: {} },
	// 		datasetForm: { jsonSchema: {}, uiSchema: {}, properties: {} },
	// 	};
	//
	// 	$httpBackend
	// 		.expectPOST(`${RestURLs.tcompUrl}/datasets/${datasetId}`)
	// 		.respond(200);
	//
	// 	// when
	// 	ImportRestService.editDataset(datasetId, formData);
	// 	$httpBackend.flush();
	// 	$rootScope.$digest();
	//
	// 	// then
	// 	// Expect http call to be performed. If not an error is thrown by $httpBackend
	// }));
});
