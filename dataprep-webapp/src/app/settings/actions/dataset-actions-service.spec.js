/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Datasets actions service', () => {
	beforeEach(angular.mock.module('app.settings.actions'));

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
					sortBy: 'name',
					sortDesc: true,
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
						sortBy: 'name',
						sortDesc: true,
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
					sortBy: 'name',
					sortDesc: true,
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
					model: {id: 'dataset'}
				}
			};
			spyOn(MessageService, 'success').and.returnValue();

			// when
			DatasetActionsService.dispatch(action);
			$rootScope.$digest();

			// then
			expect(DatasetService.clone).toHaveBeenCalledWith({id: 'dataset'});
			expect(MessageService.success).toHaveBeenCalled();
		}));

		it('should add dataset to the favourite list', inject((DatasetService, DatasetActionsService) => {
			// given
			const action = {
				type: '@@dataset/FAVOURITE',
				payload: {
					method: 'toggleFavorite',
					args: [],
					model: {id: 'dataset'}
				}
			};

			// when
			DatasetActionsService.dispatch(action);

			// then
			expect(DatasetService.toggleFavorite).toHaveBeenCalledWith({id: 'dataset'});
		}));

		it('should update dataset', inject(($document, StateService, DatasetActionsService) => {
			// given
			const action = {
				type: '@@dataset/UPDATE',
				payload: {
					method: '',
					args: [],
					model: {id: 'dataset'}
				}
			};

			const element = angular.element('div');

			spyOn($document[0], 'getElementById').and.returnValue(element);
			spyOn(element, 'click');

			// when
			DatasetActionsService.dispatch(action);

			// then
			expect(element.click).toHaveBeenCalled();
			expect(StateService.setDatasetToUpdate).toHaveBeenCalledWith({id: 'dataset'});
		}));

		it('should remove dataset', inject(($q, $rootScope, DatasetService, DatasetActionsService, MessageService, TalendConfirmService) => {
			// given
			const action = {
				type: '@@dataset/REMOVE',
				payload: {
					method: 'remove',
					args: [],
					model: {id: 'dataset', name: 'dataset'}
				}
			};

			spyOn(TalendConfirmService, 'confirm').and.returnValue($q.when());
			spyOn(MessageService, 'success').and.returnValue();

			// when
			DatasetActionsService.dispatch(action);
			$rootScope.$digest();

			// then
			expect(TalendConfirmService.confirm).toHaveBeenCalled();
			expect(DatasetService.delete).toHaveBeenCalledWith({id: 'dataset', name: 'dataset'});
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
						model: {id: 'dataset', name: 'dataset'}
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
			expect(DatasetService.rename).toHaveBeenCalledWith({id: 'dataset', name: 'dataset'}, 'new dataset');
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
						model: {id: 'dataset', name: 'dataset'}
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
	});
});
