/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Dataset Import Live controller', () => {

	let ctrl;
	let createController;
	let scope;

	beforeEach(angular.mock.module('data-prep.dataset-import'));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();

		createController = () => {
			return $componentController(
				'liveDatasetImport',
				{ $scope: scope }
			);
		};
	}));

	describe('$onChanges', () => {
		beforeEach(inject(() => {
			ctrl = createController();
		}));

		describe('without item', () => {
			it('should get data store form', inject(($q, $timeout, ImportService) => {
				// given
				const dataStoreFormData = {};
				spyOn(ImportService, 'importParameters').and.returnValue($q.when({
					data: dataStoreFormData,
				}));

				// when
				ctrl.$onChanges();
				scope.$digest();
				$timeout.flush();

				// then
				expect(ctrl.datastoreForm).toBe(dataStoreFormData);
				expect(ctrl.datasetForm).toBeUndefined();
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

		}));

		it('should test connection fail', inject(($q, ImportService, MessageService) => {

		}));

		it('should create dataset', inject(($q, DatasetService, ImportService, UploadWorkflowService) => {

		}));

		it('should edit dataset', inject(($q, ImportService, UploadWorkflowService) => {

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
	});
});
