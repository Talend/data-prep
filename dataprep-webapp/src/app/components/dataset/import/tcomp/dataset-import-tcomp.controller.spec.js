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

	describe('onDatastoreFormChange', () => {
		let formData;
		let formId;
		let propertyName;
		let fakeData;

		beforeEach(inject(() => {
			ctrl = createController();
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

		it('should refresh parameters', inject((ImportService, $q) => {
			// given
			spyOn(ImportService, 'refreshDatastoreForm').and.returnValue($q.when({ data: fakeData }));

			// when
			ctrl.onDatastoreFormChange(formData, formId, propertyName);
			scope.$digest();

			// then
			expect(ImportService.refreshDatastoreForm).toHaveBeenCalledWith(formId, propertyName, formData);
			expect(ctrl.datastoreForm).toEqual(fakeData);
		}));

		it('should not refresh parameters if promise fails', inject((ImportService, $q) => {
			// given
			spyOn(ImportService, 'refreshDatastoreForm').and.returnValue($q.reject());

			// when
			ctrl.onDatastoreFormChange(formData, formId, propertyName);
			scope.$digest();

			// then
			expect(ImportService.refreshDatastoreForm).toHaveBeenCalledWith(formId, propertyName, formData);
			expect(ctrl.datastoreForm).not.toEqual(fakeData);
		}));
	});

	describe('onDatastoreFormSubmit', () => {
		let definitionName;
		let uiSpecs;
		let fakeDatastoreId;
		let fakeDatasetForm;

		beforeEach(inject(() => {
			ctrl = createController();
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

		it('should get datastore id while testing connection', inject(($q, ImportService) => {
			// given
			spyOn(ImportService, 'testConnection').and.returnValue($q.when({ data: { dataStoreId: fakeDatastoreId } }));
			spyOn(ImportService, 'getDatasetForm').and.returnValue($q.when({ data: fakeDatasetForm }));

			// when
			ctrl.onDatastoreFormSubmit(uiSpecs, definitionName);
			scope.$digest();

			// then
			expect(ImportService.testConnection).toHaveBeenCalledWith(definitionName, uiSpecs.formData);
			expect(ctrl.dataStoreId).toEqual(fakeDatastoreId);
			expect(ImportService.getDatasetForm).toHaveBeenCalledWith(fakeDatastoreId);
			expect(ctrl.datasetForm).toEqual(fakeDatasetForm);
		}));

		it('should not get datastore id if promise fails', inject(($q, ImportService) => {
			// given
			spyOn(ImportService, 'testConnection').and.returnValue($q.reject());

			// when
			ctrl.onDatastoreFormSubmit(uiSpecs, definitionName);
			scope.$digest();

			// then
			expect(ImportService.testConnection).toHaveBeenCalledWith(definitionName, uiSpecs.formData);
			expect(ctrl.dataStoreId).toBeUndefined();
		}));
	});

	describe('onDatasetFormChange', () => {
		let formData;
		let datastoreFormId;
		let propertyName;
		let fakeData;

		beforeEach(inject(() => {
			ctrl = createController();
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
			ctrl.dataStoreId = datastoreFormId;
		}));

		it('should refresh dataset form', inject((ImportService, $q) => {
			// given
			spyOn(ImportService, 'refreshDatasetForm').and.returnValue($q.when({ data: fakeData }));

			// when
			ctrl.onDatasetFormChange(formData, null, propertyName);
			scope.$digest();

			// then
			expect(ImportService.refreshDatasetForm).toHaveBeenCalledWith(datastoreFormId, propertyName, formData);
			expect(ctrl.datasetForm).toEqual(fakeData);
		}));

		it('should not refresh dataset form if promise fails', inject((ImportService, $q) => {
			// given
			spyOn(ImportService, 'refreshDatasetForm').and.returnValue($q.reject());

			// when
			ctrl.onDatasetFormChange(formData, null, propertyName);
			scope.$digest();

			// then
			expect(ImportService.refreshDatasetForm).toHaveBeenCalledWith(datastoreFormId, propertyName, formData);
			expect(ctrl.datasetForm).not.toEqual(fakeData);
		}));
	});

	describe('onDatasetFormCancel', () => {

		it('should reset modal display flag and datastore creation form', inject(() => {
			// given
			ctrl = createController();
			ctrl.showModal = true;
			ctrl.datastoreForm = {};
			ctrl.dataStoreId = '';
			ctrl.datasetForm = {};

			// when
			ctrl.onDatasetFormCancel();
			scope.$digest();

			// then
			expect(ctrl.showModal).toBeFalsy();
			expect(ctrl.datastoreForm).toBeNull();
			expect(ctrl.dataStoreId).toBeNull();
			expect(ctrl.datasetForm).toBeNull();
		}));
	});

	describe('onDatasetFormSubmit', () => {
		let dataStoreId;
		let uiSpecs;
		let fakeDatasetId;

		beforeEach(inject(() => {
			ctrl = createController();
			dataStoreId = 'datastoreId';
			uiSpecs = {
				formData: {
					propertyName: 'propertyValue1',
				},
			};
			fakeDatasetId = 'abc-123-def';
			ctrl.dataStoreId = dataStoreId;
		}));

		it('should open dataset', inject(($q, ImportService, DatasetService, UploadWorkflowService) => {
			// given
			spyOn(ImportService, 'createDataset').and.returnValue($q.when({ data: { dataSetId: fakeDatasetId } }));
			spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when());
			spyOn(UploadWorkflowService, 'openDataset');

			// when
			ctrl.onDatasetFormSubmit(uiSpecs, dataStoreId);
			scope.$digest();

			// then
			expect(ImportService.createDataset).toHaveBeenCalledWith(dataStoreId, uiSpecs.formData);
			expect(DatasetService.getDatasetById).toHaveBeenCalledWith(fakeDatasetId);
		}));

		it('should not open dataset if promise fails', inject(($q, ImportService, DatasetService) => {
			// given
			spyOn(ImportService, 'createDataset').and.returnValue($q.reject());
			spyOn(DatasetService, 'getDatasetById').and.returnValue();

			// when
			ctrl.onDatasetFormSubmit(uiSpecs, dataStoreId);
			scope.$digest();

			// then
			expect(ImportService.createDataset).toHaveBeenCalledWith(dataStoreId, uiSpecs.formData);
			expect(DatasetService.getDatasetById).not.toHaveBeenCalled();
		}));
	});
});
