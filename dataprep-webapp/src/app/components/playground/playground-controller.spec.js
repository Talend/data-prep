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

describe('Playground controller', () => {
	let createController;
	let scope;
	let stateMock;
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

	const createPreparation = { id: 'create-preparation-id' };

	beforeEach(angular.mock.module('data-prep.playground', ($provide) => {
		stateMock = {
			route: {
				previous: HOME_PREPARATIONS_ROUTE,
			},
			playground: {
				dataset: {},
				lookup: { actions: [] },
				preparationName: '',
				recipe: { current: { steps: [] } },
			},
			inventory: {
				homeFolder: { id: 'LW==' },
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

	beforeEach(inject(($rootScope, $q, $controller, $state, PlaygroundService, PreparationService, StateService) => {
		scope = $rootScope.$new();

		createController = () => {
			return $controller('PlaygroundCtrl', { $scope: scope });
		};

		spyOn(PlaygroundService, 'createOrUpdatePreparation').and.returnValue($q.when(createPreparation));
		spyOn(PreparationService, 'setName').and.returnValue($q.when());
		spyOn(PreparationService, 'move').and.returnValue($q.when());
		spyOn($state, 'go').and.returnValue();

		spyOn(StateService, 'setIsPreprationPickerVisible').and.returnValue();
		spyOn(StateService, 'setIsNameValidationVisible').and.returnValue();
		spyOn(StateService, 'setIsSavingPreparationFoldersLoading').and.returnValue();
		spyOn(StateService, 'setSavingPreparationFolders').and.returnValue();
	}));

	afterEach(inject(($stateParams) => {
		$stateParams.prepid = null;
		$stateParams.datasetid = null;
	}));

	describe('initialization', () => {
		beforeEach(inject(() => {
			stateMock.inventory = {
				preparations,
				datasets,
				folder: {
					metadata: {
						id: 'abcd'
					}
				}
			};
		}));

		describe('preparation', () => {
			beforeEach(inject(($q, $stateParams, PlaygroundService, PreparationService, StateService, DatasetService) => {
				spyOn(PreparationService, 'getDetails').and.returnValue($q.when(preparations[0]));
				spyOn(DatasetService, 'getMetadata').and.returnValue($q.when(datasets[0]));
				spyOn(PlaygroundService, 'startLoader').and.returnValue();
				spyOn(StateService, 'setIsLoadingPlayground').and.returnValue();
				spyOn(StateService, 'setPreviousRoute').and.returnValue();

				$stateParams.prepid = preparations[0].id;
				stateMock.playground.data = {
					metadata: {
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

			it('should set previous route to preparations home', inject((StateService) => {
				// when
				createController();

				// then
				expect(StateService.setPreviousRoute).toHaveBeenCalledWith(
					HOME_PREPARATIONS_ROUTE,
					{ folderId: 'abcd' }
				);
			}));

		});
	});

	describe('recipe header', () => {
		it('should create/update preparation with clean name on name edition confirmation', inject((PlaygroundService) => {
			// given
			const ctrl = createController();
			stateMock.playground.preparationName = '  my new name  ';

			// when
			ctrl.confirmPrepNameEdition(stateMock.playground.preparationName);

			// then
			expect(PlaygroundService.createOrUpdatePreparation).toHaveBeenCalledWith('my new name');
		}));

		it('should change route to preparation route on name edition confirmation', inject(($rootScope, $state) => {
			// given
			const ctrl = createController();
			stateMock.playground.preparationName = '  my new name  ';
			stateMock.playground.preparation = { id: 'fe6843da512545e' };

			// when
			ctrl.confirmPrepNameEdition(stateMock.playground.preparationName);
			$rootScope.$digest();

			// then
			expect($state.go).toHaveBeenCalledWith('playground.preparation', { prepid: createPreparation.id });
		}));

		it('should not call service create/updateName service if name is blank on name edition confirmation', inject((PlaygroundService) => {
			// given
			const ctrl = createController();

			// when
			ctrl.confirmPrepNameEdition(' ');

			// then
			expect(PlaygroundService.createOrUpdatePreparation).not.toHaveBeenCalled();
		}));
	});

	describe('lookup', () => {
		beforeEach(inject(($q, LookupService, StateService) => {
			spyOn(LookupService, 'initLookups').and.returnValue($q.when());
			spyOn(StateService, 'setLookupVisibility').and.returnValue();
			spyOn(StateService, 'setStepInEditionMode').and.returnValue();

			stateMock.playground.grid = {
				selectedColumns: [{
					id: '0001',
					name: 'firstname',
				}]
			};

		}));

		it('should load lookup panel when it is hidden', inject((LookupService) => {
			// given
			stateMock.playground.lookup.visibility = false;
			const ctrl = createController();

			// when
			ctrl.toggleLookup();

			// then
			expect(LookupService.initLookups).toHaveBeenCalled();
		}));

		it('should display lookup panel when it is hidden', inject((StateService) => {
			// given
			stateMock.playground.lookup.visibility = false;
			const ctrl = createController();

			// when
			ctrl.toggleLookup();
			scope.$digest();

			// then
			expect(StateService.setLookupVisibility).toHaveBeenCalledWith(true, undefined);
		}));

		it('should hide lookup panel when it is visible', inject((LookupService, StateService) => {
			// given
			stateMock.playground.lookup.visibility = true;
			const ctrl = createController();

			// when
			ctrl.toggleLookup();

			// then
			expect(LookupService.initLookups).not.toHaveBeenCalled();
			expect(StateService.setStepInEditionMode).toHaveBeenCalledWith(null);
			expect(StateService.setLookupVisibility).toHaveBeenCalledWith(false);
		}));
	});

	describe('apply other preparation steps', () => {
		it('should update the display modal state', inject((StateService) => {
			// given
			const ctrl = createController();
			expect(StateService.setIsPreprationPickerVisible).not.toHaveBeenCalled();

			// when
			ctrl.showPreparationPicker();

			// then
			expect(StateService.setIsPreprationPickerVisible).toHaveBeenCalledWith(true);
		}));

		it('should fetch compatible preparations', inject((DatasetService) => {
			// given
			spyOn(DatasetService, 'getCompatiblePreparations').and.returnValue();
			const ctrl = createController();
			const datasetId = 'abcde-123';

			// when
			ctrl.fetchCompatiblePreparations(datasetId);

			// then
			expect(DatasetService.getCompatiblePreparations).toHaveBeenCalledWith(datasetId);
		}));

		it('should copy reference steps', inject(($q, PlaygroundService) => {
			// given
			const referenceId = '17614ef63541ba25c48';
			const ctrl = createController();
			ctrl.displayPreparationPicker = true;

			spyOn(PlaygroundService, 'copySteps').and.returnValue($q.when());

			// when
			ctrl.applySteps(referenceId);

			// then
			expect(PlaygroundService.copySteps).toHaveBeenCalledWith(referenceId);
		}));

		it('should hide picker after steps copy', inject(($q, PlaygroundService, StateService) => {
			// given
			const ctrl = createController();
			spyOn(PlaygroundService, 'copySteps').and.returnValue($q.when());
			expect(StateService.setIsPreprationPickerVisible).not.toHaveBeenCalled();

			// when
			ctrl.applySteps();
			scope.$digest();

			// then
			expect(StateService.setIsPreprationPickerVisible).toHaveBeenCalledWith(false);
		}));
	});

	describe('close', () => {
		let ctrl;
		let preparation;

		beforeEach(inject(($q, PreparationService, StateService) => {
			preparation = { id: '9af874865e42b546', draft: true };
			stateMock.playground.preparation = preparation;
			stateMock.route.previous = HOME_PREPARATIONS_ROUTE;

			spyOn(PreparationService, 'delete').and.returnValue($q.when(true));
			spyOn(StateService, 'resetPlayground').and.returnValue();
			spyOn(StateService, 'setIsSavingPreparation').and.returnValue();

			ctrl = createController();
		}));

		describe('before close', () => {
			it('should reset and redirect with NOT implicit preparation', inject((PlaygroundService) => {
				// given
				preparation.draft = false;
				spyOn(PlaygroundService, 'close').and.returnValue();

				// when
				ctrl.beforeClose();

				// then
				expect(PlaygroundService.close).toHaveBeenCalled();
			}));

			it('should show preparation save/discard modal with implicit preparation if there are steps', () => {
				// given
				spyOn(ctrl, 'showNameValidationModal').and.returnValue();
				stateMock.playground.recipe.current.steps = [{ id: '000' }];

				// when
				ctrl.beforeClose();

				// then
				expect(ctrl.showNameValidationModal).toHaveBeenCalled();
			});

			it('should discard the preparation if there are no steps', () => {
				// given
				spyOn(ctrl, 'discardSaveOnClose').and.returnValue();

				// when
				ctrl.beforeClose();

				// then
				expect(ctrl.discardSaveOnClose).toHaveBeenCalled();
			});
		});

		describe('discard preparation', () => {
			it('should call startLoader', inject((PlaygroundService) => {
				// given
				spyOn(PlaygroundService, 'startLoader');

				// when
				ctrl.discardSaveOnClose();

				// then
				expect(PlaygroundService.startLoader).toHaveBeenCalled();
			}));

			it('should call stopLoader', inject((PlaygroundService) => {
				// given
				spyOn(PlaygroundService, 'stopLoader');

				// when
				ctrl.discardSaveOnClose();
				scope.$digest();

				// then
				expect(PlaygroundService.stopLoader).toHaveBeenCalled();
			}));

			it('should delete current preparation', inject((PreparationService) => {
				// when
				ctrl.discardSaveOnClose();

				// then
				expect(PreparationService.delete).toHaveBeenCalledWith(preparation);
			}));

			it('should reset and redirect to previous route', inject(($timeout, $state, StateService) => {
				// when
				ctrl.discardSaveOnClose();
				scope.$digest();
				$timeout.flush();

				// then
				expect(StateService.resetPlayground).toHaveBeenCalled();
				expect($state.go).toHaveBeenCalledWith(HOME_PREPARATIONS_ROUTE, undefined);
			}));
		});

		describe('save preparation', () => {
			it('should determinate if submit is disabled', () => {
				// given
				ctrl.savePreparationForm = {
					'$invalid': false,
				};
				stateMock.playground.isSavingPreparationFoldersLoading = true;
				// when
				const isSubmitDisabled = ctrl.isSubmitDisabled();

				// then
				expect(isSubmitDisabled).toBeTruthy();
			});

			it('should determinate if submit is enabled', () => {
				// given
				ctrl.savePreparationForm = {
					'$invalid': false,
				};
				stateMock.playground.isSavingPreparationFoldersLoading = false;

				// when
				const isSubmitDisabled = ctrl.isSubmitDisabled();

				// then
				expect(isSubmitDisabled).toBeFalsy();
			});

			it('should change preparation name when destination is home', inject((PreparationService) => {
				// given
				ctrl.destinationFolder = { id: 'LW==', path: '' };
				stateMock.playground.preparation = { id: '541da3f5c64' };
				stateMock.playground.preparationName = '  my preparation ';

				expect(PreparationService.setName).not.toHaveBeenCalled();

				// when
				ctrl.confirmSaveOnClose();

				// then
				expect(PreparationService.setName).toHaveBeenCalledWith('541da3f5c64', 'my preparation');
			}));

			it('should move preparation when destination has changed', inject((PreparationService) => {
				// given
				ctrl.destinationFolder = { id: 'L215L2ZvbGRlcg==', path: '/my/folder' };
				stateMock.playground.preparation = { id: '541da3f5c64' };
				stateMock.playground.preparationName = '  my preparation ';

				expect(PreparationService.move).not.toHaveBeenCalled();

				// when
				ctrl.confirmSaveOnClose();

				// then
				expect(PreparationService.move).toHaveBeenCalledWith('541da3f5c64', 'LW==', 'L215L2ZvbGRlcg==', 'my preparation');
			}));

			it('should reset and redirect to previous route', inject(($timeout, $state, StateService) => {
				// given
				ctrl.destinationFolder = { path: '/my/folder' };

				// when
				ctrl.confirmSaveOnClose();
				scope.$digest();
				$timeout.flush();

				// then
				expect(StateService.resetPlayground).toHaveBeenCalled();
				expect($state.go).toHaveBeenCalledWith(HOME_PREPARATIONS_ROUTE, undefined);
			}));

			it('should determinate when submit is loading', () => {
				// given
				stateMock.playground.isSavingPreparation = true;

				// when
				const isSubmitLoading = ctrl.isSubmitLoading();

				// then
				expect(isSubmitLoading).toBeTruthy();
			});

			it('should manage isSaving flag', inject((StateService) => {
				// given
				ctrl.destinationFolder = { path: '/my/folder' };
				expect(StateService.setIsSavingPreparation).not.toHaveBeenCalled();

				// when
				ctrl.confirmSaveOnClose();
				expect(StateService.setIsSavingPreparation).toHaveBeenCalledWith(true);
				scope.$digest();

				// then
				expect(StateService.setIsSavingPreparation).toHaveBeenCalledWith(false);
			}));
		});
	});

	describe('feedback', () => {
		it('should open feedback modal', inject((StateService) => {
			// given
			spyOn(StateService, 'showFeedback').and.returnValue();
			const ctrl = createController();

			// when
			ctrl.openFeedbackForm();

			// then
			expect(StateService.showFeedback).toHaveBeenCalled();
		}));
	});

	describe('onboarding', () => {
		it('should start onboarding tour', inject((OnboardingService) => {
			// given
			spyOn(OnboardingService, 'startTour').and.returnValue();
			const ctrl = createController();
			const tourId = 'tour-123';

			// when
			ctrl.startOnBoarding(tourId);

			// then
			expect(OnboardingService.startTour).toHaveBeenCalledWith(tourId);
		}));
	});

	describe('dataset parameters', () => {
		beforeEach(inject(($q, StateService, PlaygroundService) => {
			spyOn(StateService, 'hideDatasetParameters').and.returnValue();
			spyOn(StateService, 'toggleDatasetParameters').and.returnValue();
			spyOn(StateService, 'setIsSendingDatasetParameters').and.returnValue();
			spyOn(PlaygroundService, 'changeDatasetParameters').and.returnValue($q.when());
		}));

		it('should open dataset parameters', inject((StateService) => {
			// given
			const ctrl = createController();
			expect(StateService.toggleDatasetParameters).not.toHaveBeenCalled();

			// when
			ctrl.toggleParameters();

			// then
			expect(StateService.toggleDatasetParameters).toHaveBeenCalled();
		}));

		it('should manage progress flag', inject((StateService) => {
			// given
			const ctrl = createController();
			const parameters = { separator: ';', encoding: 'UTF-8' };

			expect(StateService.setIsSendingDatasetParameters).not.toHaveBeenCalled();

			// when
			ctrl.changeDatasetParameters(parameters);
			expect(StateService.setIsSendingDatasetParameters).toHaveBeenCalledWith(true);
			scope.$digest();

			// then
			expect(StateService.setIsSendingDatasetParameters).toHaveBeenCalledWith(false);
		}));

		it('should call parameter change function', inject((PlaygroundService) => {
			// given
			const ctrl = createController();
			const parameters = { separator: ';', encoding: 'UTF-8' };

			expect(PlaygroundService.changeDatasetParameters).not.toHaveBeenCalled();

			// when
			ctrl.changeDatasetParameters(parameters);

			// then
			expect(PlaygroundService.changeDatasetParameters).toHaveBeenCalled();
		}));

		it('should hide dataset parameters', inject((StateService) => {
			// given
			const ctrl = createController();
			const parameters = { separator: ';', encoding: 'UTF-8' };

			expect(StateService.hideDatasetParameters).not.toHaveBeenCalled();

			// when
			ctrl.changeDatasetParameters(parameters);
			scope.$digest();

			// then
			expect(StateService.hideDatasetParameters).toHaveBeenCalled();
		}));
	});

	describe('preview in progress', () => {
		it('should check if preview is in progress', inject((PreviewService) => {
			// given
			spyOn(PreviewService, 'previewInProgress').and.returnValue();
			const ctrl = createController();

			// when
			ctrl.previewInProgress();

			// then
			expect(PreviewService.previewInProgress).toHaveBeenCalled();
		}));
	});

	describe('Name Validation Modal', () => {
		it('should show Name Validation modal', inject(($q, StateService, FolderService) => {
			// given
			spyOn(FolderService, 'tree').and.returnValue($q.when());
			const ctrl = createController();

			// when
			ctrl.showNameValidationModal();

			// then
			expect(StateService.setIsNameValidationVisible).toHaveBeenCalledWith(true);
			expect(StateService.setIsSavingPreparationFoldersLoading).toHaveBeenCalledWith(true);
		}));

		it('should fetch folders', inject(($q, StateService, FolderService) => {
			// given
			spyOn(FolderService, 'tree').and.returnValue($q.when());
			const ctrl = createController();

			// when
			ctrl.showNameValidationModal();

			// then
			expect(FolderService.tree).toHaveBeenCalled();
		}));

		it('should store folders after fetching', inject(($q, StateService, FolderService) => {
			// given
			spyOn(FolderService, 'tree').and.returnValue($q.when({folders: []}));
			const ctrl = createController();

			// when
			ctrl.showNameValidationModal();
			scope.$digest();

			// then
			expect(StateService.setSavingPreparationFolders).toHaveBeenCalledWith({folders: []});
			expect(StateService.setIsSavingPreparationFoldersLoading).toHaveBeenCalledWith(false);
		}));
	});

});
