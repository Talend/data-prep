/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Dataset Import controller', () => {

	let ctrl;
	let createController;
	let scope;

	beforeEach(angular.mock.module('data-prep.dataset-import'));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();

		createController = () => {
			return $componentController(
				'datasetImport',
				{ $scope: scope }
			);
		};
	}));

	describe('$onChanges', () => {
		beforeEach(inject(() => {
			ctrl = createController();
		}));

		describe('with location type', () => {
			it('should get data store form', inject(($q, $timeout, ImportService) => {
				// given
				const dataStoreFormData = {};
				spyOn(ImportService, 'importParameters').and.returnValue($q.when({
					data: dataStoreFormData,
				}));

				// when
				ctrl.$onChanges({
					locationType: {
						currentValue: 'locationType',
					},
				});
				scope.$digest();
				$timeout.flush();

				// then
				expect(ctrl.datastoreForm).toBe(dataStoreFormData);
				expect(ctrl.datasetForm).toBeUndefined();
			}));

			it('should get data set form if test connection button is disabled', inject(($q, $timeout, ImportService) => {
				// given
				const dataStoreFormData = {
					properties: {
						tdp_isTestConnectionEnabled: false,
					},
				};
				const dataSetFormData = {
					properties: {},
				};
				spyOn(ImportService, 'importParameters').and.returnValue($q.when({
					data: dataStoreFormData,
				}));
				spyOn(ImportService, 'getDatasetForm').and.returnValue($q.when({
					data: dataSetFormData,
				}));

				// when
				ctrl.$onChanges({
					locationType: {
						currentValue: 'locationType',
					},
				});
				scope.$digest();
				$timeout.flush();

				// then
				expect(ctrl.datastoreForm).toBe(dataStoreFormData);
				expect(ctrl.datasetForm).toBe(dataSetFormData);
			}));
		});

		describe('with item', () => {
			it('should retrieve forms', inject(($q, $timeout, ImportService) => {
				// given
				const dataStoreFormData = {};
				const dataSetFormData = {};
				spyOn(ImportService, 'getFormsByDatasetId').and.returnValue($q.when({
					data: {
						dataStoreFormData,
						dataSetFormData,
					},
				}));

				// when
				ctrl.item = { id: 'id' };
				ctrl.$onChanges({
					item: {
						currentValue: {
							id: 'id',
						},
					},
				});
				scope.$digest();
				$timeout.flush();

				// then
				expect(ctrl.datastoreForm).toBe(dataStoreFormData);
				expect(ctrl.datasetForm).toBe(dataSetFormData);
			}));
		});
	});

	describe('onDatastoreFormChange', () => {
		let definitionName;
		let uiSpecs;
		let propertyName;
		let fakeData;

		beforeEach(inject(() => {
			ctrl = createController();
			definitionName = 'definitionName';
			propertyName = 'propertyNameWithTrigger';
			uiSpecs = {
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

		it('should refresh parameters', inject(($q, $timeout, ImportService) => {
			// given
			spyOn(ImportService, 'refreshForm').and.returnValue($q.when({ data: fakeData }));

			// when
			ctrl.onDatastoreFormChange(uiSpecs, definitionName, propertyName);
			$timeout.flush();
			scope.$digest();

			// then
			expect(ImportService.refreshForm).toHaveBeenCalledWith(propertyName, uiSpecs);
			expect(ctrl.datastoreForm).toEqual(fakeData);
		}));

		it('should not refresh parameters if promise fails', inject((ImportService, $q) => {
			// given
			spyOn(ImportService, 'refreshForm').and.returnValue($q.reject());

			// when
			ctrl.onDatastoreFormChange(uiSpecs, definitionName, propertyName);
			scope.$digest();

			// then
			expect(ImportService.refreshForm).toHaveBeenCalledWith(propertyName, uiSpecs);
			expect(ctrl.datastoreForm).not.toEqual(fakeData);
		}));
	});

	describe('onDatastoreFormSubmit', () => {
		let dataset;
		let definitionName;
		let uiSpecs;
		let fakeDatastoreForm;
		let fakeDatasetForm;
		let fakeFormsData;

		beforeEach(inject(() => {
			ctrl = createController();
			dataset = { id: 'dataSetId' };
			definitionName = 'definitionName';
			uiSpecs = {
				formData: {},
			};
			fakeDatastoreForm = {
				jsonSchema: {},
				uiSchema: {},
				properties: {},
			};
			fakeDatasetForm = {
				jsonSchema: {},
				uiSchema: {},
				properties: {},
			};
			fakeFormsData = {
				dataStoreProperties: fakeDatastoreForm.properties,
				dataSetProperties: fakeDatasetForm.properties,
			};
		}));

		it('should test connection ok', inject(($q, $timeout, ImportService, MessageService) => {
			// given
			spyOn(ImportService, 'testConnection').and.returnValue($q.when());
			spyOn(MessageService, 'success').and.returnValue($q.when());
			spyOn(ImportService, 'getDatasetForm').and.returnValue($q.when({ data: fakeDatasetForm }));

			// when
			ctrl.onDatastoreFormSubmit(uiSpecs, definitionName);
			scope.$digest();
			$timeout.flush();

			// then
			const { formData } = uiSpecs;
			expect(ImportService.testConnection).toHaveBeenCalledWith(definitionName, formData);
			expect(MessageService.success).toHaveBeenCalledWith(
				'DATASTORE_TEST_CONNECTION',
				'DATASTORE_CONNECTION_SUCCESSFUL'
			);
			expect(ImportService.getDatasetForm).toHaveBeenCalledWith(formData);
			expect(ctrl.datasetForm).toBe(fakeDatasetForm);
		}));

		it('should test connection fail', inject(($q, ImportService, MessageService) => {
			// given
			spyOn(ImportService, 'testConnection').and.returnValue($q.reject());
			spyOn(MessageService, 'success').and.returnValue();
			spyOn(ImportService, 'getDatasetForm').and.returnValue();

			// when
			ctrl.onDatastoreFormSubmit(uiSpecs, definitionName);
			scope.$digest();

			// then
			const { formData } = uiSpecs;
			expect(ImportService.testConnection).toHaveBeenCalledWith(definitionName, formData);
			expect(MessageService.success).not.toHaveBeenCalled();
			expect(ImportService.getDatasetForm).not.toHaveBeenCalled();
			expect(ctrl.datasetForm).toBeUndefined();
		}));

		it('should do nothing if datastore form without button is submitted', inject((ImportService) => {
			// given
			ctrl.submitLock = false;
			ctrl.datastoreForm = {
				properties: {
					tdp_isTestConnectionEnabled: false,
				},
			};
			spyOn(ImportService, 'testConnection').and.returnValue();
			spyOn(ImportService, 'createDataset').and.returnValue();
			spyOn(ImportService, 'editDataset').and.returnValue();
			spyOn(ImportService, 'refreshForms').and.returnValue();

			// when
			ctrl.onDatastoreFormSubmit(uiSpecs, definitionName);

			// then
			expect(ImportService.testConnection).not.toHaveBeenCalled();
			expect(ImportService.createDataset).not.toHaveBeenCalled();
			expect(ImportService.editDataset).not.toHaveBeenCalled();
			expect(ImportService.refreshForms).not.toHaveBeenCalled();
		}));

		it('should create dataset', inject(($q, DatasetService, ImportService, UploadWorkflowService) => {
			// given
			ctrl.submitLock = true;
			ctrl.locationType = definitionName;
			ctrl.datasetFormData = {};

			spyOn(ImportService, 'createDataset').and.returnValue($q.when({ data: { dataSetId: 'dataSetId' } }));
			spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when(dataset));
			spyOn(UploadWorkflowService, 'openDataset').and.returnValue();

			// when
			ctrl.onDatastoreFormSubmit(uiSpecs, definitionName);
			scope.$digest();

			// then
			expect(ImportService.createDataset).toHaveBeenCalledWith(definitionName, fakeFormsData);
			expect(UploadWorkflowService.openDataset).toHaveBeenCalledWith(dataset);
			expect(ctrl.submitLock).toBeFalsy();
		}));

		it('should edit dataset', inject(($q, ImportService, UploadWorkflowService) => {
			// given
			ctrl.submitLock = true;
			ctrl.item = dataset;
			ctrl.datasetFormData = {};

			spyOn(ImportService, 'editDataset').and.returnValue($q.when());
			spyOn(UploadWorkflowService, 'openDataset').and.returnValue();

			// when
			ctrl.onDatastoreFormSubmit(uiSpecs, definitionName);
			scope.$digest();

			// then
			expect(ImportService.editDataset).toHaveBeenCalledWith('dataSetId', fakeFormsData);
			expect(UploadWorkflowService.openDataset).toHaveBeenCalledWith(dataset);
			expect(ctrl.submitLock).toBeFalsy();
		}));

		it('should refresh dataset', inject(($q, ImportService) => {
			// given
			ctrl.submitLock = true;
			ctrl.currentPropertyName = 'currentPropertyName';
			ctrl.datasetFormData = fakeDatasetForm.properties;

			spyOn(ImportService, 'refreshForms').and.returnValue($q.when(fakeDatastoreForm.properties));

			// when
			ctrl.onDatastoreFormSubmit(uiSpecs, definitionName);
			scope.$digest();

			// then
			expect(ImportService.refreshForms).toHaveBeenCalledWith('currentPropertyName', fakeFormsData);
			expect(ctrl.currentPropertyName).toBeNull();
			expect(ctrl.submitLock).toBeFalsy();
		}));
	});

	describe('onDatasetFormChange', () => {
		let definitionName;
		let formData;
		let propertyName;

		beforeEach(inject(() => {
			ctrl = createController();
			definitionName = 'definitionName';
			propertyName = 'propertyNameWithTrigger';
			formData = {};
		}));

		it('should refresh dataset form', inject(() => {
			// given
			spyOn(ctrl, '_simulateDatastoreSubmit').and.returnValue();
			// when
			ctrl.onDatasetFormChange(formData, definitionName, propertyName);
			scope.$digest();

			// then
			expect(ctrl.currentPropertyName).toEqual(propertyName);
			expect(ctrl._simulateDatastoreSubmit).toHaveBeenCalled();
		}));
	});

	describe('onDatasetFormSubmit', () => {
		let uiSpecs;

		beforeEach(inject(() => {
			uiSpecs = {
				formData: {},
			};

			ctrl = createController();
		}));

		it('should trigger datastore form submit button', inject(($document) => {
			// given
			ctrl.submitLock = false;
			spyOn($document, 'find').and.returnValue({
				eq: () => [{
					click: () => {
					},
				}],
			});

			// when
			ctrl.onDatasetFormSubmit(uiSpecs);

			// then
			expect(ctrl.submitLock).toBeTruthy();
			expect(ctrl.datasetFormData).toEqual(uiSpecs.formData);
		}));
	});
});
