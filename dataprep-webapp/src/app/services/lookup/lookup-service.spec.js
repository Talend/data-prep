/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Lookup service', () => {
	'use strict';
	let stateMock;

	//lookup dataset content
	const firstDsLookupId = 'first_lookup_dataset_id';
	const dsLookupContent = {
		metadata: {
			id: 'first_lookup_dataset_id',
			records: 3,
			certificationStep: 'NONE',
			location: {
				type: 'local',
			},
			name: 'lookup_2',
			author: 'anonymous',
			created: 1447689742940,
			encoding: 'UTF-8',
			columns: [{ id: '0000' }],
		},
	};
	const datasetsToAdd = [
		{
			id: 'first_lookup_dataset_id',
			name: 'lookup_2',
			author: 'anonymous',
			records: 3,
			nbLinesHeader: 1,
			nbLinesFooter: 0,
			created: 1447689742940,
			addedToLookup: false,
			enableToAddToLookup: true,
		},
		{
			id: 'second_lookup_dataset_id',
			name: 'customers_jso',
			author: 'anonymousUser',
			records: 1000,
			nbLinesHeader: 1,
			nbLinesFooter: 0,
			created: '03-30-2015 07:35',
			addedToLookup: false,
			enableToAddToLookup: true
		}
	];
	const datasets = [
		{
			id: 'first_lookup_dataset_id',
			name: 'lookup_2',
			author: 'anonymous',
			records: 3,
			nbLinesHeader: 1,
			nbLinesFooter: 0,
			created: 1447689742940,
		},
		{
			id: 'second_lookup_dataset_id',
			name: 'customers_jso',
			author: 'anonymousUser',
			records: 1000,
			nbLinesHeader: 1,
			nbLinesFooter: 0,
			created: '03-30-2015 07:35',
		},
		{
			id: '4d0a2718-bec6-4614-ad6c-8b3b326ff6c7',
			name: 'first_interactions',
			author: 'anonymousUser',
			records: 29379,
			nbLinesHeader: 1,
			nbLinesFooter: 0,
			created: '03-30-2015 08:05',
		},
		{
			id: '5e95be9e-88cd-4765-9ecc-ee48cc28b6d5',
			name: 'first_interactions_400',
			author: 'anonymousUser',
			records: 400,
			nbLinesHeader: 1,
			nbLinesFooter: 0,
			created: '03-30-2015 08:06',
		},
	];

	//lookup actions
	const lookupActions = [
		{
			category: 'data_blending',
			name: 'lookup',
			parameters: [
				{
					name: 'column_id',
					type: 'string',
					default: '',
				},
				{
					name: 'filter',
					type: 'filter',
					default: '',
				},
				{
					name: 'lookup_ds_name',
					type: 'string',
					default: 'lookup_2',
				},
				{
					name: 'lookup_ds_id',
					type: 'string',
					default: 'first_lookup_dataset_id',
				},
				{
					name: 'lookup_join_on',
					type: 'string',
					default: '',
				},
				{
					name: 'lookup_join_on_name',
					type: 'string',
					default: '',
				},
				{
					name: 'lookup_selected_cols',
					type: 'list',
					default: '',
				},
			],
		},
		{
			category: 'data_blending',
			name: 'lookup',
			parameters: [
				{
					name: 'column_id',
					type: 'string',
					default: '',
				},
				{
					name: 'filter',
					type: 'filter',
					default: '',
				},
				{
					name: 'lookup_ds_name',
					type: 'string',
					default: 'lookup_2',
				},
				{
					name: 'lookup_ds_id',
					type: 'string',
					default: 'second_lookup_dataset_id',
				},
				{
					name: 'lookup_join_on',
					type: 'string',
					default: '',
				},
				{
					name: 'lookup_join_on_name',
					type: 'string',
					default: '',
				},
				{
					name: 'lookup_selected_cols',
					type: 'list',
					default: '',
				},
			],
		},
	];

	//recipe
	const lookupStep = {
		column: {
			id: '0000',
			name: 'id',
		},
		row: {
			id: '11',
		},
		transformation: {
			stepId: '72fe267d489b06890da69368f4760530b076ec59',
			name: 'lookup',
			label: 'Lookup',
			description: 'Blends columns from another dataset into this one',
			parameters: [],
			dynamic: false,
		},
		actionParameters: {
			action: 'lookup',
			parameters: {
				column_id: '0000',
				column_name: 'id',
				filter: '',
				lookup_ds_id: 'first_lookup_dataset_id',
				lookup_ds_name: 'cluster_dataset',
				lookup_join_on: '0000',
				lookup_join_on_name: 'id',
				lookup_selected_cols: [
					{
						id: '0001',
						name: 'uglystate',
					},
				],
				row_id: '11',
				scope: 'dataset',
			},
		},
		diff: {
			createdColumns: [
				'0009',
			],
		},
		filters: null,
	};

	const sortList = [
		{ id: 'name', name: 'NAME_SORT', property: 'name' },
		{ id: 'date', name: 'DATE_SORT', property: 'created' },
	];

	const orderList = [
		{ id: 'asc', name: 'ASC_ORDER' },
		{ id: 'desc', name: 'DESC_ORDER' },
	];

	beforeEach(angular.mock.module('data-prep.services.lookup', ($provide) => {
		stateMock = {
			playground: {
				data: null,
				dataset: { id: 'abcd' },
				lookup: {
					visibility: false,
					addedActions: [],
				},
				stepInEditionMode: null,
				grid: {},
				recipe: { current: { steps: [] } },
			},
			inventory: {
				datasets: { content: datasets },
				sortList: sortList,
				orderList: orderList,
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($q, TransformationRestService, DatasetRestService, StateService) => {
		spyOn(DatasetRestService, 'getContent').and.returnValue($q.when(dsLookupContent));
		spyOn(StateService, 'setGridSelection').and.returnValue();
		spyOn(StateService, 'setLookupActions').and.returnValue();
		spyOn(StateService, 'setLookupDataset').and.returnValue();
		spyOn(StateService, 'setLookupSelectedColumn').and.returnValue();
		spyOn(StateService, 'setLookupData').and.returnValue();
		spyOn(StateService, 'setLookupDatasets').and.returnValue();
	}));

	describe('init lookup', () => {
		beforeEach(inject(($q, TransformationRestService, DatasetListService) => {
			spyOn(TransformationRestService, 'getDatasetTransformations').and.returnValue($q.when({ data: lookupActions }));
			spyOn(DatasetListService, 'refreshDatasets').and.returnValue($q.when());
			stateMock.playground.grid.selectedColumns = [];
		}));

		it('should load the first action as new lookup',
			inject(($rootScope, LookupService, DatasetRestService, StateService) => {
				//given
				stateMock.playground.lookup.addedActions = lookupActions;
				stateMock.playground.grid.selectedColumns = [];

				//when
				LookupService.initLookups();
				$rootScope.$digest();

				//then
				expect(StateService.setGridSelection).not.toHaveBeenCalled();// called only in update
				expect(StateService.setLookupDataset)
					.toHaveBeenCalledWith(lookupActions[0]);
		}));

		describe('datasets', () => {
			it('should fetch datasets list', inject((LookupService, DatasetListService) => {
				stateMock.inventory.datasets.content = null;

				//when
				LookupService.initLookups();

				//then
				expect(DatasetListService.refreshDatasets).toHaveBeenCalled();
			}));

			it('should NOT fetch datasets list if it already exists',
				inject((DatasetListService, LookupService) => {
				//given
				stateMock.inventory.datasets.content = [{ id: '123' }];

				//when
				LookupService.initLookups();

				//then
				expect(DatasetListService.refreshDatasets).not.toHaveBeenCalled();
			}));
		});

		describe('actions', () => {
			it('should fetch lookup actions when they are not initialized yet',
				inject(($rootScope, $q, LookupService, StateService, TransformationRestService) => {
				//given
				stateMock.playground.lookup.addedActions = [];

				//when
				LookupService.initLookups();
				$rootScope.$digest();

				//then
				expect(TransformationRestService.getDatasetTransformations)
					.toHaveBeenCalledWith(stateMock.playground.dataset.id);
				expect(StateService.setLookupDatasets).toHaveBeenCalledWith(datasetsToAdd);
				expect(StateService.setLookupActions).toHaveBeenCalledWith(lookupActions);
			}));

			it('should NOT fetch lookup actions when they are already initialized',
				inject(($rootScope, $q, LookupService, StateService, TransformationRestService) => {
				//given
				stateMock.playground.lookup.addedActions = lookupActions;

				//when
				LookupService.initLookups();
				$rootScope.$digest();

				//then
				expect(TransformationRestService.getDatasetTransformations).not.toHaveBeenCalled();
				expect(StateService.setLookupDatasets).not.toHaveBeenCalled();
				expect(StateService.setLookupActions).not.toHaveBeenCalled();
			}));
		});
	});

	describe('create a lookup step', () => {
		it('should initialize the lookup creation',
			inject(($rootScope, LookupService, DatasetRestService, StateService) => {
			// given
			stateMock.playground.lookup.addedActions = lookupActions;

			// when
			LookupService.loadFromAction(lookupActions[1]);
			$rootScope.$digest();

			// then
			expect(DatasetRestService.getContent).toHaveBeenCalledWith('second_lookup_dataset_id', true);
			expect(StateService.setLookupDataset).toHaveBeenCalledWith(lookupActions[1]);
			expect(StateService.setLookupData).toHaveBeenCalledWith(dsLookupContent, stateMock.playground.stepInEditionMode);
			expect(StateService.setLookupSelectedColumn).toHaveBeenCalledWith(dsLookupContent.metadata.columns[0]);
		}));
	});

	describe('update a lookup step', () => {
		beforeEach(inject(($q, TransformationRestService, DatasetListService) => {
			spyOn(TransformationRestService, 'getDatasetTransformations').and.returnValue($q.when({ data: lookupActions }));
			spyOn(DatasetListService, 'refreshDatasets').and.returnValue($q.when());
			stateMock.playground.data = { metadata: { columns: [{ id: '0000' }] } };
			stateMock.playground.stepInEditionMode = lookupStep;
		}));

		it('should not get dataset transformations if there is not dataset yet', inject((LookupService, TransformationRestService) => {
			//when
			LookupService.loadFromStep(lookupStep);

			//then
			expect(TransformationRestService.getDatasetTransformations).not.toHaveBeenCalled();
		}));

		it('should set the join column in the state', inject(($rootScope, LookupService, DatasetRestService, StateService) => {
			//given
			stateMock.playground.lookup.addedActions = lookupActions;

			//when
			LookupService.loadFromStep(lookupStep);
			$rootScope.$digest();

			//then
			expect(StateService.setLookupSelectedColumn).toHaveBeenCalledWith(dsLookupContent.metadata.columns[0]);
		}));

		it('should update the base lookup column', inject(($rootScope, LookupService, StateService) => {
			//given
			stateMock.playground.lookup.addedActions = lookupActions;

			//when
			LookupService.loadFromStep(lookupStep);
			$rootScope.$digest();

			//then
			expect(StateService.setGridSelection).toHaveBeenCalledWith([{ id: '0000' }]);
		}));
	});

	describe('fetching lookup dataset content and updating the state', () => {
		it('should fetch dataset content the lookup creation',
			inject(($rootScope, LookupService, DatasetRestService, StateService) => {
				// given
				stateMock.playground.lookup.addedActions = lookupActions;

				// when
				LookupService.fetchLookupDatasetContent(firstDsLookupId, lookupActions[0]);
				$rootScope.$digest();

				// then
				expect(DatasetRestService.getContent).toHaveBeenCalledWith('first_lookup_dataset_id', true);
				expect(StateService.setLookupDataset).toHaveBeenCalledWith(lookupActions[0]);
				expect(StateService.setLookupData).toHaveBeenCalledWith(dsLookupContent, stateMock.playground.stepInEditionMode);
				expect(StateService.setLookupSelectedColumn).toHaveBeenCalledWith(dsLookupContent.metadata.columns[0]);
			}));
	});

	describe('add datasets to lookup', () => {
		it('should disable datasets which are used in recipe steps', inject(($rootScope, LookupService) => {
			//given
			stateMock.playground.lookup.datasets = [
				{ id: 'first_lookup_dataset_id' },
				{ id: '3' },
				{ id: '2' },
			];
			stateMock.playground.recipe.current.steps = [lookupStep];

			//when
			LookupService.disableDatasetsUsedInRecipe();

			//then
			expect(stateMock.playground.lookup.datasets[0].enableToAddToLookup).toBe(false);
			expect(stateMock.playground.lookup.datasets[1].enableToAddToLookup).toBe(true);
			expect(stateMock.playground.lookup.datasets[2].enableToAddToLookup).toBe(true);
		}));

		it('should initialize lookup datasets', inject(($q, $rootScope, LookupService, StorageService, StateService, TransformationRestService) => {
			//given
			stateMock.playground.lookup.datasets = [
				{ id: 'first_lookup_dataset_id', addedToLookup: false, created: 80 },
				{ id: '3', addedToLookup: false, created: 90 },
				{ id: '2', addedToLookup: false, created: 100 },
			];

			stateMock.playground.lookup.actions = lookupActions;
			stateMock.playground.lookup.addedActions = [];
			stateMock.playground.lookup.sort = {
				id: 'date',
				name: 'DATE_SORT',
				property: 'created'
			};
			stateMock.playground.lookup.order = { id: 'desc', name: 'DESC_ORDER' };
			stateMock.playground.recipe.current.steps = [lookupStep];

			spyOn(StorageService, 'getLookupDatasets').and.returnValue(['1']);
			spyOn(StorageService, 'setLookupDatasets').and.returnValue();
			spyOn(StateService, 'setLookupAddedActions').and.returnValue();
			spyOn(TransformationRestService, 'getDatasetTransformations').and.returnValue($q.when({ data: lookupActions }));

			//when
			LookupService.initLookups();
			$rootScope.$digest();

			//then
			expect(StorageService.setLookupDatasets).toHaveBeenCalledWith(['1', 'first_lookup_dataset_id']);

			expect(stateMock.playground.lookup.datasets[0].addedToLookup).toBe(true);
			expect(stateMock.playground.lookup.datasets[1].addedToLookup).toBe(false);
			expect(stateMock.playground.lookup.datasets[2].addedToLookup).toBe(false);
		}));

		it('should update lookup datasets', inject(($q, $rootScope, LookupService, StorageService, StateService) => {
			//given
			stateMock.playground.lookup.datasets = [
				{ id: 'first_lookup_dataset_id', addedToLookup: true, created: 100 },
				{ id: '3', addedToLookup: false, created: 90 },
				{ id: '2', addedToLookup: false, created: 80 },
			];

			stateMock.playground.lookup.actions = lookupActions;
			stateMock.playground.lookup.addedActions = [];
			stateMock.playground.dataset.id = '4';

			spyOn(StorageService, 'getLookupDatasets').and.returnValue(['4']);
			spyOn(StorageService, 'setLookupDatasets').and.returnValue();
			spyOn(StateService, 'setLookupAddedActions').and.returnValue();

			//when
			LookupService.updateLookupDatasets();
			$rootScope.$digest();

			//then
			expect(StorageService.setLookupDatasets).toHaveBeenCalledWith(['first_lookup_dataset_id', '4']);
		}));
	});
});
