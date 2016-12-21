/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Type transform menu controller', function () {
	'use strict';

	let createController;
	let scope;
	let currentMetadata = { id: '719b84635c436ef245' };
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

	beforeEach(angular.mock.module('data-prep.type-transformation-menu', ($provide) => {
		stateMock = {
			playground: {
				preparation: {
					id: 'prepId'
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($rootScope, $controller, $q, $componentController, state, ColumnTypesService) => {
		scope = $rootScope.$new();
		createController = () => {
			const ctrl = $componentController('typeTransformMenu', {
				$scope: scope,
			});
			ctrl.column = {
				id: '0001',
				name: 'awesome cities',
				domain: 'CITY',
				domainLabel: 'CITY',
				domainFrequency: 18,
				type: 'string',
				semanticDomains: [
					{ id: '', label: '', frequency: 15 },
					{ id: 'CITY', label: 'CITY', frequency: 18 },
					{ id: 'REGION', label: 'REGION', frequency: 6 },
					{ id: 'COUNTRY', label: 'COUNTRY', frequency: 17 },
				],
			};
			return ctrl;
		};

		state.playground.dataset = currentMetadata;
		spyOn(ColumnTypesService, 'getTypes').and.returnValue($q.when(types));
		spyOn(ColumnTypesService, 'getColSemanticDomains').and.returnValue($q.when(semanticDomains));
	}));

	describe('init', () => {
		it('should get semantic domains and types on init', inject((ColumnTypesService) => {
			// given
			const ctrl = createController();
			ctrl.column = {
				id: '0000',
				name: 'awesome cities',
				domain: 'CITY',
				domainLabel: 'CITY',
				domainFrequency: 18,
				type: 'string',
				semanticDomains: [
					{ id: '', label: '', frequency: 15 },
					{ id: 'CITY', label: 'CITY', frequency: 18 },
					{ id: 'REGION', label: 'REGION', frequency: 6 },
					{ id: 'COUNTRY', label: 'COUNTRY', frequency: 17 },
				],
			};

			// when
			ctrl.$onInit();
			expect(ColumnTypesService.getColSemanticDomains).not.toHaveBeenCalled();
			scope.$digest();

			// then
			expect(ColumnTypesService.getColSemanticDomains).toHaveBeenCalledWith('preparation', 'prepId', ctrl.column.id);
		}));
	});

	describe('semantic domains', () => {
		it('should get semantic domains of a preparation', inject((ColumnTypesService) => {
			// given
			const ctrl = createController();

			// when
			ctrl.getTypesAndDomains();
			scope.$digest();

			// then
			expect(ColumnTypesService.getColSemanticDomains).toHaveBeenCalledWith('preparation', 'prepId', ctrl.column.id);
			expect(ctrl.semanticDomains).toEqual(semanticDomains.reverse());
		}));

		it('should get semantic domains of a dataset', inject((ColumnTypesService) => {
			// given
			stateMock.playground.preparation = null;
			stateMock.playground.dataset = {
				id: 'datasetId',
			};
			const ctrl = createController();

			// when
			ctrl.getTypesAndDomains();
			scope.$digest();

			// then
			expect(ColumnTypesService.getColSemanticDomains).toHaveBeenCalledWith('dataset', 'datasetId', ctrl.column.id);
		}));

		it('should refresh current domain from column domain', () => {
			// given
			const ctrl = createController();

			// when
			ctrl.getTypesAndDomains();
			scope.$digest();

			// then
			expect(ctrl.currentDomain).toBe('CITY');
			expect(ctrl.currentSimplifiedDomain).toBe('CITY');
		});

		it('should refresh current domain from column type', inject((ConverterService) => {
			// given
			spyOn(ConverterService, 'simplifyType').and.returnValue();
			const ctrl = createController();
			ctrl.column.domain = '';

			// when
			ctrl.getTypesAndDomains();
			scope.$digest();

			// then
			expect(ctrl.currentDomain).toBe('STRING');
			expect(ConverterService.simplifyType).toHaveBeenCalledWith(ctrl.column.type);
		}));
	});

	describe('types', () => {
		it('should get primitive types', inject((ColumnTypesService) => {
			// given
			const expectedTypes = [
				{ id: 'STRING', name: 'string', labelKey: 'STRING' },
				{ id: 'INTEGER', name: 'integer', labelKey: 'INTEGER' },
				{ id: 'FLOAT', name: 'float', labelKey: 'FLOAT' },
				{ id: 'BOOLEAN', name: 'boolean', labelKey: 'BOOLEAN' },
				{ id: 'DATE', name: 'date', labelKey: 'DATE' },
			];
			const ctrl = createController();

			// when
			ctrl.getTypesAndDomains();
			scope.$digest();

			// then
			expect(ColumnTypesService.getTypes).toHaveBeenCalled();
			expect(ctrl.types).toEqual(expectedTypes);
		}));
	});

	describe('changing domain', () => {
		it('should change domain locally and call backend to add a step', inject(($q, PlaygroundService) => {
			//given
			spyOn(PlaygroundService, 'appendStep').and.returnValue($q.when());
			var ctrl = createController();
			var newDomain = {
				id: 'COUNTRY',
				label: 'COUNTRY',
				frequency: 17,
			};

			//when
			ctrl.changeDomain(newDomain);

			//then
			expect(ctrl.column.domain).toBe('COUNTRY');
			expect(ctrl.column.domainLabel).toBe('COUNTRY');
			expect(ctrl.column.domainFrequency).toBe(17);
			expect(ctrl.currentDomain).toBe('COUNTRY');
			expect(ctrl.currentSimplifiedDomain).toBe('COUNTRY');

			const expectedParams = [{
				action: 'domain_change',
				parameters: {
					scope: 'column',
					column_id: '0001',
					column_name: 'awesome cities',
					new_domain_id: 'COUNTRY',
					new_domain_label: 'COUNTRY',
					new_domain_frequency: 17,
				}
			}];
			expect(PlaygroundService.appendStep).toHaveBeenCalledWith(expectedParams);
		}));

		it('should revert domain when backend return error', inject(($q, PlaygroundService) => {
			//given
			spyOn(PlaygroundService, 'appendStep').and.returnValue($q.reject());
			var ctrl = createController();
			var newDomain = {
				id: 'COUNTRY',
				label: 'COUNTRY',
				frequency: 17,
			};

			//when
			ctrl.changeDomain(newDomain);
			scope.$digest();

			//then
			expect(ctrl.column.domain).toBe('CITY');
			expect(ctrl.column.domainLabel).toBe('CITY');
			expect(ctrl.column.domainFrequency).toBe(18);
			expect(ctrl.currentDomain).toBe('CITY');
			expect(ctrl.currentSimplifiedDomain).toBe('CITY');
		}));

		it('should change type and clear domain locally and call backend', inject(($q, PlaygroundService) => {
			//given
			spyOn(PlaygroundService, 'appendStep').and.returnValue($q.when());
			var ctrl = createController();
			var newType = {
				id: 'integer',
			};

			//when
			ctrl.changeType(newType);

			//then
			expect(ctrl.column.type).toBe('integer');
			expect(ctrl.column.domain).toBe('');
			expect(ctrl.column.domainLabel).toBe('');
			expect(ctrl.column.domainFrequency).toBe(0);
			expect(ctrl.currentDomain).toBe('INTEGER');
			expect(ctrl.currentSimplifiedDomain).toBe('integer');

			const expectedParams = [{
				action: 'type_change',
				parameters: {
					scope: 'column',
					column_id: '0001',
					column_name: 'awesome cities',
					new_type: 'integer',
				}
			}];

			expect(PlaygroundService.appendStep).toHaveBeenCalledWith(expectedParams);
		}));

		it('should revert type and domain when backend return error', inject(($q, PlaygroundService) => {
			//given
			spyOn(PlaygroundService, 'appendStep').and.returnValue($q.reject());
			var ctrl = createController();
			var newType = {
				id: 'integer',
			};

			//when
			ctrl.changeType(newType);
			scope.$digest();

			//then
			expect(ctrl.column.type).toBe('string');
			expect(ctrl.column.domain).toBe('CITY');
			expect(ctrl.column.domainLabel).toBe('CITY');
			expect(ctrl.column.domainFrequency).toBe(18);
			expect(ctrl.currentDomain).toBe('CITY');
			expect(ctrl.currentSimplifiedDomain).toBe('CITY');
		}));
	});

	describe('checking the type radio', () => {
		it('should check decimal (float) type when current type is double', () => {
			//given
			var ctrl = createController();
			ctrl.currentDomain = 'double';
			var type = { id: 'FLOAT', name: 'float', labelKey: 'FLOAT' };

			//when
			var result = ctrl.shouldBeChecked(type);

			//then
			expect(result).toBe(true);
		});

		it('should check decimal (float) type when current type is float', () => {
			//given
			var ctrl = createController();
			ctrl.currentDomain = 'float';
			var type = { id: 'FLOAT', name: 'float', labelKey: 'FLOAT' };

			//when
			var result = ctrl.shouldBeChecked(type);

			//then
			expect(result).toBe(true);
		});

		it('should not check type when it does NOT match current domain', () => {
			//given
			var ctrl = createController();
			ctrl.currentDomain = 'beer_name'; // maybe dq library could detect beers names?
			var type = { id: 'STRING', name: 'string', labelKey: 'STRING' };

			//when
			var result = ctrl.shouldBeChecked(type);

			//then
			expect(result).toBe(false);
		});

		it('should check integer type', () => {
			//given
			var ctrl = createController();
			ctrl.currentDomain = 'integer';
			var type = { id: 'INTEGER', name: 'integer', labelKey: 'INTEGER' };

			//when
			var result = ctrl.shouldBeChecked(type);

			//then
			expect(result).toBe(true);
		});
	});
});
