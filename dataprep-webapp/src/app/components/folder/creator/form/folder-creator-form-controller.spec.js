/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Folder Creator Form Controller', () => {
	let createController;
	let scope;
	let stateMock;

	beforeEach(angular.mock.module('data-prep.folder-creator', ($provide) => {
		stateMock = {
			inventory: {
				folder: {
					metadata: {
						id: 'folder1',
					},
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new(true);

		createController = () => {
			return $componentController('folderCreatorForm', { $scope: scope });
		};
	}));

	it('should init name', () => {
		// when
		const ctrl = createController();

		// then
		expect(ctrl.name).toBe('');
	});

	describe('createFolder', () => {
		let ctrl;
		
		beforeEach(inject(($q, FolderService, StateService) => {
			// given
			spyOn(FolderService, 'create').and.returnValue($q.when());
			spyOn(FolderService, 'refresh').and.returnValue($q.when());
			spyOn(StateService, 'toggleFolderCreator').and.returnValue();

			ctrl = createController();
			ctrl.name = 'my new folder';
			ctrl.folderNameForm = {
				$commitViewValue: jasmine.createSpy('$commitViewValue'),
			};
		}));
		
		it('should commit form value', () => {
			// when
			ctrl.createFolder();

			// then
			expect(ctrl.folderNameForm.$commitViewValue).toHaveBeenCalled();
		});
		
		it('should create folder', inject((FolderService) => {
			// given
			ctrl.name = 'my new folder';
			
			// when
			ctrl.createFolder();

			// then
			expect(FolderService.create).toHaveBeenCalledWith('folder1', 'my new folder');
		}));
		
		it('should toggle folder creator', inject((StateService) => {
			// when
			ctrl.createFolder();
			scope.$digest();

			// then
			expect(StateService.toggleFolderCreator).toHaveBeenCalled();
		}));
		
		it('should refresh current folder', inject((FolderService) => {
			// when
			ctrl.createFolder();
			scope.$digest();

			// then
			expect(FolderService.refresh).toHaveBeenCalledWith('folder1');
		}));
	});
});
