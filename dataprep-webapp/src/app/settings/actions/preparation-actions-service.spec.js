/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import { HOME_PREPARATIONS_ROUTE } from '../../index-route';

describe('Preparation actions service', () => {
	let stateMock;

	beforeEach(angular.mock.module('app.settings.actions', ($provide) => {
		stateMock = {
			inventory: {
				folder: {
					sort: {
						field: 'date',
						isDescending: false,
					},
					metadata: { id: 'folder 1' }
				},
			}
		};
		$provide.constant('state', stateMock);
	}));

	describe('dispatch @@preparation/SORT', () => {
		it('should set sort in app state',
			inject(($q, FolderService, PreparationActionsService) => {
				// given
				spyOn(FolderService, 'changeSort').and.returnValue($q.when());
				const action = {
					type: '@@preparation/SORT',
					payload: {
						method: 'changeSort',
						field: 'name',
						isDescending: true,
					},
				};

				// when
				PreparationActionsService.dispatch(action);

				// then
				expect(FolderService.changeSort).toHaveBeenCalledWith(action.payload);
			})
		);
	});

	describe('dispatch @@preparation/FOLDER_REMOVE', () => {
		it('should remove folder', inject(($q, FolderService, PreparationActionsService) => {
			// given
			spyOn(FolderService, 'remove').and.returnValue($q.when());
			const action = {
				type: '@@preparation/FOLDER_REMOVE',
				payload: {
					method: 'remove',
					id: '1f387a645c84',
				}
			};

			// when
			PreparationActionsService.dispatch(action);

			// then
			expect(FolderService.remove).toHaveBeenCalledWith(action.payload);
		}));
	});

	describe('dispatch @@preparation/CREATE', () => {
		it('should toggle preparation creator', inject((StateService, PreparationActionsService) => {
			// given
			const action = {
				type: '@@preparation/CREATE',
				payload: {
					method: 'togglePreparationCreator',
					args: [],
				}
			};
			spyOn(StateService, 'togglePreparationCreator').and.returnValue();

			// when
			PreparationActionsService.dispatch(action);

			// then
			expect(StateService.togglePreparationCreator).toHaveBeenCalled();
		}));
	});

	describe('dispatch @@preparation/FOLDER_FETCH', () => {
		const folderId = '';

		beforeEach(inject(($q, $stateParams, StateService, FolderService, PreparationActionsService) => {
			// given
			$stateParams.folderId = folderId;
			spyOn(StateService, 'setPreviousRoute').and.returnValue();
			spyOn(StateService, 'setFetchingInventoryPreparations').and.returnValue();
			spyOn(FolderService, 'init').and.returnValue($q.when());

			const action = {
				type: '@@preparation/FOLDER_FETCH',
				payload: {
					method: 'init',
					args: [],
				}
			};

			// when
			PreparationActionsService.dispatch(action);
		}));

		it('should set current folder as the previous route for redirection',
			inject((StateService) => {
				// then
				expect(StateService.setPreviousRoute).toHaveBeenCalledWith(
					HOME_PREPARATIONS_ROUTE,
					{ folderId }
				);
			})
		);

		it('should init the folder which id is in url param',
			inject((FolderService) => {
				// then
				expect(FolderService.init).toHaveBeenCalledWith(folderId);
			})
		);

		it('should manage fetching flag',
			inject(($rootScope, StateService) => {
				// then
				expect(StateService.setFetchingInventoryPreparations)
					.toHaveBeenCalledWith(true);

				// when
				$rootScope.$digest();

				// then
				expect(StateService.setFetchingInventoryPreparations)
					.toHaveBeenCalledWith(false);
			})
		);
	});

	describe('dispatch @@preparation/COPY_MOVE', () => {
		it('should toggle preparation copy move', inject((StateService, PreparationActionsService) => {
			// given
			const folder = stateMock.inventory.folder.metadata;
			const preparation = { id: 'prep 1' };
			const action = {
				type: '@@preparation/COPY_MOVE',
				payload: {
					method: 'toggleCopyMovePreparation',
					args: [],
					model: preparation,
				}
			};
			spyOn(StateService, 'toggleCopyMovePreparation').and.returnValue();

			// when
			PreparationActionsService.dispatch(action);

			// then
			expect(StateService.toggleCopyMovePreparation)
				.toHaveBeenCalledWith(folder, preparation);
		}));

		it('should show copy/move modal', inject(($q, StateService, FolderService, PreparationActionsService) => {
			// given
			const preparation = { id: 'prep 1' };
			const action = {
				type: '@@preparation/COPY_MOVE',
				payload: {
					method: 'toggleCopyMovePreparation',
					args: [],
					model: preparation,
				}
			};
			spyOn(StateService, 'toggleCopyMovePreparation').and.returnValue();
			spyOn(StateService, 'setCopyMoveTreeLoading').and.returnValue();
			spyOn(FolderService, 'tree').and.returnValue($q.when());

			// when
			PreparationActionsService.dispatch(action);

			// then
			expect(StateService.setCopyMoveTreeLoading).toHaveBeenCalledWith(true);
			expect(FolderService.tree).toHaveBeenCalled();
		}));

		it('should store folders after fetching', inject(($q, $rootScope, StateService, FolderService, PreparationActionsService) => {
			// given
			const preparation = { id: 'prep 1' };
			const action = {
				type: '@@preparation/COPY_MOVE',
				payload: {
					method: 'toggleCopyMovePreparation',
					args: [],
					model: preparation,
				}
			};
			spyOn(StateService, 'toggleCopyMovePreparation').and.returnValue();
			spyOn(StateService, 'setCopyMoveTreeLoading').and.returnValue();
			spyOn(StateService, 'setCopyMoveTree').and.returnValue();
			spyOn(FolderService, 'tree').and.returnValue($q.when({folders: []}));

			// when
			PreparationActionsService.dispatch(action);
			$rootScope.$digest();

			// then
			expect(StateService.setCopyMoveTree).toHaveBeenCalledWith({folders: []});
		}));
	});

	describe('dispatch @@preparation/SUBMIT_EDIT', () => {
		let preparation;
		let folder;
		let action;
		beforeEach(inject(($q, StateService, FolderService, PreparationService) => {
			preparation = { id: 'prepId', name: 'prep1', type: 'preparation' };
			folder = { id: 'folderId', name: 'folder1', type: 'folder' };
			action = {
				type: '@@preparation/SUBMIT_EDIT',
				payload: {
					method: '',
					args: [],
					model: preparation,
				}
			};

			spyOn(StateService, 'disableInventoryEdit').and.returnValue();
			spyOn(PreparationService, 'setName').and.returnValue($q.when());
			spyOn(FolderService, 'rename').and.returnValue($q.when());
			spyOn(FolderService, 'refresh').and.returnValue($q.when());
		}));

		it('should disable title edition', inject((StateService, PreparationActionsService) => {
			// when
			PreparationActionsService.dispatch(action);

			// then
			expect(StateService.disableInventoryEdit).toHaveBeenCalledWith(preparation);
		}));

		it('should NOT set name if it hasn\'t change',
			inject((PreparationService, FolderService, PreparationActionsService) => {
				// given
				action.payload.value = preparation.name;

				// when
				PreparationActionsService.dispatch(action);

				// then
				expect(PreparationService.setName).not.toHaveBeenCalled();
				expect(FolderService.rename).not.toHaveBeenCalled();
			})
		);

		it('should NOT set name if it is empty', inject((PreparationService, PreparationActionsService) => {
			// given
			action.payload.value = '';

			// when
			PreparationActionsService.dispatch(action);

			// then
			expect(PreparationService.setName).not.toHaveBeenCalled();
		}));

		it('should set new preparation name', inject((PreparationService, PreparationActionsService) => {
			// given
			action.payload.value = 'New prep1 name';

			// when
			PreparationActionsService.dispatch(action);

			// then
			expect(PreparationService.setName).toHaveBeenCalledWith(
				preparation.id,
				'New prep1 name'
			);
		}));

		it('should rename folder', inject((FolderService, PreparationActionsService) => {
			// given
			action.payload.value = 'New folder1 name';
			action.payload.model = folder;

			// when
			PreparationActionsService.dispatch(action);

			// then
			expect(FolderService.rename).toHaveBeenCalledWith(
				folder.id,
				'New folder1 name'
			);
		}));

		it('should set refresh current folder', inject(($rootScope, FolderService, PreparationActionsService) => {
			// given
			const currentFolderId = stateMock.inventory.folder.metadata.id;
			action.payload.value = 'New prep1 name';

			// when
			PreparationActionsService.dispatch(action);
			$rootScope.$digest();

			// then
			expect(FolderService.refresh).toHaveBeenCalledWith(currentFolderId);
		}));
	});

	describe('dispatch @@preparation/REMOVE', () => {
		const preparation = { id: 'prep 1' };

		beforeEach(inject(($rootScope, $q, MessageService, TalendConfirmService,
		                   FolderService, PreparationService, PreparationActionsService) => {
			// given
			spyOn(TalendConfirmService, 'confirm').and.returnValue($q.when());
			spyOn(PreparationService, 'delete').and.returnValue();
			spyOn(FolderService, 'refresh').and.returnValue();
			spyOn(MessageService, 'success').and.returnValue();

			const action = {
				type: '@@preparation/REMOVE',
				payload: {
					method: 'delete',
					args: [],
					model: preparation,
				}
			};

			// when
			PreparationActionsService.dispatch(action);
			$rootScope.$digest();
		}));

		it('should ask confirmation', inject((TalendConfirmService) => {
			// then
			expect(TalendConfirmService.confirm).toHaveBeenCalledWith(
				['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'],
				{ type: 'preparation', name: preparation.name }
			);
		}));

		it('should remove preparation', inject((PreparationService) => {
			// then
			expect(PreparationService.delete).toHaveBeenCalledWith(preparation);
		}));

		it('should refresh current folder', inject((FolderService) => {
			// given
			const currentFolderId = stateMock.inventory.folder.metadata.id;

			// then
			expect(FolderService.refresh).toHaveBeenCalledWith(currentFolderId);
		}));

		it('should display success message', inject((MessageService) => {
			// then
			expect(MessageService.success).toHaveBeenCalledWith(
				'PREPARATION_REMOVE_SUCCESS_TITLE',
				'REMOVE_SUCCESS',
				{ type: 'preparation', name: preparation.name }
			);
		}));
	});

});
