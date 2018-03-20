/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('InventoryCopyMove component', () => {
	let scope;
	let createElement;
	let element;
	let controller;

	beforeEach(angular.mock.module('data-prep.inventory-copy-move'));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new();
		scope.item = { name: 'my item' };
		scope.initialFolder = { path: '0-folder1/1-folder11' };

		createElement = () => {
			element = angular.element(
				`<inventory-copy-move
                    initial-folder="initialFolder"
                    item="item"
                ></inventory-copy-move>`
			);
			$compile(element)(scope);
			scope.$digest();

			controller = element.controller('inventoryCopyMove');
		};
	}));

	beforeEach(inject(($q, FolderService) => {
		spyOn(FolderService, 'tree').and.returnValue($q.when({ folder: { id: '1' }, children: [] }));
	}));
	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('render', () => {
		it('should render copy/move elements', () => {
			// when
			createElement();
			scope.$digest();

			// then
			expect(element.find('folder-selection').length).toBe(1);
			expect(element.find('input#copy-move-name-input').length).toBe(1);
			expect(element.find('button').length).toBe(3);
		});
	});

	describe('form', () => {
		it('should disable submit buttons when form is invalid', () => {
			// given
			createElement();
			expect(element.find('#copy-move-cancel-btn').eq(0).attr('disabled')).toBeFalsy();
			expect(element.find('#copy-move-copy-btn').eq(0).attr('disabled')).toBeFalsy();
			expect(element.find('#copy-move-move-btn').eq(0).attr('disabled')).toBeFalsy();

			// when
			controller.newName = '';
			scope.$digest();

			// then
			expect(controller.copyMoveForm.$invalid).toBeTruthy();
			expect(element.find('#copy-move-cancel-btn').eq(0).attr('disabled')).toBeFalsy();
			expect(element.find('#copy-move-copy-btn').eq(0).attr('disabled')).toBe('disabled');
			expect(element.find('#copy-move-move-btn').eq(0).attr('disabled')).toBe('disabled');
		});

		describe('move', () => {
			beforeEach(() => {
				createElement();
			});

			it('should disable submit buttons while moving', () => {
				// given
				expect(element.find('#copy-move-cancel-btn').eq(0).attr('disabled')).toBeFalsy();
				expect(element.find('#copy-move-copy-btn').eq(0).attr('disabled')).toBeFalsy();
				expect(element.find('#copy-move-move-btn').eq(0).attr('disabled')).toBeFalsy();

				// when
				controller.isMoving = true;
				scope.$digest();

				// then
				expect(element.find('#copy-move-cancel-btn').eq(0).attr('disabled')).toBe('disabled');
				expect(element.find('#copy-move-copy-btn').eq(0).attr('disabled')).toBe('disabled');
				expect(element.find('#copy-move-move-btn').eq(0).attr('disabled')).toBe('disabled');
			});
		});

		describe('copy', () => {
			beforeEach(() => {
				createElement();
			});

			it('should disable submit buttons while copying', () => {
				// given
				expect(element.find('#copy-move-cancel-btn').eq(0).attr('disabled')).toBeFalsy();
				expect(element.find('#copy-move-copy-btn').eq(0).attr('disabled')).toBeFalsy();
				expect(element.find('#copy-move-move-btn').eq(0).attr('disabled')).toBeFalsy();

				// when
				controller.isCopying = true;
				scope.$digest();

				// then
				expect(element.find('#copy-move-cancel-btn').eq(0).attr('disabled')).toBe('disabled');
				expect(element.find('#copy-move-copy-btn').eq(0).attr('disabled')).toBe('disabled');
				expect(element.find('#copy-move-move-btn').eq(0).attr('disabled')).toBe('disabled');
			});
		});
	});
});
