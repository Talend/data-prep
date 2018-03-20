/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Datagrid header controller', () => {
	'use strict';

	let createController;
	let scope;
	let stateMock;

	const column = {
		id: '0001',
		name: 'Original name',
		type: 'string',
	};

	const transformationsMock = () => {
		const transformations = [
			{
				name: 'uppercase',
				category: 'case',
				actionScope: [],
				items: null,
				parameters: null,
			},
			{
				name: 'rename',
				category: 'column_metadata',
				actionScope: ['column_metadata'],
				items: null,
				parameters: null,
			},
			{
				name: 'lowercase',
				category: 'case',
				actionScope: [],
				items: null,
				parameters: null,
			},
			{
				name: 'withParam',
				category: 'case',
				actionScope: [],
				items: null,
				parameters: [
					{
						name: 'param',
						type: 'string',
						default: '.',
						inputType: 'text',
					},
				],
			},
			{
				name: 'split',
				category: 'column_metadata',
				actionScope: ['column_metadata'],
				parameters: null,
				items: [
					{
						name: 'mode',
						values: [
							{ name: 'noparam' },
							{
								name: 'regex',
								parameters: [
									{
										name: 'regexp',
										type: 'string',
										default: '.',
										inputType: 'text',
									},
								],
							},
							{
								name: 'index',
								parameters: [
									{
										name: 'index',
										type: 'integer',
										default: '5',
										inputType: 'number',
									},
								],
							},
							{
								name: 'threeParams',
								parameters: [
									{
										name: 'index',
										type: 'numeric',
										default: '5',
										inputType: 'number',
									},
									{
										name: 'index2',
										type: 'float',
										default: '5',
										inputType: 'number',
									},
									{
										name: 'index3',
										type: 'double',
										default: '5',
										inputType: 'number',
									},
								],
							},
						],
					},
				],
			},
		];
		return {
			allTransformations: transformations,
		};
	};

	beforeEach(angular.mock.module('data-prep.datagrid-header', ($provide) => {
		stateMock = {
			playground: {
				preparation: {
					id: 'prepId',
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($rootScope, $controller) => {
		scope = $rootScope.$new();

		createController = () => {
			const ctrlFn = $controller('DatagridHeaderCtrl', {
				$scope: scope,
			}, true);

			ctrlFn.instance.column = column;
			const ctrl = ctrlFn();
			scope.$digest();
			return ctrl;
		};
	}));

	describe('init transformation', () => {

		beforeEach(inject(($q, StateService, ColumnTypesService) => {
			spyOn(ColumnTypesService, 'refreshSemanticDomains').and.returnValue();
			spyOn(ColumnTypesService, 'refreshTypes').and.returnValue();
		}));

		it('should refresh domains and types',
			inject((StateService, ColumnTypesService) => {
				// given
				const ctrl = createController();

				// when
				ctrl.initTransformations();

				//then
				expect(ColumnTypesService.refreshSemanticDomains).toHaveBeenCalledWith(ctrl.column.id);
				expect(ColumnTypesService.refreshTypes).toHaveBeenCalled();
			})
		);

		it('should get and init only "column_metadata" category transformations',
			inject(($q, TransformationService) => {
				//given
				spyOn(TransformationService, 'getTransformations').and.returnValue($q.when(transformationsMock()));
				const ctrl = createController();

				//when
				ctrl.initTransformations();
				scope.$digest();

				//then
				expect(TransformationService.getTransformations).toHaveBeenCalledWith('column', column);
				expect(ctrl.transformations.length).toBe(2);
				expect(ctrl.transformations[0].name).toBe('rename');
				expect(ctrl.transformations[1].name).toBe('split');
			})
		);

		it('should NOT get transformations if transformations are being fetched',
			inject(($q, TransformationService) => {
				//given
				spyOn(TransformationService, 'getTransformations').and.returnValue($q.when());
				let ctrl = createController();
				ctrl.initTransformations();

				//when
				ctrl.initTransformations();

				//then
				expect(TransformationService.getTransformations.calls.count()).toBe(1);
			})
		);

		it('should NOT get transformations if transformations are already initialized',
			inject((TransformationService) => {
				//given
				spyOn(TransformationService, 'getTransformations').and.returnValue();
				let ctrl = createController();
				ctrl.transformations = [{}];

				//when
				ctrl.initTransformations();

				//then
				expect(TransformationService.getTransformations).not.toHaveBeenCalled();
			})
		);

		it('should change inProgress and error flags on error', inject(($q, TransformationService) => {
			//given
			spyOn(TransformationService, 'getTransformations').and.returnValue($q.reject('server error'));
			const ctrl = createController();
			expect(ctrl.transformationsRetrieveError).toBeFalsy();
			expect(ctrl.initTransformationsInProgress).toBeFalsy();

			ctrl.transformationsRetrieveError = true;

			//when
			ctrl.initTransformations();
			expect(ctrl.initTransformationsInProgress).toBeTruthy();
			expect(ctrl.transformationsRetrieveError).toBeFalsy();
			scope.$digest();

			//then
			expect(ctrl.transformationsRetrieveError).toBeTruthy();
			expect(ctrl.initTransformationsInProgress).toBeFalsy();
		}));
	});

	describe('update column name', () => {
		beforeEach(inject(($q, PlaygroundService) => {
			spyOn(PlaygroundService, 'appendStep').and.returnValue($q.when(true));
		}));

		it('should update column name', inject((PlaygroundService) => {
			//given
			const ctrl = createController();
			ctrl.newName = 'new name';

			//when
			ctrl.updateColumnName();

			//then
			const expectedParams = [{
				action: 'rename_column',
				parameters: {
					new_column_name: 'new name',
					scope: 'column',
					column_id: '0001',
					column_name: 'Original name',
				}
			}];

			expect(PlaygroundService.appendStep).toHaveBeenCalledWith(expectedParams);
		}));

		it('should turn off edition mode after name update', () => {
			//given
			const ctrl = createController();
			ctrl.newName = 'new name';
			ctrl.isEditMode = true;

			//when
			ctrl.updateColumnName();
			scope.$apply();

			//then
			expect(ctrl.isEditMode).toBe(false);
		});

		it('should return true when name has changed and is not empty', () => {
			//given
			const ctrl = createController();
			ctrl.newName = 'new name';

			//when
			const hasChanged = ctrl.nameHasChanged();

			//then
			expect(hasChanged).toBe(true);
		});

		it('should return false when name is unchanged', () => {
			//given
			const ctrl = createController();
			ctrl.setEditMode(true);
			ctrl.newName = 'Original name';

			//when
			const hasChanged = ctrl.nameHasChanged();

			//then
			expect(hasChanged).toBeFalsy();
		});

		it('should return false when name is falsy', () => {
			//given
			const ctrl = createController();
			ctrl.setEditMode(true);
			ctrl.newName = '';

			//when
			const hasChanged = ctrl.nameHasChanged();

			//then
			expect(hasChanged).toBeFalsy();
		});

		it('should update edition mode to true', () => {
			//given
			const ctrl = createController();
			expect(ctrl.isEditMode).toBeFalsy();

			//when
			ctrl.setEditMode(true);

			//then
			expect(ctrl.isEditMode).toBeTruthy();
		});

		it('should update edition mode to false', () => {
			//given
			const ctrl = createController();
			ctrl.isEditMode = true;

			//when
			ctrl.setEditMode(false);

			//then
			expect(ctrl.isEditMode).toBe(false);
		});

		it('should reset name when edition mode is set to true', () => {
			//given
			const ctrl = createController();
			ctrl.newName = 'new name';

			//when
			ctrl.setEditMode(true);

			//then
			expect(ctrl.newName).toBe('Original name');
		});
	});

	describe('filter', () => {
		it('should add filter',
			inject(() => {
				// given
				const ctrl = createController();
				ctrl.column = {
					id: 'id1',
					name: 'col1'
				};
				spyOn(ctrl.filterManagerService, 'addFilter');

				// when
				ctrl.addFilter('valid_records');

				//then
				expect(ctrl.filterManagerService.addFilter).toHaveBeenCalledWith('valid_records', 'id1', 'col1');
			})
		);
	});
});
