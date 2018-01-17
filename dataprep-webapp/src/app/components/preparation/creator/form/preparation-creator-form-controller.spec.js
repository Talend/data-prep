/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../../../mocks/Settings.mock';

describe('Preparation Creator Form Controller', () => {
	let createController;
	let scope;
	let stateMock;
	let uploadDefer;
	let filters;

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {
			PREPARATION: 'Preparation',
		});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(angular.mock.module('data-prep.preparation-creator', ($provide) => {
		stateMock = {
			inventory: {
				folder: {
					content: {
						preparations: [
							{ id: 'abc-def', name: 'my dataset Preparation' },
							{ id: 'a95-def', name: 'my dataset Preparation (1)' },
						],
					},
				},
			},
			progress: {
				types: {
					progression: 'PROGRESSION',
					infinite: 'INFINITE',
				},
				states: {
					inProgress: 'IN_PROGRESS',
					future: 'FUTURE',
					complete: 'COMPLETE',
				},
				schemas: {
					dataset: {
						title: 'ADD_NEW_DATASET',
						steps: [
							{
								type: 'PROGRESSION',
								state: 'IN_PROGRESS',
								label: 'UPLOADING_FILE',
							},
							{
								type: 'INFINITE',
								state: 'FUTURE',
								label: 'PROFILING_DATA',
							},
						],
					},
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new(true);

		createController = () => {
			return $componentController('preparationCreatorForm', { $scope: scope });
		};
	}));

	beforeEach(inject(($q, DatasetService, RestURLs) => {
		RestURLs.register({ serverUrl: '' }, settings.uris);

		uploadDefer = $q.defer();
		uploadDefer.promise.progress = (callback) => {
			uploadDefer.progressCb = callback;
			return uploadDefer.promise;
		};

		filters = DatasetService.filters;
	}));

	describe('init', () => {
		it('should load recent datasets by default', inject(($q, DatasetService) => {
			//given
			spyOn(DatasetService, 'getFilteredDatasets').and.returnValue($q.when());
			const ctrl = createController();

			expect(DatasetService.getFilteredDatasets).not.toHaveBeenCalled();
			expect(ctrl.selectedFilter).toBeFalsy();

			//when
			ctrl.$onInit();

			//then
			expect(ctrl.selectedFilter).toBe(filters[0]);
			expect(DatasetService.getFilteredDatasets).not.toHaveBeenCalledWith(filters[0]);
		}));
	});

	describe('loadDatasets', () => {
		const filteredDs = [
			{ id: 'def12535-212', name: 'datasetName1' },
			{ id: 'abc15455-212', name: 'datasetName1' },
		];

		it('should get filtered datasets', inject(($q, DatasetService) => {
			//given
			spyOn(DatasetService, 'getFilteredDatasets').and.returnValue($q.when(filteredDs));
			const ctrl = createController();
			ctrl.enteredFilterText = 'toto';

			//when
			ctrl.loadDatasets(filters[2]);
			scope.$digest();

			//then
			expect(DatasetService.getFilteredDatasets).toHaveBeenCalledWith(filters[2], 'toto');
			expect(ctrl.filteredDatasets).toBe(filteredDs);
		}));

		it('should manage datasets fetch flag', inject(($q, DatasetService) => {
			//given
			spyOn(DatasetService, 'getFilteredDatasets').and.returnValue($q.when(filteredDs));
			const ctrl = createController();
			ctrl.enteredFilterText = 'toto';
			expect(ctrl.isFetchingDatasets).toBe(false);

			//when
			ctrl.loadDatasets(filters[2]);
			expect(ctrl.isFetchingDatasets).toBe(true);
			scope.$digest();

			//then
			expect(ctrl.isFetchingDatasets).toBe(false);
		}));

		it('should NOT change datasets and manage flags when get fails', inject(($q, DatasetService) => {
			//given
			spyOn(DatasetService, 'getFilteredDatasets').and.returnValue($q.reject());
			const ctrl = createController();
			expect(ctrl.filteredDatasets).toEqual([]);
			expect(ctrl.isFetchingDatasets).toBe(false);

			//when
			ctrl.loadDatasets(filters[2]);
			expect(ctrl.isFetchingDatasets).toBe(true);
			scope.$digest();

			//then
			expect(ctrl.isFetchingDatasets).toBe(false);
			expect(ctrl.filteredDatasets).toEqual([]);
		}));

		it('should call load datasets', inject(($q, DatasetService) => {
			//given
			spyOn(DatasetService, 'getFilteredDatasets').and.returnValue($q.when(filteredDs));
			const ctrl = createController();
			ctrl.selectedFilter = filters[2];
			ctrl.enteredFilterText = 'toto';

			//when
			ctrl.applyNameFilter();
			scope.$digest();

			//then
			expect(DatasetService.getFilteredDatasets).toHaveBeenCalledWith(filters[2], 'toto');
			expect(ctrl.filteredDatasets).toBe(filteredDs);
		}));
	});

	describe('import with an unavailable dataset name', () => {
		beforeEach(inject(($q, DatasetService) => {
			spyOn(DatasetService, 'createDatasetInfo').and.returnValue();
			spyOn(DatasetService, 'create').and.returnValue(uploadDefer.promise);
		}));

		describe('dataset name is NOT available', () => {
			beforeEach(inject(($q, DatasetService) => {
				spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.reject());
			}));

			it('should prompt for a new name', inject(($q, ImportService) => {
				//given
				const file = { name: 'my Dataset name (1).csv' };
				const ctrl = createController();
				ctrl.datasetFile = [file];

				//when
				ctrl.import();
				scope.$digest();

				//then
				expect(ImportService.datasetNameModal).toBeTruthy();
			}));
		});

		describe('dataset name is available', () => {
			beforeEach(inject(($q, ImportService) => {
				spyOn(ImportService, 'import').and.returnValue($q.when(true));
			}));

			it('should call import dataset function', inject(($q, ImportService) => {
				//given
				const file = { name: 'my Dataset name (1).csv' };
				const ctrl = createController();
				ctrl.datasetFile = [file];

				//when
				ctrl.import();
				scope.$digest();

				//then
				expect(ImportService.import).toHaveBeenCalled();
			}));
		});
	});

	describe('import', () => {
		let dataset;
		let ctrl;
		beforeEach(inject(($q, $state, DatasetService) => {
			dataset = {
				name: 'name',
				progress: 0,
				file: {},
				error: false,
				id: 'abc-deff',
				type: 'file',
			};

			spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.when());
			spyOn($state, 'go').and.returnValue();
			spyOn(DatasetService, 'createDatasetInfo').and.returnValue(dataset);

			ctrl = createController();
			ctrl.addPreparationForm = { $commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue() };
			ctrl.datasetFile = [{ name: 'my Dataset name (1).csv' }];
			scope.$digest();
		}));

		describe('succeeds', () => {
			beforeEach(inject(($q, $state, DatasetService, PreparationService) => {
				spyOn(DatasetService, 'create').and.returnValue(uploadDefer.promise);
				spyOn(PreparationService, 'create').and.returnValue($q.when(true));
				spyOn(ctrl, 'createPreparation').and.returnValue();
			}));

			it('should launch preparation creation process once the dataset creation finished', inject(($q, DatasetService) => {
				//given
				spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when(dataset));
				expect(ctrl.importDisabled).toBe(false);

				//when
				ctrl.import();
				expect(ctrl.importDisabled).toBe(true);
				uploadDefer.resolve({ data: dataset.id });
				scope.$digest();

				//then
				expect(ctrl.importDisabled).toBe(false);
				expect(DatasetService.getDatasetById).toHaveBeenCalledWith(dataset.id);
				expect(ctrl.baseDataset).toBe(dataset);
				expect(ctrl.createPreparation).toHaveBeenCalled();
			}));

			it('should create a preparation with a generated unique name', inject(($q, DatasetService) => {
				//given
				ctrl.datasetFile = [{ name: 'my dataset.csv' }];
				ctrl.userHasTypedName = false;
				ctrl.enteredName = '';
				spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when({
					id: '123',
					name: 'my dataset'
				}));

				//when
				ctrl.import();
				uploadDefer.resolve({ data: dataset.id });
				scope.$digest();

				//then
				expect(ctrl.enteredName).toBe('my dataset Preparation (2)');
			}));

			it('should create a preparation with with the entered name by the user', inject(($q, DatasetService) => {
				//given
				ctrl.datasetFile = [{ name: 'my dataset.csv' }];
				ctrl.userHasTypedName = true;
				ctrl.enteredName = 'prep name by user';
				spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when({
					id: '123',
					name: 'my dataset'
				}));

				//when
				ctrl.import();
				uploadDefer.resolve({ data: dataset.id });
				scope.$digest();

				//then
				expect(ctrl.enteredName).toBe('prep name by user');
			}));
		});

		describe('progressing', () => {
			it('should update progress bar', inject((DatasetService) => {
				//given
				spyOn(DatasetService, 'create').and.returnValue(uploadDefer.promise);
				expect(dataset.progress).toBeFalsy();
				const event = {
					loaded: 100,
					total: 200,
				};

				//when
				ctrl.import();
				scope.$digest();
				uploadDefer.progressCb(event);
				scope.$digest();

				//then
				expect(DatasetService.create).toHaveBeenCalled();
				expect(dataset.progress).toBe(50);
				expect(dataset.error).toBeFalsy();
			}));
		});

		describe('fails', () => {
			it('should trigger error', inject((DatasetService) => {
				//given
				spyOn(DatasetService, 'create').and.returnValue(uploadDefer.promise);

				//when
				ctrl.import();
				uploadDefer.reject();
				scope.$digest();

				//then
				expect(dataset.error).toBe(true);
				expect(ctrl.baseDataset).toBe(null);
				expect(ctrl.importDisabled).toBe(false);
			}));
		});
	});

	describe('createPreparation', () => {
		let newPreparation = {
			id: 'def-12558',
		};
		beforeEach(inject(($q, $state, PreparationService, UploadWorkflowService) => {
			spyOn(PreparationService, 'create').and.returnValue($q.when(newPreparation.id));
			spyOn(UploadWorkflowService, 'openDraft').and.returnValue();
			spyOn($state, 'go').and.returnValue();
		}));

		it('should call create preparation service', inject(($state, PreparationService) => {
			//given
			const ctrl = createController();
			ctrl.baseDataset = { id: 'abc-54' };
			ctrl.enteredName = 'prep name';
			stateMock.inventory.folder.metadata = { path: '/amaa' };
			ctrl.addPreparationForm = { $commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue() };

			//when
			ctrl.createPreparation();
			scope.$digest();

			//then
			expect(PreparationService.create).toHaveBeenCalledWith(ctrl.baseDataset.id, ctrl.enteredName, stateMock.inventory.folder.metadata.id);
			expect($state.go).toHaveBeenCalledWith('playground.preparation', { prepid: newPreparation.id });
		}));

		it('should call UploadWorkflowService openDraft for multisheet dataset',
			inject((StateService, UploadWorkflowService) => {
				//given
				stateMock.inventory.folder.metadata = { id: '15b68a46' };
				const ctrl = createController();
				ctrl.baseDataset = { draft: true, id: 'abc-54', name: 'test' };
				ctrl.enteredName = 'test';

				spyOn(StateService, 'togglePreparationCreator').and.returnValue();
				expect(UploadWorkflowService.openDraft).not.toHaveBeenCalled();

				//when
				ctrl.createPreparation();
				scope.$digest();

				//then
				expect(StateService.togglePreparationCreator).toHaveBeenCalled();
				expect(UploadWorkflowService.openDraft).toHaveBeenCalledWith(ctrl.baseDataset, true, 'test');
			}));
	});

	describe('checkExistingPrepName', () => {
		it('should update userHasTypedName flag', () => {
			//given
			const ctrl = createController();
			expect(ctrl.userHasTypedName).toBe(false);

			//when
			ctrl.checkExistingPrepName('user');

			//then
			expect(ctrl.userHasTypedName).toBe(true);
		});

		it('should not update userHasTypedName flag', () => {
			//given
			const ctrl = createController();
			expect(ctrl.userHasTypedName).toBe(false);

			//when
			ctrl.checkExistingPrepName();

			//then
			expect(ctrl.userHasTypedName).toBe(false);
		});

		it('should set alreadyExistingName flag to true', () => {
			//given
			const ctrl = createController();
			ctrl.enteredName = 'my dataset Preparation';

			//when
			ctrl.checkExistingPrepName();

			//then
			expect(ctrl.userHasTypedName).toBe(false);
		});

		it('should set alreadyExistingName flag to false', () => {
			//given
			const ctrl = createController();
			ctrl.enteredName = 'unique name';

			//when
			ctrl.checkExistingPrepName();

			//then
			expect(ctrl.userHasTypedName).toBe(false);
		});
	});

	describe('selectBaseDataset', () => {
		let dataset = {
			id: 'abc-5424',
			name: 'my dataset',
		};

		it('should update selection flag', () => {
			//given
			const ctrl = createController();
			expect(ctrl.baseDataset).not.toBe(dataset);

			//when
			ctrl.selectBaseDataset(dataset);

			//then
			expect(ctrl.baseDataset).toBe(dataset);
		});

		it('should generate a unique preparation Name given the selected dataset', () => {
			//given
			const ctrl = createController();
			ctrl.userHasTypedName = false;
			ctrl.enteredName = '';

			//when
			ctrl.selectBaseDataset(dataset);

			//then
			expect(ctrl.enteredName).toBe('my dataset Preparation (2)');
		});

		it('should NOT change the entered preparation Name by the user', () => {
			//given
			const ctrl = createController();
			ctrl.userHasTypedName = true;
			ctrl.enteredName = 'prep name';

			//when
			ctrl.selectBaseDataset(dataset);

			//then
			expect(ctrl.enteredName).toBe('prep name');
		});
	});

	describe('anyMissingEntries', () => {
		it('should disable form submission when entered name is empty', () => {
			//given
			const ctrl = createController();
			ctrl.enteredName = '';
			ctrl.baseDataset = {};
			ctrl.alreadyExistingName = false;

			//when
			const disableForm = ctrl.anyMissingEntries();

			//then
			expect(disableForm).toBeTruthy();
		});

		it('should disable form submission when there is no selected dataset', () => {
			//given
			const ctrl = createController();
			ctrl.enteredName = 'prep Name';
			ctrl.baseDataset = null;
			ctrl.alreadyExistingName = false;

			//when
			const disableForm = ctrl.anyMissingEntries();

			//then
			expect(disableForm).toBeTruthy();
		});

		it('should disable form submission when entered name already exists', () => {
			//given
			const ctrl = createController();
			ctrl.enteredName = 'prep Name';
			ctrl.baseDataset = {};
			ctrl.alreadyExistingName = true;

			//when
			const disableForm = ctrl.anyMissingEntries();

			//then
			expect(disableForm).toBeTruthy();
		});
	});

	describe('getImportTitle', () => {
		it('should return IMPORT_IN_PROGRESS when import is disabled', () => {
			//given
			const ctrl = createController();
			ctrl.importDisabled = true;

			//when
			const title = ctrl.getImportTitle();

			//then
			expect(title).toBe('IMPORT_IN_PROGRESS');
		});

		it('should return TRY_CHANGING_NAME when name exists', () => {
			//given
			const ctrl = createController();
			ctrl.alreadyExistingName = true;

			//when
			const title = ctrl.getImportTitle();

			//then
			expect(title).toBe('TRY_CHANGING_NAME');
		});

		it('should return IMPORT_FILE_DESCRIPTION by default', () => {
			//given
			const ctrl = createController();
			ctrl.alreadyExistingName = false;

			//when
			const title = ctrl.getImportTitle();

			//then
			expect(title).toBe('IMPORT_FILE_DESCRIPTION');
		});
	});
});
