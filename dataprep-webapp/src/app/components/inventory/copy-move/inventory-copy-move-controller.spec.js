/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('InventoryCopyMove controller', () => {
	let createController;
	let scope;
	let ctrl;
	let $element;

	beforeEach(angular.mock.module('data-prep.inventory-copy-move'));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();
		$element = {};
		createController = () => {
			return $componentController('inventoryCopyMove', {
					$scope: scope,
					$element: $element,
				},
				{
					item: { name: 'my item' },
					initialFolder: {
						path: 'folder1',
						name: 'my folder name',
					},
				});
		};
	}));

	describe('isActionDisabled', () => {
		beforeEach(() => {
			ctrl = createController();
			ctrl.destinationFolder = { name: 'my folder name', path: '/parent/child' };
			ctrl.copyMoveForm = {};
			ctrl.copyMoveForm.$commitViewValue = jasmine.createSpy('$commitViewValue');
		});

		it('should return true if isLoading true', () => {
			//given
			ctrl.isLoading = true;

			//then
			expect(ctrl.isActionDisabled()).toBeTruthy();

		});

		it('should return true if copyMoveForm is invalid', () => {
			//given
			ctrl.copyMoveForm.$invalid = true;

			//then
			expect(ctrl.isActionDisabled()).toBeTruthy();

		});

		it('should return true if isMoving is true', () => {
			//given
			ctrl.isMoving = true;

			//then
			expect(ctrl.isActionDisabled()).toBeTruthy();

		});

		it('should return true if isCopying is true', () => {
			//given
			ctrl.isCopying = true;

			//then
			expect(ctrl.isActionDisabled()).toBeTruthy();

		});
	});

	describe('copy', () => {
		beforeEach(() => {
			ctrl = createController();
			ctrl.destinationFolder = { name: 'my folder name', path: '/parent/child' };
			ctrl.copyMoveForm = {};
			ctrl.copyMoveForm.$commitViewValue = jasmine.createSpy('$commitViewValue');
		});

		it('should not call copy service', inject(($q) => {
			//given
			ctrl.onCopy = jasmine.createSpy('onCopy').and.returnValue($q.when(true));
			spyOn(ctrl, 'isActionDisabled').and.returnValue(true);

			//when
			ctrl.copy();

			//then
			expect(ctrl.copyMoveForm.$commitViewValue).not.toHaveBeenCalled();
		}));

		it('should call copy service', inject(($q) => {
			//given
			ctrl.onCopy = jasmine.createSpy('onCopy').and.returnValue($q.when(true));
			expect(ctrl.copyMoveForm.$commitViewValue).not.toHaveBeenCalled();
			expect(ctrl.onCopy).not.toHaveBeenCalled();

			//when
			ctrl.copy();

			//then
			expect(ctrl.copyMoveForm.$commitViewValue).toHaveBeenCalled();
			expect(ctrl.onCopy).toHaveBeenCalledWith({
				item: ctrl.item,
				destination: ctrl.destinationFolder,
				name: ctrl.newName,
			});
		}));

		it('should manage cloning flag', inject(($q) => {
			//given
			ctrl.onCopy = jasmine.createSpy('onCopy').and.returnValue($q.when(true));
			expect(ctrl.isCopying).toBeFalsy();

			//when
			ctrl.copy();
			expect(ctrl.isCopying).toBeTruthy();
			scope.$digest();

			//then
			expect(ctrl.isCopying).toBeFalsy();
		}));

		it('should reset flag and focus on input when copy fails', inject(($q) => {
			//given
			ctrl.onCopy = jasmine.createSpy('onCopy').and.returnValue($q.reject());
			ctrl._focusOnNameInput = jasmine.createSpy('_focusOnNameInput');

			//when
			ctrl.copy();
			expect(ctrl.isCopying).toBeTruthy();
			scope.$digest();

			//then
			expect(ctrl.isCopying).toBeFalsy();
			expect(ctrl._focusOnNameInput).toHaveBeenCalled();
		}));
	});

	describe('move', () => {
		beforeEach(() => {
			ctrl = createController();
			ctrl.destinationFolder = { name: 'my folder name', path: '/parent/child' };
			ctrl.copyMoveForm = {};
			ctrl.copyMoveForm.$commitViewValue = jasmine.createSpy('$commitViewValue');
		});

		it('should not call copy service', inject(($q) => {
			//given
			ctrl.onMove = jasmine.createSpy('onMove').and.returnValue($q.when(true));
			spyOn(ctrl, 'isActionDisabled').and.returnValue(true);

			//when
			ctrl.move();

			//then
			expect(ctrl.copyMoveForm.$commitViewValue).not.toHaveBeenCalled();
		}));

		it('should call move service', inject(($q) => {
			//given
			ctrl.onMove = jasmine.createSpy('onMove').and.returnValue($q.when(true));
			expect(ctrl.copyMoveForm.$commitViewValue).not.toHaveBeenCalled();
			expect(ctrl.onMove).not.toHaveBeenCalled();

			//when
			ctrl.move();

			//then
			expect(ctrl.copyMoveForm.$commitViewValue).toHaveBeenCalled();
			expect(ctrl.onMove).toHaveBeenCalledWith({
				item: ctrl.item,
				destination: ctrl.destinationFolder,
				name: ctrl.newName,
			});
		}));

		it('should manage moving flag', inject(($q) => {
			//given
			ctrl.onMove = jasmine.createSpy('onMove').and.returnValue($q.when(true));
			expect(ctrl.isMoving).toBeFalsy();

			//when
			ctrl.move();
			expect(ctrl.isMoving).toBeTruthy();
			scope.$digest();

			//then
			expect(ctrl.isMoving).toBeFalsy();
		}));

		it('should reset flag and focus on input when copy fails', inject(($q) => {
			//given
			ctrl.onMove = jasmine.createSpy('onMove').and.returnValue($q.reject());
			ctrl._focusOnNameInput = jasmine.createSpy('_focusOnNameInput');

			//when
			ctrl.move();
			expect(ctrl.isMoving).toBeTruthy();
			scope.$digest();

			//then
			expect(ctrl.isMoving).toBeFalsy();
			expect(ctrl._focusOnNameInput).toHaveBeenCalled();
		}));
	});
});
