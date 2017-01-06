/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Dataset Import TCOMP controller', () => {

	let ctrl;
	let createController;
	let scope;

	beforeEach(angular.mock.module('data-prep.dataset-import'));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();

		createController = () => {
			return $componentController(
				'tcompDatasetImport',
				{ $scope: scope }
			);
		};
	}));

	// TODO
	// describe('onDatastoreFormChange', () => {
	// 	let definitionName;
	// 	let uiSpecs;
	// 	let propertyName;
	// 	let fakeData;
	//
	// 	beforeEach(inject(() => {
	// 		ctrl = createController();
	// 		definitionName = 'definitionName';
	// 		propertyName = 'propertyNameWithTrigger';
	// 		uiSpecs = {
	// 			propertyName: 'propertyValue1',
	// 		};
	// 		fakeData = {
	// 			jsonSchema: {},
	// 			uiSchema: {},
	// 			properties: {
	// 				propertyName: 'propertyValue2',
	// 			},
	// 		};
	// 	}));
	//
	// 	it('should refresh parameters', inject((ImportService, $q) => {
	// 		// given
	// 		spyOn(ImportService, 'refreshForm').and.returnValue($q.when({ data: fakeData }));
	//
	// 		// when
	// 		ctrl.onDatastoreFormChange(uiSpecs, definitionName, propertyName);
	// 		scope.$digest();
	//
	// 		// then
	// 		expect(ImportService.refreshForm).toHaveBeenCalledWith(definitionName, propertyName, uiSpecs);
	// 		expect(ctrl.datastoreForm).toEqual(fakeData);
	// 	}));
	//
	// 	it('should not refresh parameters if promise fails', inject((ImportService, $q) => {
	// 		// given
	// 		spyOn(ImportService, 'refreshForm').and.returnValue($q.reject());
	//
	// 		// when
	// 		ctrl.onDatastoreFormChange(uiSpecs, definitionName, propertyName);
	// 		scope.$digest();
	//
	// 		// then
	// 		expect(ImportService.refreshForm).toHaveBeenCalledWith(definitionName, propertyName, uiSpecs);
	// 		expect(ctrl.datastoreForm).not.toEqual(fakeData);
	// 	}));
	// });
	//
	// describe('onDatastoreFormSubmit', () => {
	// 	let definitionName;
	// 	let uiSpecs;
	// 	let fakeDatastoreId;
	// 	let fakeDatasetForm;
	//
	// 	beforeEach(inject(() => {
	// 		ctrl = createController();
	// 		definitionName = 'definitionName';
	// 		uiSpecs = {
	// 			formData: {
	// 				propertyName: 'propertyValue1',
	// 			},
	// 		};
	// 		fakeDatastoreId = 'abc-123-def';
	// 		fakeDatasetForm = {
	// 			jsonSchema: {},
	// 			uiSchema: {},
	// 		};
	// 	}));
	//
	// 	it('should get datastore id while testing connection', inject(($q, ImportService) => {
	// 		// given
	// 		spyOn(ImportService, 'testConnection').and.returnValue($q.when({ data: { dataStoreId: fakeDatastoreId } }));
	// 		spyOn(ImportService, 'getDatasetForm').and.returnValue($q.when({ data: fakeDatasetForm }));
	//
	// 		// when
	// 		ctrl.onDatastoreFormSubmit(uiSpecs, definitionName);
	// 		scope.$digest();
	//
	// 		// then
	// 		expect(ImportService.testConnection).toHaveBeenCalledWith(definitionName, uiSpecs.formData);
	// 		expect(ImportService.getDatasetForm).toHaveBeenCalledWith(fakeDatastoreId);
	// 		expect(ctrl.datasetForm).toEqual(fakeDatasetForm);
	// 	}));
	//
	// 	it('should not get datastore id if promise fails', inject(($q, ImportService) => {
	// 		// given
	// 		spyOn(ImportService, 'testConnection').and.returnValue($q.reject());
	//
	// 		// when
	// 		ctrl.onDatastoreFormSubmit(uiSpecs, definitionName);
	// 		scope.$digest();
	//
	// 		// then
	// 		expect(ImportService.testConnection).toHaveBeenCalledWith(definitionName, uiSpecs.formData);
	// 	}));
	// });
	//
	// describe('onDatasetFormChange', () => {
	// 	let definitionName;
	// 	let formData;
	// 	let propertyName;
	// 	let fakeData;
	//
	// 	beforeEach(inject(() => {
	// 		ctrl = createController();
	// 		propertyName = 'propertyNameWithTrigger';
	// 		formData = {
	// 			propertyName: 'propertyValue1',
	// 		};
	// 		fakeData = {
	// 			jsonSchema: {},
	// 			uiSchema: {},
	// 			properties: {
	// 				propertyName: 'propertyValue2',
	// 			},
	// 		};
	// 	}));
	//
	// 	it('should refresh dataset form', inject((ImportService, $q) => {
	// 		// given
	// 		spyOn(ImportService, 'refreshForm').and.returnValue($q.when({ data: fakeData }));
	//
	// 		// when
	// 		ctrl.onDatasetFormChange(formData, null, propertyName);
	// 		scope.$digest();
	//
	// 		// then
	// 		expect(ImportService.refreshForm).toHaveBeenCalledWith(definitionName, propertyName, formData);
	// 		expect(ctrl.datasetForm).toEqual(fakeData);
	// 	}));
	//
	// 	it('should not refresh dataset form if promise fails', inject((ImportService, $q) => {
	// 		// given
	// 		spyOn(ImportService, 'refreshForm').and.returnValue($q.reject());
	//
	// 		// when
	// 		ctrl.onDatasetFormChange(formData, null, propertyName);
	// 		scope.$digest();
	//
	// 		// then
	// 		expect(ImportService.refreshForm).toHaveBeenCalledWith(definitionName, propertyName, formData);
	// 		expect(ctrl.datasetForm).not.toEqual(fakeData);
	// 	}));
	// });
	//
	// describe('onDatasetFormCancel', () => {
	//
	// 	it('should reset modal display flag and datastore creation form', inject(() => {
	// 		// given
	// 		ctrl = createController();
	// 		ctrl.showModal = true;
	// 		ctrl.datastoreForm = {};
	// 		ctrl.datasetForm = {};
	//
	// 		// when
	// 		ctrl.onDatasetFormCancel();
	// 		scope.$digest();
	//
	// 		// then
	// 		expect(ctrl.showModal).toBeFalsy();
	// 		expect(ctrl.datastoreForm).toBeNull();
	// 		expect(ctrl.datasetForm).toBeNull();
	// 	}));
	// });
	//
	// describe('onDatasetFormSubmit', () => {
	// 	let uiSpecs;
	// 	let fakeDatasetId;
	//
	// 	beforeEach(inject(() => {
	// 		ctrl = createController();
	// 		uiSpecs = {
	// 			formData: {
	// 				propertyName: 'propertyValue1',
	// 			},
	// 		};
	// 		fakeDatasetId = 'abc-123-def';
	// 	}));
	//
	// 	it('should open dataset', inject(($q, ImportService, DatasetService, UploadWorkflowService) => {
	// 		// given
	// 		spyOn(ImportService, 'createDataset').and.returnValue($q.when({ data: { dataSetId: fakeDatasetId } }));
	// 		spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when());
	// 		spyOn(UploadWorkflowService, 'openDataset');
	//
	// 		// when
	// 		ctrl.onDatasetFormSubmit(uiSpecs, dataStoreId);
	// 		scope.$digest();
	//
	// 		// then
	// 		expect(ImportService.createDataset).toHaveBeenCalledWith(dataStoreId, uiSpecs.formData);
	// 		expect(DatasetService.getDatasetById).toHaveBeenCalledWith(fakeDatasetId);
	// 	}));
	//
	// 	it('should not open dataset if promise fails', inject(($q, ImportService, DatasetService) => {
	// 		// given
	// 		spyOn(ImportService, 'createDataset').and.returnValue($q.reject());
	// 		spyOn(DatasetService, 'getDatasetById').and.returnValue();
	//
	// 		// when
	// 		ctrl.onDatasetFormSubmit(uiSpecs, dataStoreId);
	// 		scope.$digest();
	//
	// 		// then
	// 		expect(ImportService.createDataset).toHaveBeenCalledWith(dataStoreId, uiSpecs.formData);
	// 		expect(DatasetService.getDatasetById).not.toHaveBeenCalled();
	// 	}));
	// });
});
