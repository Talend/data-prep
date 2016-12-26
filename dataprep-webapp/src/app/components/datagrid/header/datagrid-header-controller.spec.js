/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

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

	const types = [
		{ id: 'ANY', name: 'any', labelKey: 'ANY' },
		{ id: 'STRING', name: 'string', labelKey: 'STRING' },
		{ id: 'NUMERIC', name: 'numeric', labelKey: 'NUMERIC' },
		{ id: 'INTEGER', name: 'integer', labelKey: 'INTEGER' },
		{ id: 'DOUBLE', name: 'double', labelKey: 'DOUBLE' },
		{ id: 'FLOAT', name: 'float', labelKey: 'FLOAT' },
		{ id: 'BOOLEAN', name: 'boolean', labelKey: 'BOOLEAN' },
		{ id: 'DATE', name: 'date', labelKey: 'DATE' },
	];

	const semanticDomains = [
		{
			"id": "AIRPORT",
			"label": "Airport",
			"frequency": 3.03,
		},
		{
			"id": "CITY",
			"label": "City",
			"frequency": 99.24,
		},
	];

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
			spyOn(StateService, 'setSemanticDomains').and.returnValue();
			spyOn(StateService, 'setPrimitiveTypes').and.returnValue();
			spyOn(ColumnTypesService, 'getColSemanticDomains').and.returnValue($q.when(semanticDomains));
			spyOn(ColumnTypesService, 'getTypes').and.returnValue($q.when(types));
		}));

		describe('with transformation list success', () => {
			beforeEach(inject(($q, TransformationService) => {
				spyOn(TransformationService, 'getTransformations').and.returnValue($q.when(transformationsMock()));
			}));

			it('should filter and init only "column_metadata" category', inject((TransformationService) => {
				//given
				const ctrl = createController();

				//when
				ctrl.initTransformations();
				scope.$digest();

				//then
				expect(TransformationService.getTransformations).toHaveBeenCalledWith('column', column);
				expect(ctrl.transformations.length).toBe(2);
				expect(ctrl.transformations[0].name).toBe('rename');
				expect(ctrl.transformations[1].name).toBe('split');
			}));

			it('should not get transformations if transformations are being fetched', inject((TransformationService) => {
				//given
				let ctrl = createController();
				ctrl.initTransformations();

				//when
				ctrl.initTransformations();

				//then
				expect(TransformationService.getTransformations.calls.count()).toBe(1);
			}));
		});

		describe('with transformation list error', () => {
			beforeEach(inject(($q, TransformationService) => {
				spyOn(TransformationService, 'getTransformations').and.returnValue($q.reject('server error'));
			}));

			it('should change inProgress and error flags', () => {
				//given
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
			});
		});

		describe('fetch semantic domains & primitive types', () => {
			it('should reset semantic domains and types in the state: when preparation', inject((StateService, ColumnTypesService) => {
				// given
				const ctrl = createController();

				// when
				ctrl.initTransformations();

				//then
				expect(StateService.setSemanticDomains).toHaveBeenCalledWith([]);
				expect(StateService.setPrimitiveTypes).toHaveBeenCalledWith([]);
				expect(ColumnTypesService.getColSemanticDomains).toHaveBeenCalledWith('preparation', 'prepId', ctrl.column.id);
				expect(ColumnTypesService.getTypes).toHaveBeenCalled();
			}));

			it('should reset semantic domains and types in the state: when dataset', inject((StateService, ColumnTypesService) => {
				// given
				const ctrl = createController();
				stateMock.playground.preparation = null;
				stateMock.playground.dataset = {
					id: 'datasetId',
				};

				// when
				ctrl.initTransformations();

				//then
				expect(StateService.setSemanticDomains).toHaveBeenCalledWith([]);
				expect(StateService.setPrimitiveTypes).toHaveBeenCalledWith([]);
				expect(ColumnTypesService.getColSemanticDomains).toHaveBeenCalledWith('dataset', 'datasetId', ctrl.column.id);
				expect(ColumnTypesService.getTypes).toHaveBeenCalled();
			}));

			it('should set fetched semantic domains and types in the state', inject(($q, StateService, TransformationService) => {
				// given
				const ctrl = createController();
				const expectedTypes = [
					{ id: 'STRING', name: 'string', labelKey: 'STRING' },
					{ id: 'INTEGER', name: 'integer', labelKey: 'INTEGER' },
					{ id: 'FLOAT', name: 'float', labelKey: 'FLOAT' },
					{ id: 'BOOLEAN', name: 'boolean', labelKey: 'BOOLEAN' },
					{ id: 'DATE', name: 'date', labelKey: 'DATE' },
				];
				spyOn(TransformationService, 'getTransformations').and.returnValue($q.when(transformationsMock()));

				// when
				ctrl.initTransformations();
				scope.$digest();

				//then
				expect(StateService.setSemanticDomains).toHaveBeenCalledWith(semanticDomains.reverse());
				expect(StateService.setPrimitiveTypes).toHaveBeenCalledWith(expectedTypes);
			}));
		});
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
});
