/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { IGNORED_TYPES } from './column-types-service';

describe('Column types Service', () => {
	const primitiveTypes = [
		{ id: 'ANY', label: 'any', labelKey: 'ANY' },
		{ id: 'STRING', label: 'string', labelKey: 'STRING' },
		{ id: 'NUMERIC', label: 'numeric', labelKey: 'NUMERIC' },
		{ id: 'INTEGER', label: 'integer', labelKey: 'INTEGER' },
		{ id: 'DOUBLE', label: 'double', labelKey: 'DOUBLE' },
		{ id: 'FLOAT', label: 'float', labelKey: 'FLOAT' },
		{ id: 'BOOLEAN', label: 'boolean', labelKey: 'BOOLEAN' },
		{ id: 'DATE', label: 'date', labelKey: 'DATE' },
	];

	const semanticDomains = [
		{ id: 'AIRPORT', label: 'Airport', frequency: 3.03 },
		{ id: 'CITY', label: 'City', frequency: 99.24 },
	];

	let stateMock;

	beforeEach(angular.mock.module('data-prep.services.column-types', ($provide) => {
		stateMock = {
			playground: {
				dataset: { id: 'myDatasetId' },
				grid: {
					semanticDomains: null,
					primitiveTypes: null,
				}
			}
		};

		$provide.constant('state', stateMock);
	}));

	describe('refreshTypes', () => {
		beforeEach(inject(($q, ColumnTypesRestService) => {
			spyOn(ColumnTypesRestService, 'fetchTypes').and.returnValue($q.when(primitiveTypes));
		}));

		it('should NOT get types from backend when it is already initialized',
			inject((ColumnTypesService, ColumnTypesRestService) => {
				// given
				stateMock.playground.grid.primitiveTypes = primitiveTypes;
				
				//when
				ColumnTypesService.refreshTypes();

				//then
				expect(ColumnTypesRestService.fetchTypes).not.toHaveBeenCalled();
			})
		);

		it('should get types from backend',
			inject((ColumnTypesService, ColumnTypesRestService) => {
				//when
				ColumnTypesService.refreshTypes();

				//then
				expect(ColumnTypesRestService.fetchTypes).toHaveBeenCalled();
			})
		);

		it('should filter and set types in app state',
			inject(($rootScope, StateService, ColumnTypesService) => {
				// given
				spyOn(StateService, 'setPrimitiveTypes').and.returnValue();

				//when
				ColumnTypesService.refreshTypes();
				$rootScope.$digest();

				//then
				expect(StateService.setPrimitiveTypes).toHaveBeenCalled();
				const filteredTypes = StateService.setPrimitiveTypes.calls.argsFor(0)[0];
				expect(filteredTypes.length).toBe(5);
				IGNORED_TYPES.forEach((ignored) => {
					expect(filteredTypes.some((type) => type.id === ignored)).toBe(false);
				});
			})
		);
	});

	describe('refreshSemanticDomains', () => {
		beforeEach(inject(($q, StateService, ColumnTypesRestService) => {
			spyOn(StateService, 'setSemanticDomains').and.returnValue();
			spyOn(ColumnTypesRestService, 'fetchDomains').and.returnValue($q.when(semanticDomains));
		}));

		it('should reset semantic domains in app state',
			inject((StateService, ColumnTypesService) => {
				// given
				const colId = 'myColId';
				expect(StateService.setSemanticDomains).not.toHaveBeenCalled();

				//when
				ColumnTypesService.refreshSemanticDomains(colId);

				//then
				expect(StateService.setSemanticDomains).toHaveBeenCalledWith(null);
			})
		);

		it('should fetch semantic domains for dataset',
			inject((ColumnTypesService, ColumnTypesRestService) => {
				// given
				const colId = 'myColId';
				stateMock.playground.preparation = null;

				//when
				ColumnTypesService.refreshSemanticDomains(colId);

				//then
				expect(ColumnTypesRestService.fetchDomains).toHaveBeenCalledWith(
					'dataset',
					'myDatasetId',
					colId
				);
			})
		);

		it('should fetch semantic domains for preparation',
			inject((ColumnTypesService, ColumnTypesRestService) => {
				// given
				const colId = 'myColId';
				stateMock.playground.preparation = { id: 'myPrepId' };

				//when
				ColumnTypesService.refreshSemanticDomains(colId);

				//then
				expect(ColumnTypesRestService.fetchDomains).toHaveBeenCalledWith(
					'preparation',
					'myPrepId',
					colId
				);
			})
		);
		
		it('should sort and set semantic domains into app state',
			inject(($rootScope, StateService, ColumnTypesService) => {
				// given
				const colId = 'myColId';
				expect(StateService.setSemanticDomains).not.toHaveBeenCalled();

				//when
				ColumnTypesService.refreshSemanticDomains(colId);
				$rootScope.$digest();

				//then
				expect(StateService.setSemanticDomains).toHaveBeenCalledWith([
					{
						"id": "CITY",
						"label": "City",
						"frequency": 99.24,
					},
					{
						"id": "AIRPORT",
						"label": "Airport",
						"frequency": 3.03,
					},
				]);
			})
		);
	});
});
