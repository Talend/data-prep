/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import {
	HOME_PREPARATIONS_ROUTE,
	HOME_DATASETS_ROUTE,
} from '../../index-route';

import {
	EVENT_LOADING_START,
	EVENT_LOADING_STOP,
} from './playground-service';

describe('Playground Service', () => {
	const datasetColumnsWithoutStatistics = {
		columns: [{ id: '0001', statistics: { frequencyTable: [] } }],
		records: [],
		data: [],
	};
	const datasetColumns = {
		metadata: {
			id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
			name: 'customers_jso_light',
			author: 'anonymousUser',
			records: 15,
			nbLinesHeader: 1,
			nbLinesFooter: 0,
			created: '03-30-2015 08:06',
			columns: [{ id: '0001', statistics: { frequencyTable: [{ toto: 2 }] } }],
		},
		records: [],
		data: [],
	};
	const datasetMetadata = {
		records: 19,
		columns: [{ id: '0001', statistics: { frequencyTable: [{ toto: 2 }] } }],
	};

	const preparationMetadata = {
		metadata: {
			columns: [{ id: '0001', statistics: { frequencyTable: [{ toto: 2 }] } }],
		},
		records: [{}],
	};
	const preparationMetadataWithoutStatistics = {
		metadata: {
			columns: [{ id: '0001', statistics: { frequencyTable: [] } }],
		},
		records: [],
	};

	const datasets = [
		{
			id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
			name: 'customers_jso_light',
			author: 'anonymousUser',
			records: 15,
			nbLinesHeader: 1,
			nbLinesFooter: 0,
			created: '03-30-2015 08:06',
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
	];

	const preparations = [
		{
			id: 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
			dataSetId: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
			author: 'anonymousUser',
			creationDate: 1427447300300,
		},
		{
			id: 'fbaa18e82e913e97e5f0e9d40f04413412be1126',
			dataSetId: '3b21388c-f54a-4334-9bef-748912d0806f',
			author: 'anonymousUser',
			creationDate: 1427447330693,
		},
	];

	let stateMock;

	beforeEach(angular.mock.module('data-prep.services.playground', ($provide) => {
		stateMock = {
			route: {
				previous: HOME_PREPARATIONS_ROUTE,
			},
			playground: {
				recipe: {
					initialStep: { transformation: { stepId: 'INITIAL_STEP_ID' } },
					current: { steps: [] }
				},
				filter: {},
				grid: {},
				sampleType: 'HEAD',
				data: {
					metadata: {
						columns: [{ id: '0001', statistics: { frequencyTable: [{ toto: 2 }] } }],
					},
				},
			},
			inventory: {
				homeFolder: { id: 'Lw==' },
				currentFolder: { path: 'test' },
				folder: {
					metadata: {
						id: 'abcd'
					}
				}
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {
			PREPARATION: 'Preparation',
			TALEND: 'TALEND',
		});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($q, $state, StateService, DatasetService, RecipeService, DatagridService,
	                   PreparationService, TransformationCacheService, ExportService,
	                   HistoryService, PreviewService, FilterService, TitleService) => {
		stateMock.playground.preparationName = '';

		spyOn($state, 'go').and.returnValue();
		spyOn(DatagridService, 'updateData').and.returnValue();
		spyOn(DatasetService, 'getContent').and.returnValue($q.when(datasetColumns));
		spyOn(DatasetService, 'updateParameters').and.returnValue($q.when());
		spyOn(HistoryService, 'addAction').and.returnValue();
		spyOn(HistoryService, 'clear').and.returnValue();
		spyOn(ExportService, 'refreshTypes').and.returnValue();
		spyOn(PreparationService, 'create').and.returnValue($q.when(preparations[0].id));
		spyOn(PreparationService, 'getDetails').and.returnValue($q.when(preparations[0]));
		spyOn(PreparationService, 'setHead').and.returnValue($q.when());
		spyOn(PreparationService, 'setName').and.returnValue($q.when(preparations[0]));
		spyOn(PreviewService, 'reset').and.returnValue();
		spyOn(RecipeService, 'refresh').and.returnValue($q.when());
		spyOn(StateService, 'disableRecipeStepsAfter').and.returnValue();
		spyOn(StateService, 'resetPlayground').and.returnValue();
		spyOn(StateService, 'setCurrentData').and.returnValue();
		spyOn(StateService, 'setCurrentDataset').and.returnValue();
		spyOn(StateService, 'setCurrentPreparation').and.returnValue();
		spyOn(StateService, 'setCurrentSampleType').and.returnValue();
		spyOn(StateService, 'setPreparationName').and.returnValue();
		spyOn(StateService, 'setNameEditionMode').and.returnValue();
		spyOn(StateService, 'showRecipe').and.returnValue();
		spyOn(StateService, 'hideRecipe').and.returnValue();
		spyOn(TransformationCacheService, 'invalidateCache').and.returnValue();
		spyOn(FilterService, 'initFilters').and.returnValue();
		spyOn(TitleService, 'setStrict').and.returnValue();
	}));

	describe('update preparation', () => {
		it('should set new name to the preparation name',
			inject(($rootScope, PlaygroundService, PreparationService, TitleService, StateService) => {
				// given
				const name = 'My preparation';
				const newName = 'My new preparation name';

				PlaygroundService.preparationName = name;
				stateMock.playground.dataset = { id: '123d120394ab0c53' };
				stateMock.playground.preparation = { id: 'e85afAa78556d5425bc2' };

				// when
				PlaygroundService.createOrUpdatePreparation(newName);
				$rootScope.$digest();

				// then
				expect(PreparationService.create).not.toHaveBeenCalled();
				expect(PreparationService.setName).toHaveBeenCalledWith('e85afAa78556d5425bc2', newName);
				expect(StateService.setPreparationName).toHaveBeenCalledWith(preparations[0].name);
				expect(TitleService.setStrict).toHaveBeenCalledWith(preparations[0].name);
			}
		));

		describe('history', () => {
			let undo;
			let redo;
			const oldName = 'My preparation';
			const newName = 'My new preparation name';

			beforeEach(inject(($rootScope, PlaygroundService, HistoryService) => {
				// given
				PlaygroundService.preparationName = oldName;
				stateMock.playground.dataset = { id: '123d120394ab0c53' };
				stateMock.playground.preparation = { id: 'e85afAa78556d5425bc2', name: oldName };

				// when
				PlaygroundService.createOrUpdatePreparation(newName);
				$rootScope.$digest();

				// then
				undo = HistoryService.addAction.calls.argsFor(0)[0];
				redo = HistoryService.addAction.calls.argsFor(0)[1];
			}));

			it('should add undo/redo actions after append transformation', inject((HistoryService) => {
				// then
				expect(HistoryService.addAction).toHaveBeenCalled();
			}));

			it('should change preparation name on UNDO', inject((PreparationService) => {
				// given
				expect(PreparationService.setName.calls.count()).toBe(1);

				// when
				undo();

				// then
				expect(PreparationService.setName.calls.count()).toBe(2);
				expect(PreparationService.setName.calls.argsFor(1)).toEqual([stateMock.playground.preparation.id, oldName]);
			}));

			it('should change preparation name on REDO', inject((PreparationService) => {
				// given
				expect(PreparationService.setName.calls.count()).toBe(1);

				// when
				redo();

				// then
				expect(PreparationService.setName.calls.count()).toBe(2);
				expect(PreparationService.setName.calls.argsFor(1)).toEqual([stateMock.playground.preparation.id, newName]);
			}));
		});
	});

	describe('load dataset', () => {
		let assertNewPreparationInitialization;

		beforeEach(inject(($rootScope, TransformationCacheService, ExportService,
		                   HistoryService, FilterService, TitleService,
		                   PreviewService, StateService) => {
			spyOn($rootScope, '$emit').and.returnValue();
			assertNewPreparationInitialization = () => {
				expect(StateService.resetPlayground).toHaveBeenCalled();
				expect(StateService.setCurrentDataset).toHaveBeenCalledWith(datasetColumns.metadata);
				expect(StateService.setCurrentData).toHaveBeenCalledWith(datasetColumns);
				expect(StateService.setCurrentSampleType).toHaveBeenCalledWith('HEAD');
				expect(StateService.setNameEditionMode).toHaveBeenCalledWith(false);
				expect(TransformationCacheService.invalidateCache).toHaveBeenCalled();
				expect(HistoryService.clear).toHaveBeenCalled();
				expect(PreviewService.reset).toHaveBeenCalledWith(false);
				expect(FilterService.initFilters).toHaveBeenCalled();
				expect(ExportService.refreshTypes).toHaveBeenCalledWith('datasets', datasetColumns.metadata.id);
				expect(TitleService.setStrict).toHaveBeenCalledWith(datasetColumns.metadata.name);
			};
		}));

		it('should init playground', inject(($rootScope, PlaygroundService, PreparationService) => {
			// given
			expect(PreparationService.preparationName).toBeFalsy();

			// when
			PlaygroundService.loadDataset(datasetColumns.metadata.id);
			$rootScope.$digest();

			// then
			assertNewPreparationInitialization();
		}));

		it('should manage loading spinner', inject(($rootScope, PlaygroundService) => {
			// given
			expect($rootScope.$emit).not.toHaveBeenCalled();

			// when
			PlaygroundService.loadDataset(datasetColumns.metadata.id);
			expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_START);
			$rootScope.$digest();

			// then
			expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_STOP);
		}));

		it('should not stop spinner if it has been started more than one time', inject(($rootScope, PlaygroundService) => {
			// given
			expect($rootScope.$emit).not.toHaveBeenCalled();

			// when
			PlaygroundService.loadDataset(datasetColumns.metadata.id);
			PlaygroundService.loadDataset(datasetColumns.metadata.id);
			$rootScope.$digest();

			// then
			expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_START);
			expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_STOP);
			expect($rootScope.$emit).toHaveBeenCalledTimes(2);
		}));

		it('should reset preparation name', inject(($rootScope, PlaygroundService, StateService) => {
			// given
			PlaygroundService.preparationName = 'preparation name';

			// when
			PlaygroundService.loadDataset(datasetColumns.metadata.id);
			$rootScope.$digest();

			// then
			expect(StateService.setPreparationName).toHaveBeenCalledWith(datasetColumns.metadata.name);
		}));

		it('should start playground unboarding tour', inject(($rootScope, $timeout, PlaygroundService, OnboardingService) => {
			// given
			spyOn(OnboardingService, 'shouldStartTour').and.returnValue(true);
			spyOn(OnboardingService, 'startTour').and.returnValue();
			PlaygroundService.preparationName = 'preparation name';

			// when
			PlaygroundService.loadDataset(datasetColumns.metadata.id);
			$rootScope.$digest();
			$timeout.flush(300);

			// then
			expect(OnboardingService.shouldStartTour).toHaveBeenCalledWith('playground');
			expect(OnboardingService.startTour).toHaveBeenCalledWith('playground', 1500);
		}));

		it('should NOT start playground unboarding tour', inject(($rootScope, $timeout, PlaygroundService, OnboardingService) => {
			// given
			spyOn(OnboardingService, 'shouldStartTour').and.returnValue(false);
			spyOn(OnboardingService, 'startTour').and.returnValue();
			PlaygroundService.preparationName = 'preparation name';

			// when
			PlaygroundService.loadDataset(datasetColumns.metadata.id);
			$rootScope.$digest();
			$timeout.flush(300);

			// then
			expect(OnboardingService.shouldStartTour).toHaveBeenCalledWith('playground');
			expect(OnboardingService.startTour).not.toHaveBeenCalled();
		}));
	});

	describe('load preparation', () => {
		const dataset = { id: '1', name: 'my dataset' };
		const preparation = {
			id: '6845521254541',
			dataSetId: '1',
			name: 'prep1',
		};
		const data = {
			columns: [{ id: '0001' }],
			records: [{ id: '0', firstname: 'toto' }, { id: '1', firstname: 'tata' }, { id: '2', firstname: 'titi' }],
		};
		let assertDatasetLoadInitialized;
		let assertDatasetLoadNotInitialized;

		beforeEach(inject(($rootScope, $q, StateService, ExportService,
		                   PreparationService, RecipeService, StorageService, TitleService,
		                   TransformationCacheService, HistoryService, PreviewService, FilterService) => {
			spyOn($rootScope, '$emit').and.returnValue();
			spyOn(PreparationService, 'getContent').and.returnValue($q.when(data));
			spyOn(StorageService, 'getSelectedColumns').and.returnValue(["0001"]);
			spyOn(StateService, 'setGridSelection').and.returnValue();

			stateMock.playground.grid.columns = data.columns;

			assertDatasetLoadInitialized = (metadata, data) => {
				expect(StateService.resetPlayground).toHaveBeenCalled();
				expect(StateService.setCurrentDataset).toHaveBeenCalledWith(metadata);
				expect(StateService.setCurrentData).toHaveBeenCalledWith(data);
				expect(StateService.setCurrentSampleType).toHaveBeenCalledWith('HEAD');
				expect(RecipeService.refresh).toHaveBeenCalled();
				expect(TransformationCacheService.invalidateCache).toHaveBeenCalled();
				expect(HistoryService.clear).toHaveBeenCalled();
				expect(PreviewService.reset).toHaveBeenCalledWith(false);
				expect(FilterService.initFilters).toHaveBeenCalled();
				expect(StateService.setGridSelection).toHaveBeenCalledWith([{ id: '0001' }]);
				expect(ExportService.refreshTypes).toHaveBeenCalledWith('preparations', preparation.id);
				expect(TitleService.setStrict).toHaveBeenCalledWith('prep1');
			};

			assertDatasetLoadNotInitialized = () => {
				expect(StateService.resetPlayground).not.toHaveBeenCalled();
				expect(StateService.setCurrentDataset).not.toHaveBeenCalled();
				expect(StateService.setCurrentData).not.toHaveBeenCalled();
				expect(StateService.setCurrentSampleType).not.toHaveBeenCalled();
				expect(StateService.showRecipe).not.toHaveBeenCalled();
				expect(RecipeService.refresh).not.toHaveBeenCalled();
				expect(TransformationCacheService.invalidateCache).not.toHaveBeenCalled();
				expect(HistoryService.clear).not.toHaveBeenCalled();
				expect(PreviewService.reset).not.toHaveBeenCalled();
				expect(FilterService.initFilters).not.toHaveBeenCalled();
				expect(StateService.setGridSelection).not.toHaveBeenCalled();
				expect(ExportService.refreshTypes).not.toHaveBeenCalled();
				expect(TitleService.setStrict).toHaveBeenCalledWith('DATA_PREPARATION');
			};
		}));

		it('should load existing preparation', inject(($rootScope, PlaygroundService) => {
			// given
			stateMock.playground.preparation = { id: '5746518486846' };
			stateMock.playground.dataset = dataset;

			// when
			PlaygroundService.loadPreparation(preparation);
			$rootScope.$apply();

			// then
			assertDatasetLoadInitialized(dataset, data);
		}));

		it('should manage loading spinner on preparation load', inject(($rootScope, PlaygroundService) => {
			// given
			stateMock.playground.preparation = { id: '5746518486846' };

			// when
			PlaygroundService.loadPreparation(preparation);
			expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_START);
			$rootScope.$apply();

			// then
			expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_STOP);
		}));

		it('should load existing preparation with simulated dataset metadata when its metadata is not set yet',
			inject(($rootScope, PlaygroundService) => {
				// given
				stateMock.playground.preparation = { id: '5746518486846' };

				// when
				PlaygroundService.loadPreparation(preparation);
				$rootScope.$apply();

				// then
				assertDatasetLoadInitialized({ id: '1' }, data);
			})
		);

		it('should start preparation tour',
			inject(($rootScope, $timeout, OnboardingService, PlaygroundService) => {
				// given
				spyOn(OnboardingService, 'shouldStartTour').and.returnValue(true);
				spyOn(OnboardingService, 'startTour').and.returnValue();

				stateMock.playground.preparation = { id: '5746518486846' };

				// when
				PlaygroundService.loadPreparation(preparation);
				$rootScope.$apply();
				$timeout.flush(1500);

				// then
				expect(OnboardingService.shouldStartTour).toHaveBeenCalledWith('playground');
				expect(OnboardingService.startTour).toHaveBeenCalledWith('playground', 1500);
			})
		);

		it('should load preparation content at a specific step',
			inject(($rootScope, StateService, PlaygroundService, RecipeService, DatagridService, PreviewService) => {
				// given
				const step = {
					column: { id: '0000' },
					transformation: { stepId: 'a4353089cb0e039ac2' },
				};
				stateMock.playground.dataset = dataset;
				stateMock.playground.preparation = preparation;

				// when
				PlaygroundService.loadStep(step);
				expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_START);
				$rootScope.$apply();

				// then
				expect(StateService.resetPlayground).not.toHaveBeenCalled();
				expect(StateService.setCurrentDataset).not.toHaveBeenCalled();
				expect(RecipeService.refresh).not.toHaveBeenCalled();
				expect(StateService.disableRecipeStepsAfter).toHaveBeenCalledWith(step);
				expect(PreviewService.reset).toHaveBeenCalledWith(false);
				expect(DatagridService.updateData).toHaveBeenCalledWith(data);
				expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_STOP);
			})
		);
	});

	describe('fetch dataset statistics', () => {
		beforeEach(inject((StateService, StatisticsService) => {
			spyOn(StatisticsService, 'updateStatistics').and.returnValue();
			spyOn(StateService, 'updateDatasetStatistics').and.returnValue();
			spyOn(StateService, 'updateDatasetRecord').and.returnValue();
		}));

		it('should get metadata and set its statistics in state', inject(($rootScope, $q, PlaygroundService, DatasetService, StateService) => {
			// given
			spyOn(DatasetService, 'getMetadata').and.returnValue($q.when(datasetMetadata));
			stateMock.playground.dataset = { id: '1324d56456b84ef154', records: 15 };
			stateMock.playground.preparation = null;

			// when
			PlaygroundService.updateStatistics();
			$rootScope.$digest();

			// then
			expect(DatasetService.getMetadata).toHaveBeenCalledWith('1324d56456b84ef154');
			expect(StateService.updateDatasetStatistics).toHaveBeenCalledWith(datasetMetadata);
			expect(StateService.updateDatasetRecord).toHaveBeenCalledWith(19);
		}));

		it('should trigger statistics update', inject(($rootScope, $q, DatasetService, PlaygroundService, StatisticsService) => {
			// given
			spyOn(DatasetService, 'getMetadata').and.returnValue($q.when(datasetMetadata));
			stateMock.playground.dataset = { id: '1324d56456b84ef154' };
			stateMock.playground.preparation = null;


			// when
			PlaygroundService.updateStatistics();
			$rootScope.$digest();

			// then
			expect(StatisticsService.updateStatistics).toHaveBeenCalled();
		}));

		it('should reject promise when the statistics are not computed yet', inject(($rootScope, $q, PlaygroundService, DatasetService, StateService) => {
			// given
			let rejected = false;
			spyOn(DatasetService, 'getMetadata').and.returnValue($q.when(datasetColumnsWithoutStatistics));
			stateMock.playground.dataset = { id: '1324d56456b84ef154' };
			stateMock.playground.preparation = null;

			// when
			PlaygroundService.updateStatistics()
				.catch(() => {
					rejected = true;
				});
			$rootScope.$digest();

			// then
			expect(StateService.updateDatasetStatistics).not.toHaveBeenCalled();
			expect(rejected).toBe(true);
		}));
	});

	describe('fetch preparation statistics', () => {
		beforeEach(inject((StateService, StatisticsService) => {
			spyOn(StatisticsService, 'updateStatistics').and.returnValue();
			spyOn(StateService, 'updateDatasetStatistics').and.returnValue();

			stateMock.playground.preparation = { id: 'abc' };
			stateMock.playground.dataset = { id: '1324d56456b84ef154', records: 15 };
		}));

		it('should get metadata and set its statistics in state', inject(($rootScope, $q, PlaygroundService, PreparationService, StateService) => {
			// given
			spyOn(PreparationService, 'getMetadata').and.returnValue($q.when(preparationMetadata.metadata));

			// when
			PlaygroundService.updateStatistics();
			$rootScope.$digest();

			// then
			expect(PreparationService.getMetadata).toHaveBeenCalledWith('abc', 'head');
			expect(StateService.updateDatasetStatistics).toHaveBeenCalledWith(preparationMetadata.metadata);
		}));

		it('should trigger statistics update', inject(($rootScope, $q, PreparationService, PlaygroundService, StatisticsService) => {
			// given
			spyOn(PreparationService, 'getMetadata').and.returnValue($q.when(preparationMetadata.metadata));

			// when
			PlaygroundService.updateStatistics();
			$rootScope.$digest();

			// then
			expect(StatisticsService.updateStatistics).toHaveBeenCalled();
		}));

		it('should reject promise when the statistics are not computed yet', inject(($rootScope, $q, PlaygroundService, PreparationService, StateService) => {
			// given
			let rejected = false;
			spyOn(PreparationService, 'getMetadata').and.returnValue($q.when(preparationMetadataWithoutStatistics.metadata));

			// when
			PlaygroundService.updateStatistics()
				.catch(() => {
					rejected = true;
				});
			$rootScope.$digest();

			// then
			expect(StateService.updateDatasetStatistics).not.toHaveBeenCalled();
			expect(rejected).toBe(true);
		}));
	});

	describe('preparation steps', () => {
		let preparationHeadContent;
		let metadata;
		const lastStepId = 'a151e543456413ef51';
		const previousLastStepId = '3248fa65e45f588cb464';
		const lastStep = { transformation: { stepId: lastStepId } };
		const previousLastStep = { transformation: { stepId: previousLastStepId } };
		const previousStep = { column: { id: '0003' } };
		beforeEach(inject(($rootScope, $q, PlaygroundService, PreparationService, StepUtilsService) => {
			preparationHeadContent = {
				records: [{
					firstname: 'Grover',
					avgAmount: '82.4',
					city: 'BOSTON',
					birth: '01-09-1973',
					registration: '17-02-2008',
					id: '1',
					state: 'AR',
					nbCommands: '41',
					lastname: 'Quincy',
				}, {
					firstname: 'Warren',
					avgAmount: '87.6',
					city: 'NASHVILLE',
					birth: '11-02-1960',
					registration: '18-08-2007',
					id: '2',
					state: 'WA',
					nbCommands: '17',
					lastname: 'Johnson',
				},],
			};

			metadata = { id: 'e85afAa78556d5425bc2' };
			stateMock.playground.dataset = metadata;

			spyOn($rootScope, '$emit').and.returnValue();
			spyOn(PreparationService, 'appendStep').and.returnValue($q.when());
			spyOn(PreparationService, 'updateStep').and.returnValue($q.when());
			spyOn(PreparationService, 'moveStep').and.returnValue($q.when());
			spyOn(PreparationService, 'removeStep').and.returnValue($q.when());
			spyOn(PreparationService, 'getContent').and.returnValue($q.when(preparationHeadContent));
			spyOn(StepUtilsService, 'getLastStep').and.returnValue(lastStep);
			spyOn(StepUtilsService, 'getPreviousStep').and.callFake((recipeState, step) => {
				return step === lastStep ? previousLastStep : previousStep;
			});
		}));

		describe('append', () => {
			it('should create a preparation when there is no preparation yet', inject(($rootScope, PlaygroundService, PreparationService) => {
				// given
				stateMock.playground.dataset = {
					id: '76a415cf854d8654',
					name: 'my dataset name',
				};
				stateMock.playground.preparation = null;

				const parameters = {
					param1: 'param1Value',
					param2: 4,
					scope: 'column',
					column_id: '0001',
					column_name: 'firstname',
				};
				const actions = [{ action: 'uppercase', parameters }];

				expect(preparations[0].draft).toBeFalsy();

				// when
				PlaygroundService.appendStep(actions);
				stateMock.playground.preparation = preparations[0]; // emulate created preparation set in state
				$rootScope.$digest();

				// then
				expect(preparations[0].draft).toBe(true);
				expect(PreparationService.create).toHaveBeenCalledWith('76a415cf854d8654', 'my dataset name Preparation', 'Lw==');
				expect(PreparationService.getDetails).toHaveBeenCalledWith(preparations[0].id);
			}));

			it('should append step to the new created preparation', inject(($rootScope, PlaygroundService, PreparationService) => {
				// given
				stateMock.playground.dataset = { id: '76a415cf854d8654' };
				stateMock.playground.preparation = null;
				const parameters = {
					param1: 'param1Value',
					param2: 4,
					scope: 'column',
					column_id: '0001',
					column_name: 'firstname',
				};
				const actions = [{ action: 'uppercase', parameters }];


				// when
				PlaygroundService.appendStep(actions);
				stateMock.playground.preparation = preparations[0]; // emulate created preparation set in state
				$rootScope.$digest();

				// then
				expect(PreparationService.appendStep).toHaveBeenCalledWith(preparations[0].id, actions);
			}));

			it('should append step to an existing preparation', inject(($rootScope, PlaygroundService, PreparationService) => {
				// given
				stateMock.playground.preparation = { id: '15de46846f8a46' };
				const parameters = {
					param1: 'param1Value',
					param2: 4,
					scope: 'column',
					column_id: '0001',
					column_name: 'firstname',
				};
				const actions = [{ action: 'uppercase', parameters: parameters }];

				// when
				PlaygroundService.appendStep(actions);
				$rootScope.$digest();

				// then
				expect(PreparationService.appendStep).toHaveBeenCalledWith('15de46846f8a46', actions);
			}));

			it('should show/hide loading', inject(($rootScope, PlaygroundService) => {
				// given
				stateMock.playground.preparation = { id: '15de46846f8a46' };
				const parameters = {
					param1: 'param1Value',
					param2: 4,
					scope: 'column',
					column_id: '0001',
					column_name: 'firstname',
				};
				const actions = [{ action: 'uppercase', parameters: parameters }];

				// when
				PlaygroundService.appendStep(actions);
				expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_START);
				$rootScope.$digest();

				// then
				expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_STOP);
			}));

			it('should refresh recipe', inject(($rootScope, PlaygroundService, RecipeService) => {
				// given
				stateMock.playground.preparation = { id: '15de46846f8a46' };
				const parameters = {
					param1: 'param1Value',
					param2: 4,
					scope: 'column',
					column_id: '0001',
					column_name: 'firstname',
				};
				const actions = [{ action: 'uppercase', parameters: parameters }];
				// when
				PlaygroundService.appendStep(actions);
				$rootScope.$digest();

				// then
				expect(RecipeService.refresh).toHaveBeenCalled();
			}));

			it('should refresh datagrid with head content', inject(($rootScope, PlaygroundService, PreparationService, DatagridService, PreviewService) => {
				// given
				stateMock.playground.preparation = { id: '15de46846f8a46' };
				const parameters = {
					param1: 'param1Value',
					param2: 4,
					scope: 'column',
					column_id: '0001',
					column_name: 'firstname',
				};
				const actions = [{ action: 'uppercase', parameters: parameters }];

				// when
				PlaygroundService.appendStep(actions);
				$rootScope.$digest();

				// then
				expect(PreparationService.getContent).toHaveBeenCalledWith('15de46846f8a46', 'head', 'HEAD');
				expect(DatagridService.updateData).toHaveBeenCalledWith(preparationHeadContent);
				expect(PreviewService.reset).toHaveBeenCalledWith(false);
			}));

			describe('history', () => {
				let undo;
				const preparationId = '15de46846f8a46';

				beforeEach(inject((StepUtilsService) => {
					spyOn(StepUtilsService, 'getLastActiveStep').and.returnValue(lastStep); // loaded step is the last step
				}));

				beforeEach(inject(($rootScope, PlaygroundService, HistoryService) => {
					// given
					stateMock.playground.preparation = { id: preparationId };
					const parameters = {
						param1: 'param1Value',
						param2: 4,
						scope: 'column',
						column_id: '0001',
						column_name: 'firstname',
					};
					const actions = [{ action: 'uppercase', parameters: parameters }];

					expect(HistoryService.addAction).not.toHaveBeenCalled();

					// when
					PlaygroundService.appendStep(actions);
					$rootScope.$digest();

					// then
					undo = HistoryService.addAction.calls.argsFor(0)[0];
				}));

				it('should add undo/redo actions after append transformation', inject((HistoryService) => {
					// then
					expect(HistoryService.addAction).toHaveBeenCalled();
				}));

				it('should set preparation head to previous head on UNDO', inject(($rootScope, PreparationService) => {
					// given
					expect(PreparationService.setHead).not.toHaveBeenCalled();

					// when
					undo();

					// then
					expect(PreparationService.setHead).toHaveBeenCalledWith(preparationId, lastStepId);
				}));

				it('should refresh recipe on UNDO', inject(($rootScope, DatagridService, RecipeService) => {
					// given
					expect(RecipeService.refresh.calls.count()).toBe(1);

					// when
					undo();
					$rootScope.$digest();

					// then
					expect(RecipeService.refresh.calls.count()).toBe(2);
				}));

				it('should refresh datagrid content on UNDO', inject(($rootScope, PreparationService, DatagridService) => {
					// given
					expect(PreparationService.getContent.calls.count()).toBe(1);
					expect(DatagridService.updateData.calls.count()).toBe(1);

					// when
					undo();
					$rootScope.$digest();

					// then
					expect(PreparationService.getContent.calls.count()).toBe(2);
					expect(PreparationService.getContent.calls.argsFor(1)[0]).toBe('15de46846f8a46');
					expect(PreparationService.getContent.calls.argsFor(1)[1]).toBe('head');
					expect(PreparationService.getContent.calls.argsFor(1)[2]).toBe('HEAD');
					expect(DatagridService.focusedColumn).toBeFalsy();
					expect(DatagridService.updateData.calls.count()).toBe(2);
					expect(DatagridService.updateData.calls.argsFor(1)[0]).toBe(preparationHeadContent);
				}));
			});
		});

		describe('update', () => {
			const lastActiveIndex = 5;
			const lastActiveStep = {
				column: { id: '0000' },
				transformation: { stepId: '24a457bc464e645' },
				actionParameters: {
					action: 'touppercase',
				},
			};
			const oldParameters = { value: 'toto', column_id: '0001' };
			const stepToUpdate = {
				column: { id: '0001' },
				transformation: { stepId: '98a7565e4231fc2c7' },
				actionParameters: {
					action: 'delete_on_value',
					parameters: oldParameters,
				},
			};

			beforeEach(inject((StepUtilsService) => {
				spyOn(StepUtilsService, 'getActiveThresholdStepIndex').and.returnValue(lastActiveIndex);
				spyOn(StepUtilsService, 'getStep').and.callFake((recipeState, index) => {
					if (index === lastActiveIndex) {
						return lastActiveStep;
					}

					return stepToUpdate;
				});
			}));

			it('should not update preparation step when parameters are not changed', inject(($rootScope, PlaygroundService, PreparationService) => {
				// given
				stateMock.playground.preparation = { id: '456415ae348e6046dc' };
				const parameters = { value: 'toto', column_id: '0001' };

				// when
				PlaygroundService.updateStep(stepToUpdate, parameters);

				// then
				expect(PreparationService.updateStep).not.toHaveBeenCalled();
			}));

			it('should update preparation step when parameters are different', inject(($rootScope, PlaygroundService, PreparationService) => {
				// given
				stateMock.playground.preparation = { id: '456415ae348e6046dc' };
				const parameters = { value: 'tata', column_id: '0001' };

				// when
				PlaygroundService.updateStep(stepToUpdate, parameters);

				// then
				expect(PreparationService.updateStep).toHaveBeenCalledWith('456415ae348e6046dc', stepToUpdate, parameters);
			}));

			it('should show/hide loading', inject(($rootScope, PlaygroundService) => {
				// given
				stateMock.playground.preparation = { id: '456415ae348e6046dc' };
				const parameters = { value: 'tata', column_id: '0001' };

				// when
				PlaygroundService.updateStep(stepToUpdate, parameters);
				expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_START);
				$rootScope.$digest();

				// then
				expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_STOP);
			}));

			it('should refresh recipe', inject(($rootScope, PlaygroundService, RecipeService) => {
				// given
				stateMock.playground.preparation = { id: '456415ae348e6046dc' };
				const parameters = { value: 'tata', column_id: '0001' };

				// when
				PlaygroundService.updateStep(stepToUpdate, parameters);
				$rootScope.$digest();

				// then
				expect(RecipeService.refresh).toHaveBeenCalled();
			}));

			it('should load previous last active step', inject(($rootScope, PlaygroundService, PreparationService, DatagridService, PreviewService) => {
				// given
				const preparation = { id: '456415ae348e6046dc' };
				stateMock.playground.preparation = preparation;
				const parameters = { value: 'tata', column_id: '0001' };

				// when
				PlaygroundService.updateStep(stepToUpdate, parameters);
				$rootScope.$digest();

				// then
				expect(PreparationService.getContent).toHaveBeenCalledWith(preparation.id, lastActiveStep.transformation.stepId, 'HEAD');
				expect(DatagridService.updateData).toHaveBeenCalledWith(preparationHeadContent);
				expect(PreviewService.reset).toHaveBeenCalledWith(false);
			}));

			describe('history', () => {
				let undo;
				const preparationId = '456415ae348e6046dc';

				beforeEach(inject((StepUtilsService) => {
					spyOn(StepUtilsService, 'getLastActiveStep').and.returnValue(lastActiveStep); // loaded step is not the last step
				}));

				beforeEach(inject(($rootScope, PlaygroundService, HistoryService) => {
					// given
					stateMock.playground.preparation = { id: preparationId };
					const parameters = { value: 'tata', column_id: '0001' };

					// when
					PlaygroundService.updateStep(stepToUpdate, parameters);
					$rootScope.$digest();

					// then
					undo = HistoryService.addAction.calls.argsFor(0)[0];
				}));

				it('should add undo/redo actions after update transformation', inject((HistoryService) => {
					// then
					expect(HistoryService.addAction).toHaveBeenCalled();
				}));

				it('should set preparation head to previous head on UNDO', inject(($rootScope, PreparationService) => {
					// given
					expect(PreparationService.setHead).not.toHaveBeenCalled();

					// when
					undo();

					// then
					expect(PreparationService.setHead).toHaveBeenCalledWith(preparationId, lastStepId);
				}));

				it('should refresh recipe on UNDO', inject(($rootScope, RecipeService) => {
					// given
					expect(RecipeService.refresh.calls.count()).toBe(1);

					// when
					undo();
					$rootScope.$digest();

					// then
					expect(RecipeService.refresh.calls.count()).toBe(2);
				}));

				it('should refresh datagrid content at the last active step on UNDO', inject(($rootScope, PreparationService, DatagridService) => {
					// given
					expect(PreparationService.getContent.calls.count()).toBe(1);
					expect(DatagridService.updateData.calls.count()).toBe(1);

					// when
					undo();
					$rootScope.$digest();

					// then
					expect(PreparationService.getContent.calls.count()).toBe(2);
					expect(PreparationService.getContent.calls.argsFor(1)[0]).toBe(preparationId);
					expect(PreparationService.getContent.calls.argsFor(1)[1]).toBe(lastActiveStep.transformation.stepId);
					expect(DatagridService.updateData.calls.count()).toBe(2);
					expect(DatagridService.updateData.calls.argsFor(1)[0]).toBe(preparationHeadContent);
				}));
			});
		});

		describe('reorder', () => {
			const preparationId = 'PREPARATION_ID';
			const stepIdToMove = 'STEP_ID_3';
			const previousPosition = 2;
			const nextPosition = 1;
			const nextParentId = 'STEP_ID_1';

			beforeEach(inject((StepUtilsService) => {
				stateMock.playground.preparation = { id: preparationId };
				stateMock.playground.recipe.current.steps = [
					{
						column: { id: '0001' },
						transformation: { stepId: 'STEP_ID_1' },
						actionParameters: {
							action: 'lorem_ipsum',
							parameters: { value: 'toto', column_id: '0001' },
						},
					},
					{
						column: { id: '0001' },
						transformation: { stepId: 'STEP_ID_2' },
						actionParameters: {
							action: 'lorem_ipsum',
							parameters: { value: 'tata', column_id: '0001' },
						},
					},
					{
						column: { id: '0001' },
						transformation: { stepId: 'STEP_ID_3' },
						actionParameters: {
							action: 'lorem_ipsum',
							parameters: { value: 'titi', column_id: '0001' },
						},
					},
				];

				spyOn(StepUtilsService, 'getStep').and.callFake((recipe, stepId) => {
					return stateMock.playground.recipe.current.steps[stepId] || stateMock.playground.recipe.initialStep;
				});
			}));

			it('should not reorder steps if step does not move', inject(($rootScope, PlaygroundService, PreparationService) => {
				// given
				const previousPosition = 1;
				const nextPosition = 1;

				// when
				PlaygroundService.updateStepOrder(previousPosition, nextPosition);

				// then
				expect(PreparationService.moveStep).not.toHaveBeenCalled();
			}));

			it('should not move step up if it is already the first one', inject(($rootScope, PlaygroundService, PreparationService) => {
				// given
				const previousPosition = 0;
				const nextPosition = -1;

				// when
				PlaygroundService.updateStepOrder(previousPosition, nextPosition);

				// then
				expect(PreparationService.moveStep).not.toHaveBeenCalled();
			}));

			it('should move step up', inject(($rootScope, PlaygroundService, PreparationService) => {
				// when
				PlaygroundService.updateStepOrder(previousPosition, nextPosition);

				// then
				expect(PreparationService.moveStep).toHaveBeenCalledWith(preparationId, stepIdToMove, nextParentId);
			}));

			it('should move step up by selecting initial step as next parent if step becomes the first', inject(($rootScope, PlaygroundService, PreparationService) => {
				// given
				const nextPosition = 0;
				const nextParentId = 'INITIAL_STEP_ID';

				// when
				PlaygroundService.updateStepOrder(previousPosition, nextPosition);

				// then
				expect(PreparationService.moveStep).toHaveBeenCalledWith(preparationId, stepIdToMove, nextParentId);
			}));

			it('should move step down', inject(($rootScope, PlaygroundService, PreparationService) => {
				// given
				const stepIdToMove = 'STEP_ID_2';
				const previousPosition = 1;
				const nextPosition = 2;
				const nextParentId = 'STEP_ID_3';

				// when
				PlaygroundService.updateStepOrder(previousPosition, nextPosition);

				// then
				expect(PreparationService.moveStep).toHaveBeenCalledWith(preparationId, stepIdToMove, nextParentId);
			}));

			it('should not move step down if it is already the last one', inject(($rootScope, PlaygroundService, PreparationService) => {
				// given
				const nextPosition = 3;

				// when
				PlaygroundService.updateStepOrder(previousPosition, nextPosition);

				// then
				expect(PreparationService.moveStep).not.toHaveBeenCalled();
			}));

			it('should show/hide loading', inject(($rootScope, PlaygroundService) => {
				// when
				PlaygroundService.updateStepOrder(previousPosition, nextPosition);
				expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_START);
				$rootScope.$digest();

				// then
				expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_STOP);
			}));

			it('should refresh recipe', inject(($rootScope, PlaygroundService, RecipeService) => {
				// when
				PlaygroundService.updateStepOrder(previousPosition, nextPosition);
				$rootScope.$digest();

				// then
				expect(RecipeService.refresh).toHaveBeenCalled();
			}));

			it('should update datagrid', inject(($rootScope, PlaygroundService, PreparationService, DatagridService, PreviewService) => {
				// when
				PlaygroundService.updateStepOrder(previousPosition, nextPosition);
				$rootScope.$digest();

				// then
				expect(PreparationService.getContent).toHaveBeenCalledWith(preparationId, 'head', 'HEAD');
				expect(DatagridService.focusedColumn).toBeFalsy();
				expect(DatagridService.updateData).toHaveBeenCalledWith(preparationHeadContent);
				expect(PreviewService.reset).toHaveBeenCalledWith(false);
			}));

			describe('history', () => {
				let undo;

				beforeEach(inject((StepUtilsService) => {
					spyOn(StepUtilsService, 'getLastActiveStep').and.returnValue(lastStep);
				}));

				beforeEach(inject(($rootScope, HistoryService, PlaygroundService) => {
					// given
					expect(HistoryService.addAction).not.toHaveBeenCalled();

					// when
					PlaygroundService.updateStepOrder(previousPosition, nextPosition);
					$rootScope.$digest();

					// then
					undo = HistoryService.addAction.calls.argsFor(0)[0];
				}));

				it('should add undo/redo actions after move step', inject((HistoryService) => {
					// then
					expect(HistoryService.addAction).toHaveBeenCalled();
				}));

				it('should set preparation head to previous head on UNDO', inject((PreparationService) => {
					// given
					expect(PreparationService.setHead).not.toHaveBeenCalled();

					// when
					undo();

					// then
					expect(PreparationService.setHead).toHaveBeenCalledWith(preparationId, lastStepId);
				}));

				it('should refresh recipe on UNDO', inject(($rootScope, RecipeService) => {
					// given
					expect(RecipeService.refresh.calls.count()).toBe(1);

					// when
					undo();
					$rootScope.$digest();

					// then
					expect(RecipeService.refresh.calls.count()).toBe(2);
				}));

				it('should refresh datagrid content on UNDO', inject(($rootScope, PreparationService, DatagridService, PreviewService) => {
					// when
					undo();
					$rootScope.$digest();

					// then
					expect(PreparationService.getContent).toHaveBeenCalledWith(preparationId, 'head', 'HEAD');
					expect(DatagridService.updateData).toHaveBeenCalledWith(preparationHeadContent);
					expect(PreviewService.reset).toHaveBeenCalledWith(false);
				}));
			});
		});

		describe('remove', () => {
			const stepToDeleteId = '98a7565e4231fc2c7';
			const stepToDelete = {
				column: { id: '0001' },
				transformation: { stepId: stepToDeleteId },
				actionParameters: {
					action: 'delete_on_value',
					parameters: { value: 'toto', column_id: '0001' },
				},
			};
			const preparationId = '43ab15436f12e3456';

			it('should remove preparation step', inject(($rootScope, PlaygroundService, PreparationService) => {
				// given
				stateMock.playground.preparation = { id: preparationId };

				// when
				PlaygroundService.removeStep(stepToDelete);

				// then
				expect(PreparationService.removeStep).toHaveBeenCalledWith(preparationId, stepToDeleteId);
			}));

			it('should show/hide loading', inject(($rootScope, PlaygroundService) => {
				// given
				stateMock.playground.preparation = { id: preparationId };

				// when
				PlaygroundService.removeStep(stepToDelete);
				expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_START);
				$rootScope.$digest();

				// then
				expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_STOP);
			}));

			it('should refresh recipe', inject(($rootScope, PlaygroundService, RecipeService) => {
				// given
				stateMock.playground.preparation = { id: preparationId };

				// when
				PlaygroundService.removeStep(stepToDelete);
				$rootScope.$digest();

				// then
				expect(RecipeService.refresh).toHaveBeenCalled();
			}));

			it('should update datagrid', inject(($rootScope, PlaygroundService, PreparationService, DatagridService, PreviewService) => {
				// given
				stateMock.playground.preparation = { id: preparationId };

				// when
				PlaygroundService.removeStep(stepToDelete);
				$rootScope.$digest();

				// then
				expect(PreparationService.getContent).toHaveBeenCalledWith(preparationId, 'head', 'HEAD');
				expect(DatagridService.focusedColumn).toBeFalsy();
				expect(DatagridService.updateData).toHaveBeenCalledWith(preparationHeadContent);
				expect(PreviewService.reset).toHaveBeenCalledWith(false);
			}));

			describe('history', () => {
				let undo;

				beforeEach(inject((StepUtilsService) => {
					spyOn(StepUtilsService, 'getLastActiveStep').and.returnValue(lastStep); // loaded step is the last step
				}));

				beforeEach(inject(($rootScope, HistoryService, PlaygroundService) => {
					// given
					stateMock.playground.preparation = { id: preparationId };
					expect(HistoryService.addAction).not.toHaveBeenCalled();

					// when
					PlaygroundService.removeStep(stepToDelete);
					$rootScope.$digest();

					// then
					undo = HistoryService.addAction.calls.argsFor(0)[0];
				}));

				it('should add undo/redo actions after remove transformation', inject((HistoryService) => {
					// then
					expect(HistoryService.addAction).toHaveBeenCalled();
				}));

				it('should set preparation head to previous head on UNDO', inject((PreparationService) => {
					// given
					expect(PreparationService.setHead).not.toHaveBeenCalled();

					// when
					undo();

					// then
					expect(PreparationService.setHead).toHaveBeenCalledWith(preparationId, lastStepId);
				}));

				it('should refresh recipe on UNDO', inject(($rootScope, RecipeService) => {
					// given
					expect(RecipeService.refresh.calls.count()).toBe(1);

					// when
					undo();
					$rootScope.$digest();

					// then
					expect(RecipeService.refresh.calls.count()).toBe(2);
				}));

				it('should refresh datagrid content on UNDO', inject(($rootScope, PreparationService, DatagridService, PreviewService) => {
					// when
					undo();
					$rootScope.$digest();

					// then
					expect(PreparationService.getContent).toHaveBeenCalledWith(preparationId, 'head', 'HEAD');
					expect(DatagridService.updateData).toHaveBeenCalledWith(preparationHeadContent);
					expect(PreviewService.reset).toHaveBeenCalledWith(false);
				}));
			});
		});

		describe('edit Cell', () => {
			beforeEach(inject(($q, PlaygroundService, FilterAdapterService) => {
				spyOn(PlaygroundService, 'appendStep').and.returnValue($q.when());
				spyOn(FilterAdapterService, 'toTree').and.returnValue({
					filter: {
						eq: {
							field: '0001',
							value: 'john',
						},
					},
				});
			}));

			it('should append step on cell scope', inject((PlaygroundService) => {
				// given
				const rowItem = { tdpId: 58, '0000': 'McDonald', '0001': 'Ronald' };
				const column = { id: '0001', name: 'firstname' };
				const newValue = 'Donald';
				const updateAllCellWithValue = false; // only selected cell

				stateMock.playground.grid.selectedLine = { tdpId: 58 };
				stateMock.playground.grid.selectedColumns = [{ id: '0001', name: 'firstname' }];
				stateMock.playground.filter.applyTransformationOnFilters = false;

				// when
				PlaygroundService.editCell(rowItem, column, newValue, updateAllCellWithValue);

				// then
				const expectedParams = {
					original_value: 'Ronald',
					new_value: 'Donald',
					scope: 'cell',
					row_id: 58,
					column_id: '0001',
					column_name: 'firstname',
				};

				const actions = [{ action: 'replace_cell_value', parameters: expectedParams }];
				expect(PlaygroundService.appendStep).toHaveBeenCalledWith(actions);
			}));

			it('should append step on column scope', inject((PlaygroundService) => {
				// given
				const rowItem = { tdpId: 58, '0000': 'McDonald', '0001': 'Ronald' };
				const column = { id: '0001', name: 'firstname' };
				const newValue = 'Donald';
				const updateAllCellWithValue = true; // all cells in column

				stateMock.playground.grid.selectedLine = { tdpId: 58 };
				stateMock.playground.grid.selectedColumns = [{ id: '0001', name: 'firstname' }];
				stateMock.playground.filter.applyTransformationOnFilters = false;

				// when
				PlaygroundService.editCell(rowItem, column, newValue, updateAllCellWithValue);

				// then
				const expectedParams = {
					cell_value: {
						token: 'Ronald',
						operator: 'equals',
					},
					replace_value: 'Donald',
					scope: 'column',
					row_id: 58,
					column_id: '0001',
					column_name: 'firstname',
				};

				const actions = [{ action: 'replace_on_value', parameters: expectedParams }];
				expect(PlaygroundService.appendStep).toHaveBeenCalledWith(actions);
			}));

			it('should append step with filters', inject((PlaygroundService) => {
				// given
				const rowItem = { tdpId: 58, '0000': 'McDonald', '0001': 'Ronald' };
				const column = { id: '0001', name: 'firstname' };
				const newValue = 'Donald';
				const updateAllCellWithValue = true;

				stateMock.playground.grid.selectedLine = { tdpId: 58 };
				stateMock.playground.grid.selectedColumns = [{ id: '0001', name: 'firstname' }];
				stateMock.playground.filter.applyTransformationOnFilters = true; // apply on filter

				// when
				PlaygroundService.editCell(rowItem, column, newValue, updateAllCellWithValue);

				// then
				const expectedParams = {
					cell_value: {
						token: 'Ronald',
						operator: 'equals',
					},
					replace_value: 'Donald',
					scope: 'column',
					row_id: 58,
					column_id: '0001',
					column_name: 'firstname',
					filter: {
						eq: {
							field: '0001',
							value: 'john',
						},
					},
				};

				const actions = [{ action: 'replace_on_value', parameters: expectedParams }];
				expect(PlaygroundService.appendStep).toHaveBeenCalledWith(actions);
			}));
		});

		describe('params completion and append Step', () => {
			beforeEach(inject(($q, PlaygroundService, FilterAdapterService) => {
				spyOn(PlaygroundService, 'appendStep').and.returnValue($q.when());
				spyOn(FilterAdapterService, 'toTree').and.returnValue({
					filter: {
						eq: {
							field: '0001',
							value: 'john',
						},
					},
				});
			}));

			it('should call appendStep with column', inject((PlaygroundService) => {
				// given
				const transformation = { name: 'tolowercase' };
				const scope = 'column';
				const params = { param: 'value' };
				stateMock.playground.grid.selectedColumns = [{ id: '0001', name: 'firstname' }];
				stateMock.playground.filter.applyTransformationOnFilters = false;

				// when
				PlaygroundService.completeParamsAndAppend(transformation, scope, params);

				// then
				const expectedParams = {
					param: 'value',
					scope: 'column',
					column_id: '0001',
					column_name: 'firstname',
					row_id: undefined,
				};

				const actions = [{ action: 'tolowercase', parameters: expectedParams }];
				expect(PlaygroundService.appendStep).toHaveBeenCalledWith(actions);
			}));

			it('should call appendStep with row', inject((PlaygroundService) => {
				// given
				const transformation = { name: 'tolowercase' };
				const scope = 'line';
				const params = { param: 'value' };
				stateMock.playground.grid.selectedLine = { tdpId: 125 };
				stateMock.playground.filter.applyTransformationOnFilters = false;
				stateMock.playground.grid.selectedColumns = [{ id: '0001', name: 'firstname' }];

				// when
				PlaygroundService.completeParamsAndAppend(transformation, scope, params);

				// then
				const expectedParams = {
					param: 'value',
					scope: 'line',
					row_id: 125,
				};

				const actions = [{ action: 'tolowercase', parameters: expectedParams }];
				expect(PlaygroundService.appendStep).toHaveBeenCalledWith(actions);
			}));

			it('should call appendStep without param', inject((PlaygroundService) => {
				// given
				const transformation = { name: 'tolowercase' };
				const scope = 'column';
				stateMock.playground.grid.selectedColumns = [{ id: '0001', name: 'firstname' }];
				stateMock.playground.filter.applyTransformationOnFilters = false;

				// when
				PlaygroundService.completeParamsAndAppend(transformation, scope);

				// then
				const expectedParams = {
					scope: 'column',
					column_id: '0001',
					column_name: 'firstname',
					row_id: undefined,
				};

				const actions = [{ action: 'tolowercase', parameters: expectedParams }];
				expect(PlaygroundService.appendStep).toHaveBeenCalledWith(actions);
			}));

			it('should call appendStep with filter', inject((PlaygroundService) => {
				// given
				const transformation = { name: 'tolowercase' };
				const scope = 'column';
				stateMock.playground.grid.selectedColumns = [{ id: '0001', name: 'firstname' }];
				stateMock.playground.filter.applyTransformationOnFilters = true;

				// when
				PlaygroundService.completeParamsAndAppend(transformation, scope);

				// then
				const expectedParams = {
					scope: 'column',
					column_id: '0001',
					column_name: 'firstname',
					row_id: undefined,
					filter: {
						eq: {
							field: '0001',
							value: 'john',
						},
					},
				};

				const actions = [{ action: 'tolowercase', parameters: expectedParams }];
				expect(PlaygroundService.appendStep).toHaveBeenCalledWith(actions);
			}));

			it('should create an append closure', inject((PlaygroundService) => {
				// given
				const transformation = { name: 'tolowercase' };
				const scope = 'column';
				const params = { param: 'value' };
				stateMock.playground.grid.selectedColumns = [{ id: '0001', name: 'firstname' }];
				stateMock.playground.grid.selectedLine = { tdpId: 125 };
				stateMock.playground.filter.applyTransformationOnFilters = false;

				// when
				const closure = PlaygroundService.createAppendStepClosure(transformation, scope);
				closure(params);

				// then
				const expectedParams = {
					param: 'value',
					scope: 'column',
					column_id: '0001',
					column_name: 'firstname',
					row_id: 125,
				};

				const actions = [{ action: 'tolowercase', parameters: expectedParams }];
				expect(PlaygroundService.appendStep).toHaveBeenCalledWith(actions);
			}));

			it('should create an append closure with multi columns', inject((PlaygroundService) => {
				// given
				const transformation = { name: 'tolowercase' };
				const scope = 'column';
				const params = { param: 'value' };
				stateMock.playground.grid.selectedColumns = [{ id: '0001', name: 'firstname' }, {
					id: '0002',
					name: 'lastname'
				}];
				stateMock.playground.grid.selectedLine = { tdpId: 125 };
				stateMock.playground.filter.applyTransformationOnFilters = false;

				// when
				const closure = PlaygroundService.createAppendStepClosure(transformation, scope);
				closure(params);

				// then
				const expectedParams1 = {
					param: 'value',
					scope: 'column',
					column_id: '0001',
					column_name: 'firstname',
					row_id: 125,
				};

				const expectedParams2 = {
					param: 'value',
					scope: 'column',
					column_id: '0002',
					column_name: 'lastname',
					row_id: 125,
				};

				const actions = [{ action: 'tolowercase', parameters: expectedParams1 }, {
					action: 'tolowercase',
					parameters: expectedParams2
				}];
				expect(PlaygroundService.appendStep).toHaveBeenCalledWith(actions);
			}));
		});

		describe('toggle step', () => {
			beforeEach(inject((PlaygroundService) => {
				spyOn(PlaygroundService, 'loadStep').and.returnValue();
			}));

			it('should load current step content if the step is first inactive', inject((PlaygroundService) => {
				// given
				const step = { inactive: true, column: { id: '0001' } };

				// when
				PlaygroundService.toggleStep(step);

				// then
				expect(PlaygroundService.loadStep).toHaveBeenCalledWith(step);
			}));

			it('should load previous step content if the step is first active', inject((PlaygroundService) => {
				// given
				const step = { inactive: false, column: { id: '0001' } };

				// when
				PlaygroundService.toggleStep(step);

				// then
				expect(PlaygroundService.loadStep).toHaveBeenCalledWith(previousStep);
			}));
		});
	});

	describe('dataset parameters', () => {
		let assertNewPlaygroundIsInitWith;
		let assertPreparationStepIsLoadedWith;

		beforeEach(inject((StateService, TransformationCacheService, HistoryService, PreviewService, DatagridService) => {
			assertNewPlaygroundIsInitWith = (dataset) => {
				expect(StateService.resetPlayground).toHaveBeenCalled();
				expect(StateService.setCurrentDataset).toHaveBeenCalledWith(datasetColumns.metadata);
				expect(StateService.setCurrentData).toHaveBeenCalledWith(datasetColumns);
				expect(StateService.setCurrentSampleType).toHaveBeenCalledWith('HEAD');
				expect(TransformationCacheService.invalidateCache).toHaveBeenCalled();
				expect(HistoryService.clear).toHaveBeenCalled();
				expect(PreviewService.reset).toHaveBeenCalledWith(false);
			};

			assertPreparationStepIsLoadedWith = (dataset, data, step) => {
				expect(DatagridService.updateData).toHaveBeenCalledWith(data);
				expect(StateService.disableRecipeStepsAfter).toHaveBeenCalledWith(step);
				expect(PreviewService.reset).toHaveBeenCalled();
			};
		}));

		it('should perform parameters update on current dataset', inject((PlaygroundService, DatasetService) => {
			// given
			const parameters = { separator: ';', encoding: 'UTF-8' };
			const dataset = { id: '32549c18046cd54b265' };
			stateMock.playground.dataset = dataset;
			stateMock.playground.preparation = null;

			expect(DatasetService.updateParameters).not.toHaveBeenCalled();

			// when
			PlaygroundService.changeDatasetParameters(parameters);

			// then
			expect(DatasetService.updateParameters).toHaveBeenCalledWith(dataset, parameters);
		}));

		it('should reinit playground with dataset after parameters update', inject(($rootScope, PlaygroundService) => {
			// given
			const parameters = { separator: ';', encoding: 'UTF-8' };
			const dataset = { id: '32549c18046cd54b265' };
			stateMock.playground.dataset = dataset;
			stateMock.playground.preparation = null;

			// when
			PlaygroundService.changeDatasetParameters(parameters);
			$rootScope.$digest();

			// then
			assertNewPlaygroundIsInitWith(dataset);
		}));

		it('should reinit playground with preparation at active step after parameters update', inject(($rootScope, $q, PlaygroundService, StepUtilsService, PreparationService) => {
			// given
			const parameters = { separator: ';', encoding: 'UTF-8' };
			const dataset = { id: '32549c18046cd54b265' };

			// given : state mock
			stateMock.playground.dataset = dataset;
			stateMock.playground.preparation = { id: '35d8cf964aa81b58' };

			// given : preparation content mock
			const data = { metadata: { columns: [] }, records: [] };
			spyOn(PreparationService, 'getContent').and.returnValue($q.when(data));

			// given : step mocks
			const step = { transformation: { stepId: '5874de8432c543' } };
			spyOn(StepUtilsService, 'getActiveThresholdStepIndex').and.returnValue(5);
			spyOn(StepUtilsService, 'getStep').and.callFake((recipeState, index) => {
				if (index === 5) {
					return step;
				}

				return null;
			});

			// when
			PlaygroundService.changeDatasetParameters(parameters);
			$rootScope.$digest();

			// then
			assertPreparationStepIsLoadedWith(dataset, data, step);
		}));
	});

	describe('on step append', () => {
		beforeEach(inject(($q, PreparationService, StepUtilsService) => {
			spyOn(PreparationService, 'getContent').and.returnValue($q.when({ columns: [{}] }));
			spyOn(PreparationService, 'appendStep').and.callFake(() => {
				stateMock.playground.recipe.current.steps.push({});
				return $q.when();
			});
			spyOn(StepUtilsService, 'getLastStep').and.returnValue({
				transformation: { stepId: 'a151e543456413ef51' },
			});
			spyOn(StepUtilsService, 'getPreviousStep').and.returnValue({
				transformation: { stepId: '84f654a8e64fc5' },
			});
		}));

		describe('route change', () => {
			it('should change route to preparation route on first step', inject(($rootScope, $state, PlaygroundService) => {
				// given
				expect($state.go).not.toHaveBeenCalled();
				stateMock.playground.dataset = { id: '123456' };

				const parameters = { param1: 'param1Value', param2: 4 };
				const actions = [{ action: 'uppercase', parameters: parameters }];

				// when
				PlaygroundService.appendStep(actions);
				stateMock.playground.preparation = preparations[0];
				$rootScope.$digest();

				// then
				expect($state.go).toHaveBeenCalledWith('playground.preparation', { prepid: preparations[0].id });
			}));
		});

		describe('recipe display', () => {
			it('should display onboarding on third step if the tour has not been completed yet', inject(($rootScope, $timeout, PlaygroundService, StateService, OnboardingService) => {
				// given
				stateMock.playground.dataset = { id: '123456' };
				spyOn(OnboardingService, 'startTour').and.returnValue();
				spyOn(OnboardingService, 'shouldStartTour').and.returnValue(true); // not completed

				const parameters = { param1: 'param1Value', param2: 4 };
				const actions = [{ action: 'uppercase', parameters: parameters }];

				// given : first action call
				PlaygroundService.appendStep(actions);
				stateMock.playground.preparation = preparations[0];
				$rootScope.$digest();
				$timeout.flush(300);

				// given : second action call
				PlaygroundService.appendStep(actions);
				$timeout.flush(300);
				$rootScope.$digest();

				expect(OnboardingService.startTour).not.toHaveBeenCalled();

				StateService.showRecipe.calls.reset();
				// when
				PlaygroundService.appendStep(actions);
				$rootScope.$digest();
				$timeout.flush(300);

				// then
				expect(StateService.showRecipe).toHaveBeenCalled();
				expect(OnboardingService.startTour).toHaveBeenCalled();
			}));

			it('should NOT display onboarding on third step if the tour has already been completed', inject(($rootScope, $timeout, PlaygroundService, StateService, OnboardingService) => {
				// given
				stateMock.playground.dataset = { id: '123456' };
				spyOn(OnboardingService, 'startTour').and.returnValue();
				spyOn(OnboardingService, 'shouldStartTour').and.returnValue(false); // already completed

				const parameters = { param1: 'param1Value', param2: 4 };
				const actions = [{ action: 'uppercase', parameters: parameters }];

				// given : first action call
				PlaygroundService.appendStep(actions);
				stateMock.playground.preparation = preparations[0];
				$rootScope.$digest();
				$timeout.flush(300);


				// given : second action call
				PlaygroundService.appendStep(actions);
				$timeout.flush(300);
				$rootScope.$digest();

				expect(OnboardingService.startTour).not.toHaveBeenCalled();

				// when
				PlaygroundService.appendStep(actions);
				$rootScope.$digest();
				$timeout.flush(300);

				// then
				expect(OnboardingService.startTour).not.toHaveBeenCalled();
			}));

			it('should show recipe only on the first step', inject(($rootScope, $timeout, PlaygroundService, StateService, OnboardingService) => {
				// given
				stateMock.playground.dataset = { id: '123456' };

				const parameters = { param1: 'param1Value', param2: 4 };
				const actions = [{ action: 'uppercase', parameters: parameters }];

				// given : first action call
				PlaygroundService.appendStep(actions);
				stateMock.playground.preparation = preparations[0];
				$rootScope.$digest();
				$timeout.flush(300);

				expect(StateService.showRecipe).toHaveBeenCalled();
				StateService.showRecipe.calls.reset();

				// given : second action call
				PlaygroundService.appendStep(actions);
				$timeout.flush(300);
				$rootScope.$digest();

				expect(StateService.showRecipe).not.toHaveBeenCalled();
			}));
		});
	});

	describe('update preparation details', () => {
		it('should get details for first step', inject(($rootScope, PlaygroundService, RecipeService, PreparationService) => {
			// given
			stateMock.playground.preparation = { id: '79db821355a65cd96' };

			// when
			PlaygroundService.updatePreparationDetails();
			$rootScope.$digest();

			// then
			expect(PreparationService.getDetails).toHaveBeenCalledWith('79db821355a65cd96');
			expect(RecipeService.refresh).toHaveBeenCalled();
		}));
	});

	describe('copy steps', () => {
		beforeEach(inject(($rootScope, $q, PreparationService) => {
			spyOn(PreparationService, 'copySteps').and.returnValue($q.when());
			spyOn(PreparationService, 'getContent').and.returnValue($q.when());
			spyOn($rootScope, '$emit').and.returnValue($q.when());
		}));

		it('should copy steps', inject(($rootScope, PlaygroundService, PreparationService) => {
			// given
			stateMock.playground.preparation = { id: '79db821355a65cd96' };
			expect(PreparationService.copySteps).not.toHaveBeenCalled();

			// when
			PlaygroundService.copySteps('13cf24597f9b6ba542');
			$rootScope.$digest();

			// then
			expect(PreparationService.copySteps).toHaveBeenCalledWith('79db821355a65cd96', '13cf24597f9b6ba542');
		}));

		it('should update recipe', inject(($rootScope, PlaygroundService, RecipeService) => {
			// given
			stateMock.playground.preparation = { id: '79db821355a65cd96' };
			expect(RecipeService.refresh).not.toHaveBeenCalled();

			// when
			PlaygroundService.copySteps('13cf24597f9b6ba542');
			$rootScope.$digest();

			// then
			expect(RecipeService.refresh).toHaveBeenCalled();
		}));

		it('should update datagrid', inject(($rootScope, PlaygroundService, PreparationService) => {
			// given
			stateMock.playground.preparation = { id: '79db821355a65cd96' };
			expect(PreparationService.getContent).not.toHaveBeenCalled();

			// when
			PlaygroundService.copySteps('13cf24597f9b6ba542');
			$rootScope.$digest();

			// then
			expect(PreparationService.getContent).toHaveBeenCalledWith('79db821355a65cd96', 'head', 'HEAD');
		}));

		it('should manage loader', inject(($rootScope, PlaygroundService) => {
			// given
			stateMock.playground.preparation = { id: '79db821355a65cd96' };
			expect($rootScope.$emit).not.toHaveBeenCalled();

			// when
			PlaygroundService.copySteps('13cf24597f9b6ba542');
			expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_START);
			$rootScope.$digest();

			// then
			expect($rootScope.$emit).toHaveBeenCalledWith(EVENT_LOADING_STOP);
		}));
	});

	describe('preparation', () => {
		beforeEach(inject(($q, $stateParams, DatasetService, PlaygroundService, PreparationService, StateService) => {
			spyOn(PreparationService, 'getContent').and.returnValue($q.when(preparationMetadata));
			spyOn(DatasetService, 'getMetadata').and.returnValue($q.when(datasetMetadata));
			spyOn(PlaygroundService, 'startLoader').and.returnValue();
			spyOn(StateService, 'setIsLoadingPlayground').and.returnValue();
			spyOn(StateService, 'setPreviousRoute').and.returnValue();
			spyOn(StateService, 'setIsFetchingStats').and.returnValue();

			$stateParams.reload = true;
			stateMock.playground.preparation = null;
			stateMock.playground.data = {
				metadata: {
					id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
					name: 'customers_jso_light',
					columns: [{
						statistics: {
							frequencyTable: [{ // stats already computed
								value: 'toto',
								frequency: 10,
							}],
						},
					}],
				},
			};
		}));

		it('should start loading', inject((StateService, PlaygroundService) => {
			// when
			PlaygroundService.initPreparation();

			// then
			expect(StateService.setIsLoadingPlayground).toHaveBeenCalled();
		}));

		it('should set previous route to preparations home', inject((PlaygroundService, StateService) => {
			// when
			PlaygroundService.initPreparation();

			// then
			expect(StateService.setPreviousRoute).toHaveBeenCalledWith(
				HOME_PREPARATIONS_ROUTE,
				{ folderId: 'abcd' }
			);
		}));

		it('should call get dataset details', inject(($q, $rootScope, PlaygroundService, PreparationService) => {
			// given
			const prepId = 'prep1';

			// when
			PlaygroundService.initPreparation(prepId);
			$rootScope.$apply();

			// then
			expect(PreparationService.getDetails).toHaveBeenCalledWith(prepId);
		}));

		it('should get dataset metadata', inject(($q, $rootScope, PlaygroundService, DatasetService) => {
			// given
			spyOn(PlaygroundService, 'loadPreparation').and.returnValue($q.when());

			// when
			PlaygroundService.initPreparation();
			$rootScope.$apply();

			// then
			expect(DatasetService.getMetadata).toHaveBeenCalledWith(preparations[0].dataSetId);
		}));

		it('should load playground', inject(($q, $rootScope, PlaygroundService) => {
			// given
			spyOn(PlaygroundService, 'loadPreparation').and.returnValue($q.when());

			// when
			PlaygroundService.initPreparation();
			$rootScope.$apply();

			// then

			expect(PlaygroundService.loadPreparation).toHaveBeenCalled();
		}));

		it('should not reload preparation', inject(($q, $rootScope, $stateParams, PlaygroundService) => {
			// given
			$stateParams.prepid = preparations[0].id;
			$stateParams.reload = false;
			stateMock.playground.preparation = preparations[0];
			spyOn(PlaygroundService, 'loadPreparation').and.returnValue($q.when());

			// when
			PlaygroundService.initPreparation();
			$rootScope.$apply();

			// then
			expect(PlaygroundService.loadPreparation).not.toHaveBeenCalled();
		}));

		it('should fetch statistics when they are not computed yet',
			inject(($q, $rootScope, PreparationService, PlaygroundService, StateService) => {
				// given
				spyOn(PlaygroundService, 'updateStatistics').and.returnValue($q.when());
				stateMock.playground.preparation = preparations[0];

				// when
				PlaygroundService.loadPreparation(preparations[0]);
				expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
				expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();

				stateMock.playground.data = {
					metadata: {
						id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
						name: 'customers_jso_light',
						columns: [{
							statistics: {
								frequencyTable: [],       // stats not computed
							},
						}],
					},
				};
				$rootScope.$apply();

				// then
				expect(StateService.setIsFetchingStats).toHaveBeenCalledWith(true);
				expect(PlaygroundService.updateStatistics).toHaveBeenCalled();
				expect(StateService.setIsFetchingStats).toHaveBeenCalledWith(false);
			})
		);

		it('should retry statistics fetch when the previous fetch has been rejected (stats not computed yet) with a delay of 1500ms',
			inject(($q, $rootScope, $timeout, PlaygroundService, PreparationService, StateService) => {
				// given
				let retry = 0;
				spyOn(PlaygroundService, 'updateStatistics').and.callFake(() => {
					if (retry === 0) {
						retry++;
						return $q.reject();
					}
					else {
						return $q.when();
					}
				});
				stateMock.playground.preparation = preparations[0];

				// when
				PlaygroundService.loadPreparation(preparations[0]);
				expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
				expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();

				stateMock.playground.data = {
					metadata: {
						id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
						name: 'customers_jso_light',
						columns: [{
							statistics: {
								frequencyTable: [],       // stats not computed
							},
						}],
					},
				};
				$rootScope.$apply();

				expect(StateService.setIsFetchingStats.calls.count()).toBe(1);
				expect(StateService.setIsFetchingStats).toHaveBeenCalledWith(true);
				expect(PlaygroundService.updateStatistics.calls.count()).toBe(1); // first call: rejected
				$timeout.flush(1500);

				// then
				expect(PlaygroundService.updateStatistics.calls.count()).toBe(2);
				expect(StateService.setIsFetchingStats).toHaveBeenCalledWith(false);
			})
		);

		it('should NOT fetch statistics when they are already computed',
			inject(($q, $rootScope, PlaygroundService, PreparationService, StateService) => {
				// given
				spyOn(PlaygroundService, 'updateStatistics').and.returnValue($q.when());
				stateMock.playground.preparation = preparations[0];

				// when
				expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
				PlaygroundService.loadPreparation(preparations[0]);
				expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
				expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();

				stateMock.playground.data = {
					metadata: {
						id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
						name: 'customers_jso_light',
						columns: [{
							statistics: {
								frequencyTable: [{ // stats already computed
									value: 'toto',
									frequency: 10,
								}],
							},
						}],
					},
				};
				$rootScope.$apply();

				// then
				expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
				expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();
			})
		);
	});

	describe('dataset', () => {
		beforeEach(inject(($q, DatasetService, MessageService, PlaygroundService, StateService) => {
			spyOn(PlaygroundService, 'startLoader').and.returnValue();
			spyOn(StateService, 'setIsLoadingPlayground').and.returnValue();
			spyOn(StateService, 'setPreviousRoute').and.returnValue();
			spyOn(StateService, 'setIsFetchingStats').and.returnValue();
			spyOn(MessageService, 'error').and.returnValue();
			spyOn(DatasetService, 'getMetadata').and.returnValue($q.when(datasetMetadata));

			// given
			stateMock.playground.data = {
				metadata: {
					id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
					name: 'customers_jso_light',
					columns: [{
						statistics: {
							frequencyTable: [{ // stats already computed
								value: 'toto',
								frequency: 10,
							}],
						},
					}],
				},
			};
		}));

		it('should startloading', inject((StateService, PlaygroundService) => {
			// when
			PlaygroundService.initDataset();

			// then
			expect(StateService.setIsLoadingPlayground).toHaveBeenCalled();
		}));

		it('should init previous route to dataset home', inject((PlaygroundService, StateService) => {
			// when
			PlaygroundService.initDataset();

			// then
			expect(StateService.setPreviousRoute).toHaveBeenCalledWith(HOME_DATASETS_ROUTE);
		}));

		it('should init playground', inject(($q, $rootScope, PlaygroundService) => {
			// given
			const datasetId = 'dataset1';
			spyOn(PlaygroundService, 'loadDataset').and.returnValue($q.when());

			// when
			PlaygroundService.initDataset(datasetId);
			$rootScope.$apply();

			// then
			expect(PlaygroundService.loadDataset).toHaveBeenCalledWith(datasetId);
		}));

		it('should fetch statistics when they are not computed yet',
			inject(($q, $rootScope, PlaygroundService, StateService) => {
				// given
				stateMock.playground.dataset = datasets[0];

				spyOn(PlaygroundService, 'updateStatistics').and.returnValue($q.when());

				// when
				PlaygroundService.loadDataset(datasets[0]);
				expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
				expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();

				stateMock.playground.data = {
					metadata: {
						id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
						name: 'customers_jso_light',
						columns: [{
							statistics: {
								frequencyTable: [],       // stats not computed
							},
						}],
					},
				};
				$rootScope.$apply();

				// then
				expect(StateService.setIsFetchingStats).toHaveBeenCalledWith(true);
				expect(PlaygroundService.updateStatistics).toHaveBeenCalled();
				expect(StateService.setIsFetchingStats).toHaveBeenCalledWith(false);
			})
		);

		it('should retry statistics fetch when the previous fetch has been rejected (stats not computed yet) with a delay of 1500ms',
			inject(($q, $rootScope, $timeout, PlaygroundService, StateService) => {
				// given
				let retry = 0;
				stateMock.playground.dataset = datasets[0];

				spyOn(PlaygroundService, 'updateStatistics').and.callFake(() => {
					if (retry === 0) {
						retry++;
						return $q.reject();
					}
					else {
						return $q.when();
					}
				});

				// when
				PlaygroundService.loadDataset(datasets[0]);
				expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
				expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();

				stateMock.playground.data = {
					metadata: {
						id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
						name: 'customers_jso_light',
						columns: [{
							statistics: {
								frequencyTable: [],       // stats not computed
							},
						}],
					},
				};
				$rootScope.$apply();

				expect(StateService.setIsFetchingStats.calls.count()).toBe(1);
				expect(StateService.setIsFetchingStats).toHaveBeenCalledWith(true);
				expect(PlaygroundService.updateStatistics.calls.count()).toBe(1); // first call: rejected
				$timeout.flush(1500);

				// then
				expect(PlaygroundService.updateStatistics.calls.count()).toBe(2);
				expect(StateService.setIsFetchingStats).toHaveBeenCalledWith(false);
			})
		);

		it('should NOT fetch statistics when they are already computed',
			inject(($q, $rootScope, PlaygroundService, StateService) => {
				// given
				stateMock.playground.dataset = datasets[0];

				spyOn(PlaygroundService, 'updateStatistics').and.returnValue($q.when());

				// when
				expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
				PlaygroundService.loadDataset(datasets[0]);
				expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
				expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();

				stateMock.playground.data = {
					metadata: {
						id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
						name: 'customers_jso_light',
						columns: [{
							statistics: {
								frequencyTable: [{ // stats already computed
									value: 'toto',
									frequency: 10,
								}],
							},
						}],
					},
				};
				$rootScope.$apply();

				// then
				expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
				expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();
			})
		);
	});
});
