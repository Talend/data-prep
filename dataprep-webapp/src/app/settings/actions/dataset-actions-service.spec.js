/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

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
];

describe('Datasets actions service', () => {
	let stateMock;

	beforeEach(angular.mock.module('app.settings.actions', ($provide) => {
		stateMock = {
			inventory: {
				datasetsSort: {}
			},
		};
		$provide.constant('state', stateMock);
	}));

	describe('dispatch', () => {
		beforeEach(inject(($q, DatasetService, StateService, StorageService) => {
			spyOn(DatasetService, 'init').and.returnValue($q.when());
			spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.when());
			spyOn(DatasetService, 'clone').and.returnValue($q.when());
			spyOn(DatasetService, 'toggleFavorite').and.returnValue();
			spyOn(DatasetService, 'delete').and.returnValue($q.when());
			spyOn(DatasetService, 'rename').and.returnValue($q.when());
			spyOn(StateService, 'setDatasetsDisplayMode').and.returnValue();
			spyOn(StateService, 'setDatasetsSortFromIds').and.returnValue();
			spyOn(StateService, 'setDatasetToUpdate').and.returnValue();
			spyOn(StorageService, 'setDatasetsSort').and.returnValue();
			spyOn(StorageService, 'setDatasetsOrder').and.returnValue();
		}));

		it('should update datasets list sort', inject((StateService, DatasetService, DatasetActionsService) => {
			// given
			const action = {
				type: '@@dataset/SORT',
				payload: {
					field: 'name',
					isDescending: true,
				}
			};

			// when
			DatasetActionsService.dispatch(action);

			// then
			expect(StateService.setDatasetsSortFromIds).toHaveBeenCalledWith('name', 'desc');
		}));

		it('should save sort in local storage',
			inject(($rootScope, StateService, StorageService, DatasetActionsService) => {
				// given
				const action = {
					type: '@@dataset/SORT',
					payload: {
						field: 'name',
						isDescending: true,
					}
				};

				// when
				DatasetActionsService.dispatch(action);
				$rootScope.$digest();

				// then
				expect(StorageService.setDatasetsSort).toHaveBeenCalledWith('name');
				expect(StorageService.setDatasetsOrder).toHaveBeenCalledWith('desc');
			})
		);

		it('should refresh datasets list', inject((DatasetService, DatasetActionsService) => {
			// given
			const action = {
				type: '@@dataset/SORT',
				payload: {
					field: 'name',
					isDescending: true,
				}
			};

			// when
			DatasetActionsService.dispatch(action);

			// then
			expect(DatasetService.refreshDatasets).toHaveBeenCalled();
		}));

		it('should fetch all datasets', inject((DatasetService, DatasetActionsService) => {
			// given
			const action = {
				type: '@@dataset/DATASET_FETCH'
			};

			// when
			DatasetActionsService.dispatch(action);

			// then
			expect(DatasetService.init).toHaveBeenCalled();
		}));

		it('should clone dataset', inject(($rootScope, DatasetService, DatasetActionsService, MessageService) => {
			// given
			const action = {
				type: '@@dataset/CLONE',
				payload: {
					method: 'clone',
					args: [],
					model: { id: 'dataset' }
				}
			};
			spyOn(MessageService, 'success').and.returnValue();

			// when
			DatasetActionsService.dispatch(action);
			$rootScope.$digest();

			// then
			expect(DatasetService.clone).toHaveBeenCalledWith({ id: 'dataset' });
			expect(MessageService.success).toHaveBeenCalled();
		}));

		it('should add dataset to the favorite list', inject((DatasetService, DatasetActionsService) => {
			// given
			const action = {
				type: '@@dataset/FAVORITE',
				payload: {
					method: 'toggleFavorite',
					args: [],
					model: { id: 'dataset' }
				}
			};

			// when
			DatasetActionsService.dispatch(action);

			// then
			expect(DatasetService.toggleFavorite).toHaveBeenCalledWith({ id: 'dataset' });
		}));

		it('should update dataset', inject(($document, StateService, DatasetActionsService) => {
			// given
			const action = {
				type: '@@dataset/UPDATE',
				payload: {
					method: '',
					args: [],
					model: { id: 'dataset' }
				}
			};

			const element = angular.element('div');

			spyOn($document[0], 'getElementById').and.returnValue(element);
			spyOn(element, 'click');

			// when
			DatasetActionsService.dispatch(action);

			// then
			expect(element.click).toHaveBeenCalled();
			expect(StateService.setDatasetToUpdate).toHaveBeenCalledWith({ id: 'dataset' });
		}));

		it('should remove dataset', inject(($q, $rootScope, DatasetService, DatasetActionsService, MessageService, TalendConfirmService) => {
			// given
			const action = {
				type: '@@dataset/REMOVE',
				payload: {
					method: 'remove',
					args: [],
					model: { id: 'dataset', name: 'dataset' }
				}
			};

			spyOn(TalendConfirmService, 'confirm').and.returnValue($q.when());
			spyOn(MessageService, 'success').and.returnValue();

			// when
			DatasetActionsService.dispatch(action);
			$rootScope.$digest();

			// then
			expect(TalendConfirmService.confirm).toHaveBeenCalled();
			expect(DatasetService.delete).toHaveBeenCalledWith({ id: 'dataset', name: 'dataset' });
			expect(MessageService.success).toHaveBeenCalled();
		}));

		it('should rename dataset', inject(($rootScope, DatasetService, DatasetActionsService, MessageService) => {
			// given
			const action = {
				type: '@@dataset/SUBMIT_EDIT',
				payload: {
					method: '',
					args: [],
					model: {
						model: { id: 'dataset', name: 'dataset' }
					},
					value: 'new dataset '
				}
			};

			spyOn(DatasetService, 'getDatasetByName').and.returnValue(false);
			spyOn(MessageService, 'success').and.returnValue();
			// when
			DatasetActionsService.dispatch(action);
			$rootScope.$digest();

			// then
			expect(DatasetService.rename).toHaveBeenCalledWith({
				id: 'dataset',
				name: 'dataset'
			}, 'new dataset');
			expect(MessageService.success).toHaveBeenCalled();
		}));

		it('should NOT rename dataset', inject(($rootScope, DatasetService, DatasetActionsService, MessageService) => {
			// given
			const action = {
				type: '@@dataset/SUBMIT_EDIT',
				payload: {
					method: '',
					args: [],
					model: {
						model: { id: 'dataset', name: 'dataset' }
					},
					value: 'new dataset '
				}
			};

			spyOn(DatasetService, 'getDatasetByName').and.returnValue(true);
			spyOn(MessageService, 'error').and.returnValue();

			// when
			DatasetActionsService.dispatch(action);
			$rootScope.$digest();

			// then
			expect(MessageService.error).toHaveBeenCalledWith('DATASET_NAME_ALREADY_USED_TITLE', 'DATASET_NAME_ALREADY_USED');
		}));

		it('should edit TCOMP dataset', inject((DatasetActionsService, StateService) => {
			// given
			const dataset = {};
			const action = {
				type: '@@dataset/TCOMP_EDIT',
				payload: {
					method: '',
					args: [],
					model: dataset,
				},
			};

			spyOn(StateService, 'setCurrentImportItem').and.returnValue();
			spyOn(StateService, 'showImport').and.returnValue();

			// when
			DatasetActionsService.dispatch(action);

			// then
			expect(StateService.setCurrentImportItem).toHaveBeenCalledWith(dataset);
			expect(StateService.showImport).toHaveBeenCalled();
		}));

		it('should create dataset with payload model', inject((DatasetActionsService, ImportService) => {
			// given
			const selectedType = importTypes[0];
			const action = {
				type: '@@dataset/CREATE',
				payload: {
					method: '',
					args: [],
					...selectedType,
				}
			};

			spyOn(ImportService, 'startImport');

			// when
			DatasetActionsService.dispatch(action);

			// then
			expect(ImportService.startImport).toHaveBeenCalledWith(action.payload);
		}));

		it('should create dataset with default type', inject((DatasetActionsService, ImportService) => {
			// given
			const defaultType = importTypes[2];
			const action = {
				type: '@@dataset/CREATE',
				payload: {
					method: '',
					args: [],
					items: importTypes,
				}
			};

			spyOn(ImportService, 'startImport').and.returnValue();

			// when
			DatasetActionsService.dispatch(action);

			// then
			expect(ImportService.startImport).toHaveBeenCalledWith(defaultType);
		}));

		it('should open dataset via workflow service', inject((DatasetActionsService, UploadWorkflowService) => {
			// given
			const dataset = { id: 'myDatasetId', draft: true };
			const event = { button: 1 };
			const action = {
				type: '@@dataset/OPEN',
				payload: { model: dataset },
				event,
			};

			spyOn(UploadWorkflowService, 'openDataset').and.returnValue();

			// when
			DatasetActionsService.dispatch(action);

			// then
			expect(UploadWorkflowService.openDataset).toHaveBeenCalledWith(dataset, event);
		}));
	});
});
