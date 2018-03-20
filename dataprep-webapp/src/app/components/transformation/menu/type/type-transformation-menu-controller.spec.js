/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Type transform menu controller', () => {
	'use strict';

	let createController;
	let scope;
	let currentMetadata = { id: '719b84635c436ef245' };
	let stateMock;

	const primitiveTypes = [
		{ id: 'STRING', label: 'string', labelKey: 'STRING' },
		{ id: 'INTEGER', label: 'integer', labelKey: 'INTEGER' },
		{ id: 'FLOAT', label: 'float', labelKey: 'FLOAT' },
		{ id: 'BOOLEAN', label: 'boolean', labelKey: 'BOOLEAN' },
		{ id: 'DATE', label: 'date', labelKey: 'DATE' },
	];

	const semanticDomains = [
		{ id: 'AIRPORT', label: 'Airport', frequency: 3.03 },
		{ id: 'CITY', label: 'City', frequency: 99.24 },
	];

	beforeEach(angular.mock.module('data-prep.type-transformation-menu', ($provide) => {
		stateMock = {
			playground: {
				preparation: {
					id: 'prepId'
				},
				grid: {
					semanticDomains,
					primitiveTypes,
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($rootScope, $controller, $q, $componentController, state) => {
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
			};
			return ctrl;
		};

		state.playground.dataset = currentMetadata;
	}));

	describe('init', () => {
		it('should update the selected domain/type on column change', () => {
			// given
			const ctrl = createController();
			const nextColumn = {
				id: '0000',
				name: 'awesome cities',
				domain: 'airport',
				domainLabel: 'AIRPORT',
				domainFrequency: 18,
				type: 'string',
			};
			ctrl.column = nextColumn;

			expect(ctrl.currentDomain).toBeFalsy();
			expect(ctrl.currentSimplifiedDomain).toBeFalsy();

			// when
			ctrl.$onChanges({ column: nextColumn });

			// then
			expect(ctrl.currentDomain).toBe('AIRPORT');
			expect(ctrl.currentSimplifiedDomain).toBe('AIRPORT');
		});

		it('should refresh current domain from column domain', () => {
			// given
			const ctrl = createController();

			expect(ctrl.currentDomain).toBeFalsy();
			expect(ctrl.currentSimplifiedDomain).toBeFalsy();

			// when
			ctrl._refreshCurrentDomain();

			// then
			expect(ctrl.currentDomain).toBe('CITY');
			expect(ctrl.currentSimplifiedDomain).toBe('CITY');
		});

		it('should refresh current domain from column type', () => {
			// given
			const ctrl = createController();
			ctrl.column.domain = '';
			ctrl.column.type = 'float';

			expect(ctrl.currentDomain).toBeFalsy();
			expect(ctrl.currentSimplifiedDomain).toBeFalsy();

			// when
			ctrl._refreshCurrentDomain();

			// then
			expect(ctrl.currentDomain).toBe('float');
			expect(ctrl.currentSimplifiedDomain).toBe('decimal');
		});
	});

	describe('changeDomain', () => {
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
			expect(ctrl.currentDomain).toBe('integer');
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

	describe('shouldBeChecked', () => {
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
