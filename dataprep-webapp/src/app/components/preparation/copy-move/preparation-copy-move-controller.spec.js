/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Preparation copy/move controller', () => {
	let createController;
	let scope;
	let stateMock;

	beforeEach(angular.mock.module('data-prep.preparation-copy-move', ($provide) => {
		stateMock = {
			home: {
				preparations: {
					copyMove: {
						isVisible: true,
						initialFolder: { id: 'L215L2ZvbGRlcg==', path: '/my/folder' },
						preparation: { id: '863a21ab23c66' },
					},
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($state, $q, $rootScope, $componentController, PreparationService, FolderService, MessageService, StateService) => {
		scope = $rootScope.$new(true);

		createController = () => {
			return $componentController('preparationCopyMove', {
				$scope: scope,
			});
		};

		spyOn(FolderService, 'refresh').and.returnValue($q.when());
		spyOn(MessageService, 'success').and.returnValue();
		spyOn(PreparationService, 'copy').and.returnValue($q.when());
		spyOn(PreparationService, 'move').and.returnValue($q.when());
		spyOn(StateService, 'toggleCopyMovePreparation').and.returnValue();
	}));

	describe('copy', () => {
		let preparation;
		let initialFolder;
		let destinationFolder;
		let name;

		beforeEach(() => {
			// given
			preparation = stateMock.home.preparations.copyMove.preparation;
			initialFolder = stateMock.home.preparations.copyMove.initialFolder;
			destinationFolder = { id: 'L2NvcHkvcGF0aA==', path: '/copy/path' };
			name = 'new name';
			const ctrl = createController();

			// when
			ctrl.copy(preparation, destinationFolder, name);
			scope.$digest();
		});

		it('should call preparation copy service', inject((PreparationService) => {
			//then
			expect(PreparationService.copy).toHaveBeenCalledWith(
				preparation.id,
				destinationFolder.id,
				name
			);
		}));

		it('should refresh folder content', inject((FolderService) => {
			//then
			expect(FolderService.refresh).toHaveBeenCalledWith(initialFolder.id);
		}));

		it('should show message', inject((MessageService) => {
			//then
			expect(MessageService.success).toHaveBeenCalledWith(
				'PREPARATION_COPYING_SUCCESS_TITLE',
				'PREPARATION_COPYING_SUCCESS'
			);
		}));

		it('should toggle preparation copy/move modal', inject((StateService) => {
			//then
			expect(StateService.toggleCopyMovePreparation).toHaveBeenCalled();
		}));
	});

	describe('move', () => {
		let preparation;
		let initialFolder;
		let destinationFolder;
		let name;

		beforeEach(() => {
			//given
			preparation = stateMock.home.preparations.copyMove.preparation;
			initialFolder = stateMock.home.preparations.copyMove.initialFolder;
			destinationFolder = { id: 'L2NvcHkvcGF0aA==', path: '/copy/path' };
			name = 'new name';
			const ctrl = createController();

			//when
			ctrl.move(preparation, destinationFolder, name);
			scope.$digest();
		});

		it('should call preparation move service', inject((PreparationService) => {
			//then
			expect(PreparationService.move).toHaveBeenCalledWith(
				preparation.id,
				initialFolder.id,
				destinationFolder.id,
				name
			);
		}));

		it('should refresh folder content', inject(($q, TalendConfirmService, FolderService) => {
			//then
			expect(FolderService.refresh).toHaveBeenCalledWith(initialFolder.id);
		}));

		it('should show success message', inject((MessageService) => {
			//then
			expect(MessageService.success).toHaveBeenCalledWith(
				'PREPARATION_MOVING_SUCCESS_TITLE',
				'PREPARATION_MOVING_SUCCESS'
			);
		}));

		it('should toggle preparation copy/move modal', inject((StateService) => {
			//then
			expect(StateService.toggleCopyMovePreparation).toHaveBeenCalled();
		}));
	});
});
