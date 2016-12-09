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
			spyOn(StateService, 'setDatasetsDisplayMode').and.returnValue();
			spyOn(StateService, 'setDatasetsSortFromIds').and.returnValue();
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
	});
});
