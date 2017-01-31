/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

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
	const firstDsLookupId = '9e739b88-5ec9-4b58-84b5-2127a7e2eac7';
	const dsLookupContent = {
		metadata: {
			id: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
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

	const datasets = [
		{
			id: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
			name: 'lookup_2',
			author: 'anonymous',
			records: 3,
			nbLinesHeader: 1,
			nbLinesFooter: 0,
			created: 1447689742940,
		},
		{
			id: '3b21388c-f54a-4334-9bef-748912d0806f',
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
					default: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
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
	const firstLookupAction = lookupActions[0];

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
				lookup_ds_id: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
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
		spyOn(StateService, 'setLookupAddMode').and.returnValue();
		spyOn(StateService, 'setLookupUpdateMode').and.returnValue();
	}));

	describe('init lookup', () => {
		describe('actions', () => {
			beforeEach(inject(($q, TransformationRestService, DatasetListService) => {
				spyOn(TransformationRestService, 'getDatasetTransformations').and.returnValue($q.when({ data: lookupActions }));
				spyOn(DatasetListService, 'refreshDatasets').and.returnValue($q.when());

				stateMock.playground.grid.selectedColumns = [];
			}));

			it('should fetch datasets list', inject((LookupService, DatasetListService) => {
				stateMock.inventory.datasets.content = null;

				//when
				LookupService.initLookups();

				//then
				expect(DatasetListService.refreshDatasets).toHaveBeenCalled();
			}));

			it('should NOT fetch datasets list if exist', inject((DatasetListService, LookupService) => {
				//given
				stateMock.inventory.datasets.content = [{ id: '123' }];

				//when
				LookupService.initLookups();

				//then
				expect(DatasetListService.refreshDatasets).not.toHaveBeenCalled();
			}));

			it('should fetch lookup actions when they are not initialized yet', inject(($rootScope, $q, LookupService, StateService, TransformationRestService) => {
				//given
				stateMock.playground.lookup.addedActions = [];

				stateMock.playground.lookup.sort = {
					id: 'date',
					name: 'DATE_SORT',
					property: 'created'
				};
				stateMock.playground.lookup.order = { id: 'desc', name: 'DESC_ORDER' };
				spyOn(StateService, 'setLookupDatasets');

				//when
				LookupService.initLookups();
				$rootScope.$digest();

				//then
				expect(TransformationRestService.getDatasetTransformations).toHaveBeenCalled();
				expect(StateService.setLookupActions).toHaveBeenCalledWith(lookupActions);
			}));

			it('should NOT fetch lookup actions when they are already initialized', inject(($rootScope, $q, LookupService, StateService, TransformationRestService) => {
				//given
				stateMock.playground.lookup.addedActions = lookupActions;

				//when
				LookupService.initLookups();
				$rootScope.$digest();

				//then
				expect(TransformationRestService.getDatasetTransformations).not.toHaveBeenCalled();
				expect(StateService.setLookupActions).not.toHaveBeenCalled();
			}));
		});

		describe('with no actions', () => {
			it('should NOT initialize lookup state', inject(($rootScope, $q, LookupService, StateService, TransformationRestService) => {
				//given
				spyOn(TransformationRestService, 'getDatasetTransformations').and.returnValue($q.when({ data: [] }));
				stateMock.playground.lookup.addedActions = [];

				stateMock.playground.lookup.sort = {
					id: 'date',
					name: 'DATE_SORT',
					property: 'created'
				};
				stateMock.playground.lookup.order = { id: 'desc', name: 'DESC_ORDER' };

				//when
				LookupService.initLookups();
				$rootScope.$digest();

				//then
				expect(StateService.setLookupAddMode).not.toHaveBeenCalled();
				expect(StateService.setLookupUpdateMode).not.toHaveBeenCalled();
			}));
		});

		describe('without lookup step on selected column', () => {
			it('should load the first action as new lookup', inject(($rootScope, LookupService, DatasetRestService, StateService) => {
				//given
				stateMock.playground.lookup.addedActions = lookupActions;
				stateMock.playground.grid.selectedColumns = [];

				//when
				LookupService.initLookups();
				$rootScope.$digest();

				//then
				expect(DatasetRestService.getContent).toHaveBeenCalledWith(firstDsLookupId, true);
				expect(StateService.setLookupAddMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent);
			}));
		});

		describe('with lookup step on selected column', () => {
			it('should load the step action as lookup update', inject(($rootScope, LookupService, DatasetRestService, StateService) => {
				//given
				stateMock.playground.recipe.current.steps = [lookupStep];
				stateMock.playground.data = { metadata: { columns: [{ id: '0000' }] } };
				stateMock.playground.lookup.addedActions = lookupActions;
				stateMock.playground.grid.selectedColumns = [{ id: '0000' }];

				//when
				LookupService.initLookups();
				$rootScope.$digest();

				//then
				expect(DatasetRestService.getContent).toHaveBeenCalledWith(firstDsLookupId, true);
				expect(StateService.setLookupUpdateMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent, lookupStep);
			}));
		});
	});

	describe('load from action', () => {
		it('should load action as new lookup', inject(($rootScope, LookupService, DatasetRestService, StateService) => {
			//given
			stateMock.playground.lookup.addedActions = lookupActions;
			stateMock.playground.grid.selectedColumns = [{ id: '0000' }];

			//when
			LookupService.loadFromAction(firstLookupAction);
			$rootScope.$digest();

			//then
			expect(StateService.setLookupAddMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent);
		}));

		it('should load action as lookup update when selected column has this lookup action as step', inject(($rootScope, LookupService, StateService) => {
			//given
			stateMock.playground.data = { metadata: { columns: [{ id: '0000' }] } };
			stateMock.playground.lookup.addedActions = lookupActions;
			stateMock.playground.grid.selectedColumns = [{ id: '0000' }];
			stateMock.playground.recipe.current.steps = [lookupStep];

			//when
			LookupService.loadFromAction(firstLookupAction);
			$rootScope.$digest();

			//then
			expect(StateService.setLookupUpdateMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent, lookupStep);
		}));

		it('should NOT change lookup state when the action is already loaded', inject(($rootScope, LookupService, DatasetRestService, StateService) => {
			//given
			stateMock.playground.lookup.addedActions = lookupActions;
			stateMock.playground.lookup.dataset = firstLookupAction;
			stateMock.playground.lookup.step = null;
			stateMock.playground.grid.selectedColumns = [];

			//when
			LookupService.loadFromAction(firstLookupAction);
			$rootScope.$digest();

			//then
			expect(DatasetRestService.getContent).not.toHaveBeenCalled();
			expect(StateService.setLookupAddMode).not.toHaveBeenCalled();
			expect(StateService.setLookupUpdateMode).not.toHaveBeenCalled();
		}));
	});

	describe('load from step', () => {
		beforeEach(() => {
			stateMock.playground.data = { metadata: { columns: [{ id: '0000' }] } };
		});

		it('should load action as lookup update', inject(($rootScope, LookupService, DatasetRestService, StateService) => {
			//given
			stateMock.playground.lookup.addedActions = lookupActions;

			//when
			LookupService.loadFromStep(lookupStep);
			$rootScope.$digest();

			//then
			expect(StateService.setLookupUpdateMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent, lookupStep);
		}));

		it('should set state grid selection to the step target', inject(($rootScope, LookupService, StateService) => {
			//given
			stateMock.playground.lookup.addedActions = lookupActions;

			//when
			LookupService.loadFromStep(lookupStep);
			$rootScope.$digest();

			//then
			expect(StateService.setGridSelection).toHaveBeenCalledWith([{ id: '0000' }]);
		}));

		it('should NOT change lookup state when the step lookup is already loaded', inject(($rootScope, LookupService, DatasetRestService, StateService) => {
			//given
			stateMock.playground.lookup.addedActions = lookupActions;
			stateMock.playground.lookup.dataset = firstLookupAction;
			stateMock.playground.lookup.step = lookupStep;

			//when
			LookupService.loadFromStep(lookupStep);
			$rootScope.$digest();

			//then
			expect(DatasetRestService.getContent).not.toHaveBeenCalled();
			expect(StateService.setLookupAddMode).not.toHaveBeenCalled();
			expect(StateService.setLookupUpdateMode).not.toHaveBeenCalled();
		}));
	});

	describe('update target column', () => {
		it('should load action as new lookup on the current column when it was on an update mode in the previous column', inject(($rootScope, LookupService, DatasetRestService, StateService) => {
			//given
			stateMock.playground.lookup.addedActions = lookupActions;
			stateMock.playground.lookup.dataset = firstLookupAction;
			stateMock.playground.lookup.step = lookupStep;
			stateMock.playground.lookup.visibility = true;
			stateMock.playground.grid.selectedColumns = [];
			//when
			LookupService.updateTargetColumn();
			$rootScope.$digest();

			//then
			expect(StateService.setLookupAddMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent);
		}));

		it('should load action as lookup update when the new selected column has this lookup action as step', inject(($rootScope, LookupService, StateService) => {
			//given
			stateMock.playground.data = { metadata: { columns: [{ id: '0000' }] } };
			stateMock.playground.lookup.addedActions = lookupActions;
			stateMock.playground.lookup.dataset = firstLookupAction;
			stateMock.playground.lookup.visibility = true;
			stateMock.playground.grid.selectedColumns = [{ id: '0000' }];
			stateMock.playground.recipe.current.steps = [lookupStep];

			//when
			LookupService.updateTargetColumn();
			$rootScope.$digest();

			//then
			expect(StateService.setLookupUpdateMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent, lookupStep);
		}));

		it('should NOT update target column when the lookup is not visible', inject(($rootScope, LookupService, DatasetRestService, StateService) => {
			//given
			stateMock.playground.lookup.addedActions = lookupActions;
			stateMock.playground.lookup.dataset = firstLookupAction;
			stateMock.playground.lookup.visibility = false;

			//when
			LookupService.updateTargetColumn();
			$rootScope.$digest();

			//then
			expect(StateService.setLookupAddMode).not.toHaveBeenCalled();
			expect(StateService.setLookupUpdateMode).not.toHaveBeenCalled();
		}));

		it('should NOT update target column when there is no lookup dataset', inject(($rootScope, LookupService, DatasetRestService, StateService) => {
			//given
			stateMock.playground.lookup.addedActions = lookupActions;
			stateMock.playground.lookup.dataset = null;
			stateMock.playground.lookup.visibility = true;

			//when
			LookupService.updateTargetColumn();
			$rootScope.$digest();

			//then
			expect(StateService.setLookupAddMode).not.toHaveBeenCalled();
			expect(StateService.setLookupUpdateMode).not.toHaveBeenCalled();
		}));
	});

	describe('add datasets to lookup', () => {
		it('should disable datasets which are used in recipe steps', inject(($rootScope, LookupService) => {
			//given
			stateMock.playground.lookup.datasets = [
				{ id: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7' },
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
				{ id: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7', addedToLookup: false, created: 80 },
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
			spyOn(StateService, 'setLookupDatasets');

			//when
			LookupService.initLookups();
			$rootScope.$digest();

			//then
			expect(StorageService.setLookupDatasets).toHaveBeenCalledWith(['1', '9e739b88-5ec9-4b58-84b5-2127a7e2eac7']);

			expect(stateMock.playground.lookup.datasets[0].addedToLookup).toBe(true);
			expect(stateMock.playground.lookup.datasets[1].addedToLookup).toBe(false);
			expect(stateMock.playground.lookup.datasets[2].addedToLookup).toBe(false);

			expect(StateService.setLookupAddedActions).toHaveBeenCalledWith(lookupActions);
		}));

		it('should update lookup datasets', inject(($q, $rootScope, LookupService, StorageService, StateService) => {
			//given
			stateMock.playground.lookup.datasets = [
				{ id: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7', addedToLookup: true, created: 100 },
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
			expect(StateService.setLookupAddedActions).toHaveBeenCalledWith(lookupActions);

			expect(StorageService.setLookupDatasets).toHaveBeenCalledWith(['9e739b88-5ec9-4b58-84b5-2127a7e2eac7', '4']);
		}));
	});
});
