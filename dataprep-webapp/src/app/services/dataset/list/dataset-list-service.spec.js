/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import moment from 'moment';

describe('Dataset List Service', () => {
	let datasets, adaptedDatasets;
	let stateMock;
	let restPromise;

	function initDatasets() {
		datasets = [
			{
				id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
				name: 'customers_jso_light',
				author: 'anonymousUser',
				records: 15,
				nbLinesHeader: 1,
				nbLinesFooter: 0,
				created: '03-30-2015 08:06',
				preparations: [{ name: 'preparation 1' }],
				type: 'text/csv',
			},
			{
				id: '3b21388c-f54a-4334-9bef-748912d0806f',
				name: 'customers_jso',
				author: 'anonymousUser',
				records: 1000,
				nbLinesHeader: 1,
				nbLinesFooter: 0,
				created: '03-30-2015 07:35',
				type: 'application/vnd.ms-excel',
			},
			{
				id: '4d0a2718-bec6-4614-ad6c-8b3b326ff6c7',
				name: 'first_interactions',
				author: 'anonymousUser',
				records: 29379,
				nbLinesHeader: 1,
				nbLinesFooter: 0,
				created: '03-30-2015 08:05',
				type: 'text/csv',
			},
			{
				id: '5e95be9e-88cd-4765-9ecc-ee48cc28b6d5',
				name: 'first_interactions_400',
				author: 'anonymousUser',
				records: 400,
				nbLinesHeader: 1,
				nbLinesFooter: 0,
				created: '03-30-2015 08:06',
				type: 'unknown',
			},
		];

		adaptedDatasets = [
			{
				id: datasets[0].id,
				type: 'dataset',
				name: datasets[0].name,
				author: datasets[0].owner && datasets[0].owner.displayName,
				creationDate: moment(datasets[0].created).fromNow(),
				lastModificationDate: moment(datasets[0].lastModificationDate).fromNow(),
				nbRecords: datasets[0].records,
				icon: 'talend-file-csv-o',
				displayMode: 'text',
				className: '',
				actions: ['inventory:edit', 'list:dataset:preparations', 'dataset:update', 'dataset:clone', 'dataset:remove'],
				statusActions: ['dataset:favorite'],
				preparations: datasets[0].preparations,
				model: datasets[0],
			},
			{
				id: datasets[1].id,
				type: 'dataset',
				name: datasets[1].name,
				author: datasets[1].owner && datasets[1].owner.displayName,
				creationDate: moment(datasets[1].created).fromNow(),
				lastModificationDate: moment(datasets[1].lastModificationDate).fromNow(),
				nbRecords: datasets[1].records,
				icon: 'talend-file-xls-o',
				displayMode: 'text',
				className: '',
				actions: ['inventory:edit', 'dataset:update', 'dataset:clone', 'dataset:remove'],
				statusActions: ['dataset:favorite'],
				preparations: undefined,
				model: datasets[1],
			},
			{
				id: datasets[2].id,
				type: 'dataset',
				name: datasets[2].name,
				author: datasets[2].owner && datasets[2].owner.displayName,
				creationDate: moment(datasets[2].created).fromNow(),
				lastModificationDate: moment(datasets[2].lastModificationDate).fromNow(),
				nbRecords: datasets[2].records,
				icon: 'talend-file-csv-o',
				displayMode: 'text',
				className: '',
				actions: ['inventory:edit', 'dataset:update', 'dataset:clone', 'dataset:remove'],
				statusActions: ['dataset:favorite'],
				preparations: undefined,
				model: datasets[2],
			},
			{
				id: datasets[3].id,
				type: 'dataset',
				name: datasets[3].name,
				author: datasets[3].owner && datasets[3].owner.displayName,
				creationDate: moment(datasets[3].created).fromNow(),
				lastModificationDate: moment(datasets[3].lastModificationDate).fromNow(),
				nbRecords: datasets[3].records,
				icon: 'talend-file-o',
				displayMode: 'text',
				className: '',
				actions: ['inventory:edit', 'dataset:update', 'dataset:clone', 'dataset:remove'],
				statusActions: ['dataset:favorite'],
				preparations: undefined,
				model: datasets[3],
			},
		];
	}

	const sortList = [
		{ id: 'name', name: 'NAME_SORT', property: 'name' },
		{ id: 'date', name: 'DATE_SORT', property: 'created' },
	];

	const orderList = [
		{ id: 'asc', name: 'ASC_ORDER' },
		{ id: 'desc', name: 'DESC_ORDER' },
	];

	beforeEach(angular.mock.module('data-prep.services.dataset', ($provide) => {
		stateMock = {
			inventory: {
				datasets: {
					sort: {
						field: 'date',
						isDescending: true,
					}
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($q, DatasetRestService, StateService) => {
		initDatasets();
		restPromise = $q.when(true);

		spyOn(DatasetRestService, 'create').and.returnValue(restPromise);
		spyOn(DatasetRestService, 'update').and.returnValue(restPromise);
		spyOn(DatasetRestService, 'clone').and.returnValue(restPromise);
		spyOn(DatasetRestService, 'delete').and.returnValue(restPromise);
		spyOn(DatasetRestService, 'toggleFavorite').and.returnValue(restPromise);

		spyOn(StateService, 'setDatasets').and.returnValue();
		spyOn(StateService, 'removeDataset').and.returnValue();
	}));

	describe('getter/refresher', () => {
		describe('nominal', () => {
			beforeEach(inject(($q, DatasetRestService) => {
				spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({ data: datasets.slice(0) }));
			}));

			it('should refresh dataset list', inject(($rootScope, DatasetListService, StateService) => {
				//given
				stateMock.inventory.datasets.content = [{ name: 'my dataset' }, { name: 'my second dataset' }];

				//when
				DatasetListService.refreshDatasets();
				$rootScope.$apply();

				//then
				expect(StateService.setDatasets).toHaveBeenCalledWith(adaptedDatasets);
			}));

			it('should trigger another refresh when one is already pending with different sort condition',
				inject(($rootScope, DatasetListService, DatasetRestService, StateService) => {
					//given
					stateMock.inventory.datasets.content = [{ name: 'my dataset' }, { name: 'my second dataset' }];
					DatasetListService.refreshDatasets();

					//when
					DatasetListService.refreshDatasets();
					$rootScope.$apply();

					//then
					expect(StateService.setDatasets).toHaveBeenCalledWith(adaptedDatasets);
					expect(DatasetRestService.getDatasets.calls.count()).toBe(2);
				})
			);

			it('should call refreshDatasets when datasetsPromise is false', inject((DatasetRestService, DatasetListService) => {
				//when
				DatasetListService.getDatasetsPromise();

				//then
				expect(DatasetRestService.getDatasets).toHaveBeenCalled();
			}));

			it('should return datasetsPromise when datasetsPromise is not false', inject((DatasetRestService, DatasetListService) => {

				//given
				stateMock.inventory.datasets.content = null;

				//when
				DatasetListService.getDatasetsPromise();
				DatasetListService.getDatasetsPromise();

				//then
				expect(DatasetRestService.getDatasets.calls.count()).toBe(1);
			}));

			it('should return datasetsPromise', inject((DatasetRestService, DatasetListService) => {
				//when
				const datasetsPromise = DatasetListService.hasDatasetsPromise();

				//then
				expect(datasetsPromise).toBeFalsy();
			}));
		});

		describe('errors', () => {
			beforeEach(inject(($q, DatasetRestService) => {
				spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.reject());
			}));

			it('should refresh dataset list when REST request is failed', inject(($rootScope, DatasetListService, StateService) => {
				//when
				DatasetListService.refreshDatasets();
				$rootScope.$apply();

				//then
				expect(StateService.setDatasets).toHaveBeenCalledWith([]);
			}));
		});
	});

	describe('create', () => {
		beforeEach(inject(($q, DatasetRestService) => {
			spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({ data: datasets.slice(0) }));
		}));

		it('should create dataset', inject(($rootScope, DatasetListService, DatasetRestService) => {
			//given
			const dataset = { name: 'my dataset' };
			var file = { id: '0001' };
			var contentType = 'text/plain';

			//when
			DatasetListService.create(dataset, contentType, file);
			$rootScope.$apply();

			//then
			expect(DatasetRestService.create).toHaveBeenCalledWith(dataset, contentType, file);
		}));

		it('should refresh datasets list', inject(($rootScope, DatasetListService, DatasetRestService) => {
			//given
			const dataset = { name: 'my dataset' };

			//when
			DatasetListService.create(dataset);
			$rootScope.$apply();

			//then
			expect(DatasetRestService.getDatasets).toHaveBeenCalled();
		}));

		it('should return original REST promise (not the promise with dataset list refresh)', inject(($rootScope, DatasetListService) => {
			//given
			const dataset = { name: 'my dataset' };

			//when
			const promise = DatasetListService.create(dataset);

			//then
			expect(promise).toBe(restPromise);
		}));
	});

	describe('clone', () => {
		beforeEach(inject(($q, DatasetRestService) => {
			spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({ data: datasets.slice(0) }));
		}));

		it('should call rest service clone', inject((DatasetRestService, DatasetListService) => {
			//when
			DatasetListService.clone(datasets[0]);

			//then
			expect(DatasetRestService.clone).toHaveBeenCalledWith(datasets[0]);
		}));

		it('should refresh datasets list', inject(($rootScope, DatasetRestService, DatasetListService) => {
			//when
			DatasetListService.clone(datasets[0]);
			$rootScope.$digest();

			//then
			expect(DatasetRestService.getDatasets).toHaveBeenCalled();
		}));

		it('should return original REST promise (not the promise with dataset list refresh)', inject(($rootScope, DatasetListService) => {
			//when
			const promise = DatasetListService.clone(datasets[0]);

			//then
			expect(promise).toBe(restPromise);
		}));
	});

	describe('update', () => {
		beforeEach(inject(($q, DatasetRestService) => {
			spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({ data: datasets.slice(0) }));
		}));

		it('should update dataset', inject(($rootScope, DatasetListService, DatasetRestService) => {
			//given
			const dataset = { name: 'my dataset' };

			//when
			DatasetListService.update(dataset, {size: 1000});
			$rootScope.$apply();

			//then
			expect(DatasetRestService.update).toHaveBeenCalledWith(dataset, {size: 1000});
		}));

		it('should refresh datasets list', inject(($rootScope, DatasetListService, DatasetRestService) => {
			//given
			const dataset = { name: 'my dataset' };

			//when
			DatasetListService.update(dataset);
			$rootScope.$apply();

			//then
			expect(DatasetRestService.getDatasets).toHaveBeenCalled();
		}));

		it('should return original REST promise (not the promise with dataset list refresh)', inject(($rootScope, DatasetListService) => {
			//given
			const dataset = { name: 'my dataset' };

			//when
			const promise = DatasetListService.update(dataset);

			//then
			expect(promise).toBe(restPromise);
		}));
	});

	describe('delete', () => {
		beforeEach(inject(($q, DatasetRestService) => {
			spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({ data: datasets.slice(0) }));
		}));

		it('should delete dataset', inject(($rootScope, DatasetListService, DatasetRestService) => {
			//given
			stateMock.inventory.datasets.content = datasets.slice(0);

			//when
			DatasetListService.delete(datasets[0]);
			$rootScope.$apply();

			//then
			expect(DatasetRestService.delete).toHaveBeenCalledWith(datasets[0]);
		}));

		it('should remove dataset from its internal list', inject(($rootScope, DatasetListService, StateService) => {
			//given
			stateMock.inventory.datasets.content = datasets.slice(0);

			//when
			DatasetListService.delete(datasets[0]);
			$rootScope.$apply();

			//then
			expect(StateService.removeDataset).toHaveBeenCalledWith(datasets[0]);
		}));
	});

	describe('toggle', () => {
		beforeEach(inject(($q, DatasetRestService) => {
			spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({ data: datasets.slice(0) }));
		}));

		it('should toggle dataset', inject(($rootScope, DatasetListService, DatasetRestService) => {
			//given
			const dataset = { name: 'my dataset', favorite: true };

			//when
			DatasetListService.toggleFavorite({ model: dataset });
			$rootScope.$apply();

			//then
			expect(DatasetRestService.toggleFavorite).toHaveBeenCalledWith(dataset);
			expect(dataset.favorite).toBe(false);
		}));

		it('should refresh datasets list', inject(($rootScope, DatasetListService, DatasetRestService) => {
			//given
			const dataset = { name: 'my dataset', favorite: true };

			//when
			DatasetListService.toggleFavorite({ model: dataset });
			$rootScope.$apply();

			//then
			expect(DatasetRestService.getDatasets).toHaveBeenCalled();
		}));
	});

	describe('getDatasetActions', () => {
		it('should return actions for a dataset which does not have related preparations', inject((DatasetListService) => {
			//given
			const dataset = { id: 'dataset' };

			//then
			expect(DatasetListService.getDatasetActions(dataset)).toEqual(['inventory:edit', 'dataset:update', 'dataset:clone', 'dataset:remove']);
		}));

		it('should return actions for a dataset which has related preparations', inject((DatasetListService) => {
			//given
			const dataset = { id: 'dataset', preparations: [{ id: 'preparation' }] };

			//then
			expect(DatasetListService.getDatasetActions(dataset)).toEqual(['inventory:edit', 'list:dataset:preparations', 'dataset:update', 'dataset:clone', 'dataset:remove']);
		}));
	});

	describe('getClassName', () => {
		it('should return favorite class', inject((DatasetListService) => {
			// given
			const dataset = { favorite: true };

			// when
			const classNames = DatasetListService.getClassName(dataset);

			// then
			expect(classNames).toEqual(['list-item-favorite']);
		}));

		it('should return empty array when dataset is not favorite', inject((DatasetListService) => {
			// given
			const dataset = { favorite: false };

			// when
			const classNames = DatasetListService.getClassName(dataset);

			// then
			expect(classNames).toEqual([]);
		}));
	});
});
