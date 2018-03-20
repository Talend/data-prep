/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DataViewMock from '../../../../mocks/DataView.mock';

describe('Lookup state service', () => {
	'use strict';

	const data = {
		metadata: {
			columns: [
				{ id: '0000', name: 'identif' },
				{ id: '0001', name: 'code' },
				{ id: '0002', name: 'firstname' },
				{ id: '0003', name: 'lastname' },
			],
		},
		records: [
			{ tdpId: 0, firstname: 'Tata' },
			{ tdpId: 1, firstname: 'Tetggggge' },
			{ tdpId: 2, firstname: 'Titi' },
			{ tdpId: 3, firstname: 'Toto' },
			{ tdpId: 4, name: 'AMC Gremlin' },
			{ tdpId: 5, firstname: 'Tyty' },
			{ tdpId: 6, firstname: 'Papa' },
			{ tdpId: 7, firstname: 'Pepe' },
			{ tdpId: 8, firstname: 'Pipi' },
			{ tdpId: 9, firstname: 'Popo' },
			{ tdpId: 10, firstname: 'Pupu' },
			{ tdpId: 11, firstname: 'Pypy' },
		],
	};

	const columnCheckboxesWithSelection = [
		{ id: '0000', name: 'identif', isAdded: false },
		{ id: '0001', name: 'code', isAdded: false },
		{ id: '0002', name: 'firstname', isAdded: true },
		{ id: '0003', name: 'lastname', isAdded: true },
	];

	const actions = [
		{
			category: 'data_blending',
			name: 'lookup',
			parameters: [
				{
					name: 'column_id',
					type: 'string',
					default: '',
				},
				{
					name: 'filter',
					type: 'filter',
					default: '',
				},
				{
					name: 'lookup_ds_name',
					type: 'string',
					default: 'lookup_2',
				},
				{
					name: 'lookup_ds_id',
					type: 'string',
					default: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
				},
				{
					name: 'lookup_ds_url',
					type: 'string',
					default: 'http://172.17.0.211:8080/datasets/9ee2eac7/content?metadata=true',
				},
				{
					name: 'lookup_join_on',
					type: 'string',
					default: '',
				},
				{
					name: 'lookup_join_on_name',
					type: 'string',
					default: '',
				},
				{
					name: 'lookup_selected_cols',
					type: 'list',
					default: '',
				},
			],
		},
	];

	beforeEach(angular.mock.module('data-prep.services.state'));

	beforeEach(inject((lookupState) => {
		lookupState.dataView = new DataViewMock();
	}));

	describe('visibility', () => {
		it('should set visibility flag', inject((lookupState, LookupStateService) => {
			//given
			lookupState.visibility = false;

			//when
			LookupStateService.setVisibility(true);

			//then
			expect(lookupState.visibility).toBe(true);
		}));
	});

	describe('reset', () => {
		it('should reset state', inject((lookupState, LookupStateService) => {
			// given
			lookupState.actions = [{}, {}];
			lookupState.columnsToAdd = ['0000'];
			lookupState.columnCheckboxes = [{ id: '0001', isAdded: true, name: 'vfvf' }];
			lookupState.dataset = {};
			lookupState.data = {};
			lookupState.selectedColumn = '0001';
			lookupState.visibility = true;
			lookupState.searchDatasetString = 'charles';
			lookupState.showTooltip = true;
			lookupState.tooltip = { 'htmlstr': '' };
			lookupState.tooltipRuler = {};

			// when
			LookupStateService.reset();

			// then
			expect(lookupState.actions).toEqual([]);
			expect(lookupState.columnsToAdd).toEqual([]);
			expect(lookupState.columnCheckboxes).toEqual([]);
			expect(lookupState.dataset).toBe(null);
			expect(lookupState.data).toBe(null);
			expect(lookupState.selectedColumn).toBe(null);
			expect(lookupState.visibility).toBe(false);
			expect(lookupState.searchDatasetString).toBe('');
			expect(lookupState.showTooltip).toEqual(false);
			expect(lookupState.tooltip).toEqual({});
			expect(lookupState.tooltipRuler).toEqual(null);
		}));
	});

	describe('init actions', () => {
		it('should set actions', inject((lookupState, LookupStateService) => {
			// given
			lookupState.actions = [];

			// when
			LookupStateService.setActions(actions);

			// then
			expect(lookupState.actions).toBe(actions);
		}));
	});

	describe('parameters update', () => {
		it('should update the columns to add corresponding to the checkboxes model',
			inject((lookupState, LookupStateService) => {
				// given
				lookupState.columnCheckboxes = columnCheckboxesWithSelection;
				lookupState.columnsToAdd = null;
				lookupState.selectedColumn = {
					id: '0000',
				};

				// when
				LookupStateService.updateColumnsToAdd();

				// then
				expect(lookupState.columnsToAdd).toEqual([
					{ id: '0002', name: 'firstname' },
					{ id: '0003', name: 'lastname' },
				]);
			}));

		it('should set selected column', inject((lookupState, LookupStateService) => {
			//given
			lookupState.selectedColumn = null;
			const selectedColumn = { id: '0001' };

			//when
			LookupStateService.setSelectedColumn(selectedColumn);

			//then
			expect(lookupState.selectedColumn).toBe(selectedColumn);
		}));

		it('should update the columns to add on new column selection', inject((lookupState, LookupStateService) => {
			//given
			lookupState.columnCheckboxes = columnCheckboxesWithSelection;
			const selectedColumn = { id: '0001' };

			//when
			LookupStateService.setSelectedColumn(selectedColumn);

			//then
			expect(lookupState.columnsToAdd).toEqual([
				{ id: '0002', name: 'firstname' },
				{ id: '0003', name: 'lastname' },]
			);
		}));

		it('should NOT update the columns to add when there is no selected column', inject((lookupState, LookupStateService) => {
			//given
			const addedCols = ['0018'];
			lookupState.columnsToAdd = addedCols;
			lookupState.columnCheckboxes = columnCheckboxesWithSelection;

			//when
			LookupStateService.setSelectedColumn(null);

			//then
			expect(lookupState.columnsToAdd).toBe(addedCols);
		}));
	});

	describe('sort', () => {
		it('should sort lookup step when updating the sort type', inject((lookupState, LookupStateService) => {
			//given
			lookupState.datasets = [
				{
					model: {
						id: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
						addedToLookup: true,
						created: 80
					}
				},
				{ model: { id: '3', addedToLookup: false, created: 90 } },
				{ model: { id: '2', addedToLookup: false, created: 100 } },
			];

			lookupState.sort = { id: 'name', name: 'NAME_SORT', property: 'name' };
			lookupState.order = { id: 'desc', name: 'DESC_ORDER' };


			//when
			LookupStateService.setSort({ id: 'date', name: 'DATE_SORT', property: 'created' });

			//then
			expect(lookupState.datasets[0].model.created).toBe(100);
			expect(lookupState.datasets[1].model.created).toBe(90);
			expect(lookupState.datasets[2].model.created).toBe(80);
		}));

		it('should sort lookup step when updating the order type', inject((lookupState, LookupStateService) => {
			//given
			lookupState.datasets = [
				{ model: { id: '2', addedToLookup: false, created: 100 } },
				{ model: { id: '3', addedToLookup: false, created: 90 } },
				{
					model: {
						id: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
						addedToLookup: true,
						created: 80
					}
				},
			];

			lookupState.sort = { id: 'date', name: 'DATE_SORT', property: 'created' };
			lookupState.order = { id: 'desc', name: 'DESC_ORDER' };


			//when
			LookupStateService.setOrder({ id: 'asc', name: 'ASC_ORDER' });

			//then
			expect(lookupState.datasets[0].model.created).toBe(80);
			expect(lookupState.datasets[1].model.created).toBe(90);
			expect(lookupState.datasets[2].model.created).toBe(100);
		}));

		it('should sort lookup step when updating the sort type in case of String', inject((lookupState, LookupStateService) => {
			//given
			lookupState.datasets = [
				{ model: { id: '3', addedToLookup: false, created: 'b' } },
				{
					model: {
						id: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
						addedToLookup: true,
						created: 'a'
					}
				},
				{ model: { id: '2', addedToLookup: false, created: 'C' } },
			];

			lookupState.sort = { id: 'name', name: 'NAME_SORT', property: 'name' };
			lookupState.order = { id: 'desc', name: 'DESC_ORDER' };


			//when
			LookupStateService.setSort({ id: 'date', name: 'DATE_SORT', property: 'created' });

			//then
			expect(lookupState.datasets[0].model.created).toBe('C');
			expect(lookupState.datasets[1].model.created).toBe('b');
			expect(lookupState.datasets[2].model.created).toBe('a');
		}));

		it('should sort lookup step when updating the order type in case of String', inject((lookupState, LookupStateService) => {
			//given
			lookupState.datasets = [
				{ model: { id: '2', addedToLookup: false, created: 'C' } },
				{ model: { id: '3', addedToLookup: false, created: 'b' } },
				{
					model: {
						id: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
						addedToLookup: true,
						created: 'a'
					}
				},
			];

			lookupState.sort = { id: 'date', name: 'DATE_SORT', property: 'created' };
			lookupState.order = { id: 'desc', name: 'DESC_ORDER' };


			//when
			LookupStateService.setOrder({ id: 'asc', name: 'ASC_ORDER' });

			//then
			expect(lookupState.datasets[0].model.created).toBe('a');
			expect(lookupState.datasets[1].model.created).toBe('b');
			expect(lookupState.datasets[2].model.created).toBe('C');
		}));
	});
});
