/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import { CTRL_KEY_NAME, SHIFT_KEY_NAME } from './filter-service.js';

import i18n from './../../../i18n/en.json';

describe('Filter service', () => {

	let stateMock;

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(angular.mock.module('data-prep.services.filter-service', ($provide) => {
		const columns = [
			{ id: '0000', name: 'id' },
			{ id: '0001', name: 'name' },
		];

		stateMock = {
			playground: {
				preparation: { id: 'abcd' },
				filter: { gridFilters: [] },
				data: { metadata: { columns: columns } },
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject((StateService, StorageService) => {
		spyOn(StateService, 'addGridFilter').and.returnValue();
		spyOn(StorageService, 'removeFilter').and.returnValue();
	}));

	describe('initFilters', () => {
		//given
		const filter = [{
			type: 'exact',
			colId: 'col2',
			colName: 'column 2',
			args: {
				phrase: [
					{
						value: 'Toto',
					},
				],
			},
            filterFn: () => {},
		}];

		beforeEach(inject(($q, StorageService, FilterService) => {
			spyOn(StorageService, 'getFilter').and.returnValue(filter);
			spyOn(FilterService, 'addFilter').and.returnValue();
		}));

		it('should get the filter from localStorage and add it', inject((StateService, FilterService) => {
			//given
			const preparation = { id: '0000' };
			const dataset = { id: '0000' };

			//when
			FilterService.initFilters(dataset, preparation);

			//then
			expect(FilterService.addFilter).toHaveBeenCalledWith('exact', 'col2', 'column 2', {
				phrase: [
					{
						value: 'Toto',
					},
				],
			}, null, '');
		}));
	});

	describe('add filter', () => {
		describe('with "contains" type', () => {
			it('should create filter', inject((FilterService, StateService) => {
				//given
                const removeFnCallback = () => {};

				expect(StateService.addGridFilter).not.toHaveBeenCalled();

				//when

				FilterService.addFilter('contains', 'col1', 'column name', {
					caseSensitive: true,
					phrase: [
						{
							value: 'toto\n',
						},
					],
				}, removeFnCallback);

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.type).toBe('contains');
				expect(filterInfo.colId).toBe('col1');
				expect(filterInfo.colName).toBe('column name');
				expect(filterInfo.editable).toBe(true);
				expect(filterInfo.args).toEqual({
					caseSensitive: true,
					phrase: [
						{
							label: 'toto\\n',
							value: 'toto\n',
						},
					],
				});
				expect(filterInfo.filterFn()({ col1: ' toto\nest ici' })).toBeTruthy();
				expect(filterInfo.filterFn()({ col1: ' toto est ici' })).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: ' tata est ici' })).toBeFalsy();
				expect(filterInfo.removeFilterFn).toBe(removeFnCallback);
			}));

			it('should create filter with wildcard', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();

				//when
				FilterService.addFilter('contains', 'col1', 'column name', {
					phrase: [
						{
							value: 'to*ici',
						},
					],
				});

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.filterFn()({ col1: ' toto est ici' })).toBeTruthy();
				expect(filterInfo.filterFn()({ col1: ' tata est ici' })).toBeFalsy();
			}));

			it('should remove filter when it already exists', inject((FilterService, StateService) => {
				//given
				const oldFilter = {
					colId: 'col1',
					args: {
						phrase: [
							{
								value: 'toto',
							},
						],
					},
					type: 'contains',
				};
				stateMock.playground.filter.gridFilters = [oldFilter];
				spyOn(StateService, 'removeGridFilter').and.returnValue();

				//when
				FilterService.addFilter('contains', 'col1', 'column name', {
					phrase: [
						{
							value: 'toto',
						},
					],
				}, null);

				//then
				expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
			}));

			it('should update filter when on already exists with a different value', inject((FilterService, StateService) => {
				//given
				const oldFilter = {
					colId: 'col1',
					args: {
						phrase: [
							{
								value: 'tata',
							},
						],
					},
					type: 'contains',
				};
				stateMock.playground.filter.gridFilters = [oldFilter];
				spyOn(StateService, 'updateGridFilter').and.returnValue();

				//when
				FilterService.addFilter('contains', 'col1', 'column name', {
					phrase: [
						{
							value: 'toto',
						},
					],
				}, null);

				//then
				expect(StateService.updateGridFilter).toHaveBeenCalled();
				expect(StateService.updateGridFilter.calls.argsFor(0)[0]).toBe(oldFilter);
				const newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
				expect(newFilter.type).toBe('contains');
				expect(newFilter.colId).toBe('col1');
				expect(newFilter.args).toEqual({
					phrase: [
						{
							label: 'toto',
							value: 'toto',
						},
					],
				});
			}));
		});

		describe('with "exact" type', () => {
			it('should create filter with caseSensitive', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();

				//when
				FilterService.addFilter('exact', 'col1', 'column name', {
					phrase: [
						{
							value: 'toici\n',
						},
					],
					caseSensitive: true,
				});

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.args).toEqual({
					phrase: [
						{
							label: 'toici\\n',
							value: 'toici\n',
						},
					],
					caseSensitive: true,
				});

				expect(filterInfo.filterFn()({ col1: 'toici\n' })).toBeTruthy();
				expect(filterInfo.filterFn()({ col1: 'toici' })).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: 'Toici' })).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: ' toici' })).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: 'toici ' })).toBeFalsy();
			}));

			it('should create filter without caseSensitive', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();

				//when
				FilterService.addFilter('exact', 'col1', 'column name', {
					phrase: [
						{
							value: 'toici',
						},
					],
					caseSensitive: false,
				});

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.filterFn()({ col1: 'Toici' })).toBeTruthy();
				expect(filterInfo.filterFn()({ col1: ' toici' })).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: 'toici ' })).toBeFalsy();
			}));

			it('should remove filter when it already exists', inject((FilterService, StateService) => {
				//given
				const oldFilter = {
					colId: 'col1',
					args: {
						phrase: [
							{
								value: 'toto',
							},
						],
					},
					type: 'exact',
				};
				stateMock.playground.filter.gridFilters = [oldFilter];
				spyOn(StateService, 'removeGridFilter').and.returnValue();

				//when
				FilterService.addFilter('exact', 'col1', 'column name', {
					phrase: [
						{
							value: 'toto',
						},
					],
				}, null);

				//then
				expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
			}));

			it('should update filter when on already exists with a different value with caseSensitive', inject((FilterService, StateService) => {
				//given
				const oldFilter = {
					colId: 'col1',
					args: {
						phrase: [
							{
								value: 'tata',
							},
						],
						caseSensitive: true,
					},
					type: 'exact',
				};
				stateMock.playground.filter.gridFilters = [oldFilter];
				spyOn(StateService, 'updateGridFilter').and.returnValue();

				//when
				FilterService.addFilter('exact', 'col1', 'column name', {
					phrase: [
						{
							value: 'toto',
						},
					],
				}, null);

				//then
				expect(StateService.updateGridFilter).toHaveBeenCalled();
				expect(StateService.updateGridFilter.calls.argsFor(0)[0]).toBe(oldFilter);
				const newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
				expect(newFilter.type).toBe('exact');
				expect(newFilter.colId).toBe('col1');
				expect(newFilter.args).toEqual({
					phrase: [
						{
							label: 'toto',
							value: 'toto',
						},
					],
					caseSensitive: true,
				});
			}));
		});

		describe('with "quality" type', () => {
			it('should create filter for all columns', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();
				const data = {
					metadata: {
						columns: [
							{ id: 'col0' },
							{ id: 'col1' }
						],
					},
				};

				//when
				FilterService.addFilter('quality', undefined, undefined, {
					invalid: true,
					empty: true
				});

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.type).toBe('quality');
				expect(filterInfo.colId).toBe(undefined);
				expect(filterInfo.colName).toBe(undefined);
				expect(filterInfo.value[0].label).toBe('rows with invalid or empty values');
				expect(filterInfo.editable).toBeFalsy();
				expect(filterInfo.args).toEqual({ invalid: true, empty: true });
				expect(filterInfo.filterFn(data)({
					col0: 'test',
					col1: 'NA',
					__tdpInvalid: ['col1']
				})).toBeTruthy();
				expect(filterInfo.filterFn(data)({
					col0: '',
					col1: 'NA',
					__tdpInvalid: ['col0', 'col1']
				})).toBeTruthy();
				expect(filterInfo.filterFn(data)({
					col0: 'test',
					col1: 'NA',
					__tdpInvalid: []
				})).toBeFalsy();
			}));

			it('should remove filter when it already exists', inject((FilterService, StateService) => {
				//given
				const oldFilter = { colId: undefined, type: 'quality' };
				stateMock.playground.filter.gridFilters = [oldFilter];
				spyOn(StateService, 'removeGridFilter').and.returnValue();

				//when
				FilterService.addFilter('quality', undefined, undefined, {
					invalid: true,
					empty: true
				});

				//then
				expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
			}));
		});

		describe('with "invalid records" type', () => {
			it('should create filter', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();
				const data = {
					metadata: {
						columns: [
							{ id: 'col0' },
							{ id: 'col1' }
						],
					},
				};

				//when
				FilterService.addFilter('invalid_records', 'col1', 'column name');

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.type).toBe('invalid_records');
				expect(filterInfo.colId).toBe('col1');
				expect(filterInfo.colName).toBe('column name');
				expect(filterInfo.value[0].label).toBe('rows with invalid values');
				expect(filterInfo.editable).toBe(false);
				expect(filterInfo.args).toBeFalsy();
				expect(filterInfo.filterFn(data)({
					col1: 'NA',
					__tdpInvalid: ['col1']
				})).toBeTruthy();
				expect(filterInfo.filterFn(data)({
					col1: ' tata est ici',
					__tdpInvalid: []
				})).toBeFalsy();
			}));

			it('should create filter for all columns', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();
				const data = {
					metadata: {
						columns: [
							{ id: 'col0' },
							{ id: 'col1' }
						],
					},
				};

				//when
				FilterService.addFilter('invalid_records');

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.type).toBe('invalid_records');
				expect(filterInfo.colId).toBe(undefined);
				expect(filterInfo.colName).toBe(undefined);
				expect(filterInfo.value[0].label).toBe('rows with invalid values');
				expect(filterInfo.editable).toBe(false);
				expect(filterInfo.args).toBeFalsy();
				expect(filterInfo.filterFn(data)({
					col0: 'NA',
					col1: 'NA',
					__tdpInvalid: ['col1']
				})).toBeTruthy();
				expect(filterInfo.filterFn(data)({
					col0: 'NA',
					col1: ' tata est ici',
					__tdpInvalid: ['col0']
				})).toBeTruthy();
				expect(filterInfo.filterFn(data)({
					col0: 'NA',
					col1: ' tata est ici',
					__tdpInvalid: []
				})).toBeFalsy();
			}));

			it('should remove filter when it already exists', inject((FilterService, StateService) => {
				//given
				const oldFilter = { colId: 'col1', type: 'invalid_records' };
				stateMock.playground.filter.gridFilters = [oldFilter];
				spyOn(StateService, 'removeGridFilter').and.returnValue();

				//when
				FilterService.addFilter('invalid_records', 'col1', 'column name');

				//then
				expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
			}));
		});

		describe('with "empty records" type', () => {
			it('should create filter', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();

				//when
				FilterService.addFilter('empty_records', 'col1', 'column name');

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.type).toBe('empty_records');
				expect(filterInfo.colId).toBe('col1');
				expect(filterInfo.colName).toBe('column name');
				expect(filterInfo.value[0].label).toBe('rows with empty values');
				expect(filterInfo.editable).toBe(false);
				expect(filterInfo.args).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: '' })).toBeTruthy();
				expect(filterInfo.filterFn()({ col1: ' tata est ici' })).toBeFalsy();
			}));

			it('should create filter for multi columns', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();
				const data = {
					metadata: {
						columns: [
							{ id: 'col0' },
							{ id: 'col1' }
						],
					},
				};

				//when
				FilterService.addFilter('empty_records');

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.type).toBe('empty_records');
				expect(filterInfo.colId).toBe(undefined);
				expect(filterInfo.colName).toBe(undefined);
				expect(filterInfo.value[0].label).toBe('rows with empty values');
				expect(filterInfo.editable).toBe(false);
				expect(filterInfo.args).toBeFalsy();
				expect(filterInfo.filterFn(data)({ col0: '', col1: '' })).toBeTruthy();
				expect(filterInfo.filterFn(data)({ col0: '', col1: ' tata est ici' })).toBeTruthy();
				expect(filterInfo.filterFn(data)({
					col0: ' toto est ici',
					col1: ' tata est ici'
				})).toBeFalsy();
			}));

			it('should remove filter', inject((FilterService, StateService) => {
				//given
				const oldFilter = { colId: 'col1', type: 'empty_records' };
				stateMock.playground.filter.gridFilters = [oldFilter];
				spyOn(StateService, 'removeGridFilter').and.returnValue();

				//when
				FilterService.addFilter('empty_records', 'col1', 'column name');

				//then
				expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
			}));
		});

		describe('with "valid records" type', () => {
			it('should create filter', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();
				const data = {
					metadata: {
						columns: [
							{ id: 'col0' },
							{ id: 'col1' },
						],
					},
				};

				//when
				FilterService.addFilter('valid_records', 'col1', 'column name');

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.type).toBe('valid_records');
				expect(filterInfo.colId).toBe('col1');
				expect(filterInfo.colName).toBe('column name');
				expect(filterInfo.value[0].label).toBe('rows with valid values');
				expect(filterInfo.editable).toBe(false);
				expect(filterInfo.args).toBeFalsy();
				expect(filterInfo.filterFn(data)({ col1: 'a' })).toBeTruthy();
				expect(filterInfo.filterFn(data)({
					col1: 'm',
					__tdpInvalid: ['col1']
				})).toBeFalsy();
				expect(filterInfo.filterFn(data)({ col1: '' })).toBeFalsy();
			}));

			it('should remove filter', inject((FilterService, StateService) => {
				//given
				const oldFilter = { colId: 'col1', type: 'valid_records' };
				stateMock.playground.filter.gridFilters = [oldFilter];
				spyOn(StateService, 'removeGridFilter').and.returnValue();

				//when
				FilterService.addFilter('valid_records', 'col1', 'column name');

				//then
				expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
			}));
		});

		describe('with "inside range" type', () => {
			it('should create number filter', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();

				//when
				FilterService.addFilter('inside_range', 'col1', 'column name', {
					intervals: [
						{
							label: '[0 .. 22[',
							value: [0, 22],
						},
					],
					type: 'integer',
					isMaxReached: true,
				});
				FilterService.addFilter('inside_range', 'col2', 'column name2', {
					intervals: [
						{
							label: '[0 .. 1,000,000[',
							value: [0, 1000000],
						},
					],
					type: 'integer',
					isMaxReached: false,
				});

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();
				expect(StateService.addGridFilter.calls.count()).toBe(2);

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.type).toBe('inside_range');
				expect(filterInfo.colId).toBe('col1');
				expect(filterInfo.colName).toBe('column name');
				expect(filterInfo.value[0].label).toBe('[0 .. 22[');
				expect(filterInfo.editable).toBe(false);
				expect(filterInfo.args).toEqual({
					intervals: [
						{
							label: '[0 .. 22[',
							value: [0, 22],
						},
					],
					type: 'integer',
					isMaxReached: true,
				});
				expect(filterInfo.filterFn()({ col1: '5' })).toBeTruthy();
				expect(filterInfo.filterFn()({ col1: '-5' })).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: '' })).toBeFalsy();

				const filterInfo2 = StateService.addGridFilter.calls.argsFor(1)[0];
				expect(filterInfo2.type).toBe('inside_range');
				expect(filterInfo2.colId).toBe('col2');
				expect(filterInfo2.colName).toBe('column name2');
				expect(filterInfo2.value).toEqual([
					{
						label: '[0 .. 1,000,000[',
						value: [0, 1000000],
					},
				]);
				expect(filterInfo2.editable).toBe(false);
				expect(filterInfo2.args).toEqual({
					intervals: [
						{
							label: '[0 .. 1,000,000[',
							value: [0, 1000000],
						},
					],
					type: 'integer',
					isMaxReached: false,
				});
				expect(filterInfo2.filterFn()({ col2: '1000' })).toBeTruthy();
				expect(filterInfo2.filterFn()({ col2: '-5' })).toBeFalsy();
				expect(filterInfo2.filterFn()({ col2: '' })).toBeFalsy();
			}));

			it('should create date filter', inject((FilterService, StateService) => {
				//given
				stateMock.playground.grid = {
					columns: [{
						id: 'col1',
						statistics: {
							patternFrequencyTable: [{ pattern: 'yyyy-MM-dd' }],
						},
					}, {
						id: 'col2',
						statistics: {
							patternFrequencyTable: [{ pattern: '9999' }],
						},
					}],
				};

				expect(StateService.addGridFilter).not.toHaveBeenCalled();

				//when
				FilterService.addFilter(
					'inside_range',
					'col1',
					'column name',
					{
						intervals: [
							{
								label: 'Jan 2014',
								value: [
									new Date(2014, 0, 1).getTime(),
									new Date(2014, 1, 1).getTime(),
								],
							},
						],
						type: 'date',
					},
					null
				);

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.type).toBe('inside_range');
				expect(filterInfo.colId).toBe('col1');
				expect(filterInfo.colName).toBe('column name');
				expect(filterInfo.value).toEqual([
					{
						label: 'Jan 2014',
						value: [
							new Date(2014, 0, 1).getTime(),
							new Date(2014, 1, 1).getTime(),
						],
					},
				]);
				expect(filterInfo.editable).toBe(false);
				expect(filterInfo.args).toEqual({
					intervals: [
						{
							label: 'Jan 2014',
							value: [
								new Date(2014, 0, 1).getTime(),
								new Date(2014, 1, 1).getTime(),
							],
						},
					],
					type: 'date',
				});
				expect(filterInfo.filterFn()({ col1: '2014-01-10' })).toBe(true);
				expect(filterInfo.filterFn()({ col1: '2015-12-10' })).toBe(false);
				expect(filterInfo.filterFn()({ col1: 'NA' })).toBe(false);
			}));

			it('should remove filter', inject((FilterService, StateService) => {
				//given
				const oldFilter = {
					colId: 'col1',
					type: 'inside_range',
					args: {
						intervals: [
							{
								value: [0, 22],
							},
						],
					},
				};
				stateMock.playground.filter.gridFilters = [oldFilter];
				spyOn(StateService, 'removeGridFilter').and.returnValue();

				//when
				FilterService.addFilter('inside_range', 'col1', 'column name', {
					intervals: [
						{
							value: [0, 22],
						},
					],
				});

				//then
				expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
			}));
		});

		describe('with "match" type', () => {
			it('should create filter with empty pattern', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();

				//when
				FilterService.addFilter('matches', 'col1', 'column name', {
					patterns: [
						{
							value: '',
						},
					],
				});

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.filterFn()({ col1: '2015 12 o\'clock d 12' })).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: 'Aa9' })).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: '' })).toBeTruthy();
			}));

			it('should create filter with alphanumeric patterns', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();

				//when
				FilterService.addFilter('matches', 'col1', 'column name', {
					patterns: [
						{
							value: 'Aa9/.,',
						},
					],
				});

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.filterFn()({ col1: 'Ha6/.,' })).toBeTruthy();
				expect(filterInfo.filterFn()({ col1: ' ha6/.,' })).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: ' hah/.,' })).toBeFalsy();
			}));

			it('should create filter with date patterns', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();

				//when
				FilterService.addFilter('matches', 'col1', 'column name', {
					patterns: [
						{
							value: 'yyyy-d-M',
						},
					],
				});

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.filterFn()({ col1: '2015-12-12' })).toBeTruthy();
				expect(filterInfo.filterFn()({ col1: '2015-12-13' })).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: '2015/12/13' })).toBeFalsy();
			}));

			it('should create filter with customized date patterns', inject((FilterService, StateService) => {
				//given
				expect(StateService.addGridFilter).not.toHaveBeenCalled();

				//when
				FilterService.addFilter('matches', 'col1', 'column name', {
					patterns: [
						{
							value: 'yyyy d \'o\'\'clock d\' M HH:mm:ss',
						},
					],
				});

				//then
				expect(StateService.addGridFilter).toHaveBeenCalled();

				const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
				expect(filterInfo.filterFn()({ col1: '2015 12 o\'clock d 12 00:00:00' })).toBeTruthy();
				expect(filterInfo.filterFn()({ col1: '2015 12 o\'clock D 12' })).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: '2015 12 12' })).toBeFalsy();
				expect(filterInfo.filterFn()({ col1: '2015 12 \'o\'\'clock\' 13' })).toBeFalsy();
			}));

			it('should remove filter when it already exists', inject((FilterService, StateService) => {
				//given
				const oldFilter = {
					colId: 'col1',
					args: {
						patterns: [
							{
								value: 'Aa',
							},
						],
					},
					type: 'matches',
				};
				stateMock.playground.filter.gridFilters = [oldFilter];
				spyOn(StateService, 'removeGridFilter').and.returnValue();

				//when
				FilterService.addFilter('matches', 'col1', 'column name', {
					patterns: [
						{
							value: 'Aa',
						},
					],
				}, null);

				//then
				expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
			}));

			it('should update filter when it already exists with a different pattern', inject((FilterService, StateService) => {
				//given
				const oldFilter = {
					colId: 'col1',
					args: {
						patterns: [
							{
								value: 'Aa',
							},
						],
					},
					type: 'matches',
				};
				stateMock.playground.filter.gridFilters = [oldFilter];
				spyOn(StateService, 'updateGridFilter').and.returnValue();

				//when
				FilterService.addFilter('matches', 'col1', 'column name', {
					patterns: [
						{
							value: 'Aa9',
						},
					],
				}, null);

				//then
				expect(StateService.updateGridFilter).toHaveBeenCalled();
				expect(StateService.updateGridFilter.calls.argsFor(0)[0]).toBe(oldFilter);
				const newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
				expect(newFilter.type).toBe('matches');
				expect(newFilter.colId).toBe('col1');
				expect(newFilter.args.patterns).toEqual([
					{
						value: 'Aa9',
					},
				]);
			}));
		});

		it('should not throw exception on non existing column (that could be removed by a step) in contains filter', inject((FilterService, StateService) => {
			//given
			expect(StateService.addGridFilter).not.toHaveBeenCalled();

			//when
			FilterService.addFilter('contains', 'col_that_does_not_exist', 'column name', {
				phrase: [
					{
						value: 'toto',
					},
				],
			});

			//then
			expect(StateService.addGridFilter).toHaveBeenCalled();

			const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
			expect(filterInfo.type).toBe('contains');
			expect(filterInfo.filterFn()({ col1: ' toto est ici' })).toBeFalsy();
		}));

		it('should not throw exception on non existing column (that could be removed by a step) in exact filter', inject((FilterService, StateService) => {
			//given
			expect(StateService.addGridFilter).not.toHaveBeenCalled();

			//when
			FilterService.addFilter('exact', 'col_that_does_not_exist', 'column name', {
				phrase: [
					{
						value: 'toto',
					},
				],
			});

			//then
			expect(StateService.addGridFilter).toHaveBeenCalled();

			const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
			expect(filterInfo.type).toBe('exact');
			expect(filterInfo.filterFn()({ col1: ' toto est ici' })).toBeFalsy();
		}));
	});

	describe('remove filter', () => {
		beforeEach(inject((StateService) => {
			spyOn(StateService, 'removeGridFilter').and.returnValue();
			spyOn(StateService, 'removeAllGridFilters').and.returnValue();
		}));

		it('should remove all filters', inject((FilterService, StateService) => {
			//when
			FilterService.removeAllFilters();

			//then
			expect(StateService.removeAllGridFilters).toHaveBeenCalled();
		}));

		it('should call each filter remove callback', inject((FilterService) => {
			//given
			const removeFn1 = jasmine.createSpy('removeFilterCallback');
			const removeFn2 = jasmine.createSpy('removeFilterCallback');
			const filter0 = {};
			const filter1 = { removeFilterFn: removeFn1 };
			const filter2 = { removeFilterFn: removeFn2 };
			const filter3 = {};
			stateMock.playground.filter.gridFilters = [filter0, filter1, filter2, filter3];

			//when
			FilterService.removeAllFilters();

			//then
			expect(removeFn1).toHaveBeenCalled();
			expect(removeFn2).toHaveBeenCalled();
		}));

		it('should remove filter', inject((FilterService, StateService) => {
			//given
			const filter = {};

			//when
			FilterService.removeFilter(filter);

			//then
			expect(StateService.removeGridFilter).toHaveBeenCalledWith(filter);
		}));

		it('should call filter remove callback', inject((FilterService) => {
			//given
			const removeFn = jasmine.createSpy('removeFilterCallback');
			const filter = { removeFilterFn: removeFn };

			//when
			FilterService.removeFilter(filter);

			//then
			expect(removeFn).toHaveBeenCalled();
		}));
	});

	describe('update filter', () => {
		beforeEach(inject((StateService) => {
			spyOn(StateService, 'updateGridFilter').and.returnValue();
		}));

		it('should update "contains" filter', inject((FilterService, StateService) => {
			//given
			const oldFilter = {
				type: 'contains',
				colId: 'col2',
				colName: 'column 2',
				args: {
					phrase: [
						{
							value: 'Tata',
						},
					],
				},
                filterFn: () => {},
			};
			expect(StateService.updateGridFilter).not.toHaveBeenCalled();

			//when
			FilterService.updateFilter(oldFilter, [
				{
					value: 'Tata\\n',
				},
			]);

			//then
			const argsOldFilter = StateService.updateGridFilter.calls.argsFor(0)[0];
			expect(argsOldFilter).toBe(oldFilter);

			const newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
			expect(newFilter).not.toBe(oldFilter);
			expect(newFilter.type).toBe('contains');
			expect(newFilter.colId).toBe('col2');
			expect(newFilter.colName).toBe('column 2');
			expect(newFilter.args.phrase).toEqual([
				{
					value: 'Tata\\n',
				},
			]);

			expect(newFilter.filterFn()({ col2: ' Tata\\n est ici' })).toBeTruthy();
			expect(newFilter.filterFn()({ col2: ' Tata\n est ici' })).toBeFalsy();
		}));

		it('should update "exact" filter', inject((FilterService, StateService) => {
			//given
			const oldFilter = {
				type: 'exact',
				colId: 'col2',
				colName: 'column 2',
				args: {
					phrase: [
						{
							value: 'Toto',
						},
					],
				},
                filterFn: () => {},
			};

			expect(StateService.updateGridFilter).not.toHaveBeenCalled();

			//when
			FilterService.updateFilter(oldFilter, [
				{
					value: 'Tata\\n',
				},
			]);

			//then
			const argsOldFilter = StateService.updateGridFilter.calls.argsFor(0)[0];
			expect(argsOldFilter).toBe(oldFilter);

			const newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
			expect(newFilter).not.toBe(oldFilter);
			expect(newFilter.type).toBe('exact');
			expect(newFilter.colId).toBe('col2');
			expect(newFilter.colName).toBe('column 2');
			expect(newFilter.args.phrase).toEqual([
				{
					value: 'Tata\\n',
				},
			]);
			expect(newFilter.value).toEqual([
				{
					value: 'Tata\\n',
				},
			]);

			expect(newFilter.filterFn()({ col2: 'Tata\\n' })).toBeTruthy();
			expect(newFilter.filterFn()({ col2: 'Tata\n' })).toBeFalsy();
		}));

		it('should update "inside_range" filter after a brush', inject((FilterService, StateService) => {
			//given
			const oldFilter = {
				type: 'inside_range',
				colId: 'col1',
				colName: 'column 1',
				args: {
					intervals: [
						{
							label: '[5 .. 10[',
							value: [5, 10],
						},
					],
					type: 'integer',
				},
                filterFn: () => {},
			};

			expect(StateService.updateGridFilter).not.toHaveBeenCalled();

			//when
			FilterService.updateFilter(oldFilter, [
				{
					value: [0, 22],
					label: '[0 .. 22[',
				},
			]);

			//then
			const argsOldFilter = StateService.updateGridFilter.calls.argsFor(0)[0];
			expect(argsOldFilter).toBe(oldFilter);

			const newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
			expect(newFilter).not.toBe(oldFilter);
			expect(newFilter.type).toBe('inside_range');
			expect(newFilter.colId).toBe('col1');
			expect(newFilter.colName).toBe('column 1');
			expect(newFilter.args).toEqual({
				intervals: [
					{
						value: [0, 22],
						label: '[0 .. 22[',
					},
				],
				type: 'integer',
			});
			expect(newFilter.value).toEqual([
				{
					value: [0, 22],
					label: '[0 .. 22[',
				},
			]);
		}));

		it('should update "inside_range" filter for date column', inject((FilterService, StateService) => {
			//given
			const oldFilter = {
				type: 'inside_range',
				colId: 'col1',
				colName: 'column 1',
				args: {
					intervals: [
						{
							label: 'Jan 2014',
							value: [
								new Date(2014, 0, 1).getTime(),
								new Date(2014, 1, 1).getTime(),
							],
						},
					],
					type: 'date',
				},
                filterFn: () => {},
			};

			stateMock.playground.grid = {
				columns: [{
					id: 'col1',
					statistics: {
						patternFrequencyTable: [{ pattern: 'yyyy-MM-dd' }],
					},
				}, {
					id: 'col2',
					statistics: {
						patternFrequencyTable: [{ pattern: '9999' }],
					},
				}],
			};

			expect(StateService.updateGridFilter).not.toHaveBeenCalled();

			//when
			FilterService.updateFilter(oldFilter, [
				{
					label: 'Mar 2015',
					value: [
						new Date(2015, 2, 1).getTime(),
						new Date(2015, 3, 1).getTime(),
					],
				},
			]);

			//then
			const argsOldFilter = StateService.updateGridFilter.calls.argsFor(0)[0];
			expect(argsOldFilter).toBe(oldFilter);

			const newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
			expect(newFilter).not.toBe(oldFilter);
			expect(newFilter.type).toBe('inside_range');
			expect(newFilter.colId).toBe('col1');
			expect(newFilter.colName).toBe('column 1');
			expect(newFilter.args).toEqual({
				intervals: [
					{
						label: 'Mar 2015',
						value: [
							new Date(2015, 2, 1).getTime(),
							new Date(2015, 3, 1).getTime(),
						],
					},
				],
				type: 'date',
			});
			expect(newFilter.value).toEqual([
				{
					label: 'Mar 2015',
					value: [
						new Date(2015, 2, 1).getTime(),
						new Date(2015, 3, 1).getTime(),
					],
				},
			]);
		}));

		it('should update "inside range" filter when adding an existing range filter', inject((FilterService, StateService) => {
			//given
            const removeCallback = () => {};

			FilterService.addFilter('inside_range', 'col1', 'column name', {
				intervals: [
					{
						label: '[0 .. 22[',
						value: [0, 22],
					},
				],
				type: 'integer',
			}, removeCallback);

			expect(StateService.updateGridFilter).not.toHaveBeenCalled();
			expect(StateService.addGridFilter.calls.count()).toBe(1);
			const filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
			expect(filterInfo.value).toEqual([
				{
					label: '[0 .. 22[',
					value: [0, 22],
				},
			]);

			expect(filterInfo.filterFn({ col1: '4' })).toBeTruthy();
			stateMock.playground.filter.gridFilters = [filterInfo];

			//when
			FilterService.addFilter('inside_range', 'col1', 'column name', {
				intervals: [
					{
						label: '[5 .. 10[',
						value: [5, 10],
						isMaxReached: true,
					},
				],
				type: 'integer',
			});

			//then
			expect(StateService.updateGridFilter).toHaveBeenCalled();
			expect(StateService.addGridFilter.calls.count()).toBe(1);

			const oldFilterInfo = StateService.updateGridFilter.calls.argsFor(0)[1];
			expect(oldFilterInfo).not.toBe(filterInfo);

			const newFilterInfos = StateService.updateGridFilter.calls.argsFor(0)[1];
			expect(newFilterInfos.type).toBe('inside_range');
			expect(newFilterInfos.colId).toBe('col1');
			expect(newFilterInfos.colName).toBe('column name');
			expect(newFilterInfos.value).toEqual([
				{
					label: '[5 .. 10[',
					value: [5, 10],
					isMaxReached: true,
				},
			]);
			expect(newFilterInfos.editable).toBe(false);
			expect(newFilterInfos.args).toEqual({
				intervals: [
					{
						label: '[5 .. 10[',
						value: [5, 10],
						isMaxReached: true,
					},
				],
				type: 'integer',
			});
			expect(newFilterInfos.filterFn()({ col1: '8' })).toBeTruthy();
			//the 4 is no more inside the brush range
			expect(newFilterInfos.filterFn()({ col1: '4' })).toBeFalsy();
			expect(newFilterInfos.removeFilterFn).toBe(removeCallback);
		}));

		it('should update exact filter while several values are selected', inject((FilterService, StateService) => {
			//given
			const oldFilter = {
				type: 'exact',
				colId: 'col2',
				colName: 'column 2',
				args: {
					phrase: [
						{
							value: 'Toto',
						},
					],
				},
                filterFn: () => {},
			};

			//when
			FilterService.updateFilter(oldFilter, [
				{
					value: 'Tata',
				},
			], CTRL_KEY_NAME);

			//then
			const newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
			expect(newFilter).not.toBe(oldFilter);
			expect(newFilter.args.phrase).toEqual([
				{ value: 'Toto' },
				{ value: 'Tata' },
			]);

			expect(newFilter.filterFn()({ col2: 'Toto' })).toBeTruthy();
			expect(newFilter.filterFn()({ col2: 'Tata' })).toBeTruthy();
			expect(newFilter.filterFn()({ col2: 'Titi' })).toBeFalsy();
		}));

		it('should update range filter while several values are selected', inject((FilterService, StateService) => {
			//given
			const oldFilter = {
				type: 'inside_range',
				colId: 'col1',
				colName: 'column 1',
				args: {
					intervals: [
						{
							label: 'Jan 2014',
							value: [
								new Date(2014, 0, 1).getTime(),
								new Date(2014, 1, 1).getTime(),
							],
						},
					],
					type: 'date',
				},
                filterFn: () => {},
			};

			stateMock.playground.grid = {
				columns: [{
					id: 'col1',
					statistics: {
						patternFrequencyTable: [{ pattern: 'yyyy-MM-dd' }],
					},
				}, {
					id: 'col2',
					statistics: {
						patternFrequencyTable: [{ pattern: '9999' }],
					},
				}],
			};

			//when
			FilterService.updateFilter(oldFilter, [
				{
					label: 'Feb 2014',
					value: [
						new Date(2014, 1, 1).getTime(),
						new Date(2014, 2, 1).getTime(),
					],
				},
			], CTRL_KEY_NAME);

			//then
			const newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
			expect(newFilter).not.toBe(oldFilter);
			expect(newFilter.args).toEqual({
				intervals: [
					{
						label: 'Jan 2014',
						value: [
							new Date(2014, 0, 1).getTime(),
							new Date(2014, 1, 1).getTime(),
						],
					},
					{
						label: 'Feb 2014',
						value: [
							new Date(2014, 1, 1).getTime(),
							new Date(2014, 2, 1).getTime(),
						],
					},
				],
				type: 'date',
			});

			expect(newFilter.filterFn()({ col1: '2014-01-01' })).toBeTruthy();
			expect(newFilter.filterFn()({ col1: '2014-02-01' })).toBeTruthy();
			expect(newFilter.filterFn()({ col1: '2014-03-01' })).toBeFalsy();
		}));

		it('should update range filter while from-to values are selected', inject((FilterService, StateService) => {
			//given
			const oldFilter = {
				type: 'inside_range',
				colId: 'col1',
				colName: 'column 1',
				args: {
					intervals: [
						{
							label: 'Jan 2014',
							value: [
								new Date(2014, 0, 1).getTime(),
								new Date(2014, 1, 1).getTime(),
							],
						},
					],
					type: 'date',
				},
                filterFn: () => {},
			};

			stateMock.playground.grid = {
				columns: [{
					id: 'col1',
					statistics: {
						patternFrequencyTable: [{ pattern: 'yyyy-MM-dd' }],
					},
				}, {
					id: 'col2',
					statistics: {
						patternFrequencyTable: [{ pattern: '9999' }],
					},
				}],
			};

			//when
			FilterService.updateFilter(oldFilter, [
				{
					label: 'Apr 2014',
					value: [
						new Date(2014, 3, 1).getTime(),
						new Date(2014, 4, 1).getTime(),
					],
				},
			], SHIFT_KEY_NAME);

			//then
			const newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
			expect(newFilter).not.toBe(oldFilter);
			expect(newFilter.value).toEqual(
				[
					{
						label: '[Jan 2014 .. Apr 2014[',
						value: [
							new Date(2014, 0, 1).getTime(),
							new Date(2014, 4, 1).getTime(),
						],
					},
				]
			);
			expect(newFilter.filterFn()({ col1: '2014-02-01' })).toBeTruthy();
			expect(newFilter.filterFn()({ col1: '2014-03-01' })).toBeTruthy();
			expect(newFilter.filterFn()({ col1: '2014-05-01' })).toBeFalsy();
		}));
	});

	describe('get splitted range label', () => {
		it('should isolate range values', inject((FilterService) => {
			//given
			const labels = [
				{ input: '', output: [''] },
				{ input: '[]', output: [''] },
				{ input: '[10]', output: ['10'] },
				{ input: '[0,10]', output: ['0', '10'] },
				{ input: '[0,10[', output: ['0', '10'] },
				{ input: '[0 .. 10]', output: ['0', '10'] },
				{ input: '[0 .. 10[', output: ['0', '10'] },
				{ input: '[Jan 2016,Jan 2017]', output: ['Jan 2016', 'Jan 2017'] },
				{ input: '[Jan 2016 .. Jan 2017[', output: ['Jan 2016', 'Jan 2017'] },
			];

			//when
			const fn = FilterService._getSplittedRangeLabelFor;

			//then
			labels.forEach(label => expect(fn.call(FilterService, label.input)).toEqual(label.output));
		}));
	});
});
