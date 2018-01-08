/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Suggestions state service', () => {
	beforeEach(angular.mock.module('data-prep.services.state'));

	describe('loading flag', () => {
		it('should set loading flag', inject((suggestionsState, SuggestionsStateService) => {
			// given
			suggestionsState.isLoading = false;

			// when
			SuggestionsStateService.setLoading(true);

			// then
			expect(suggestionsState.isLoading).toBe(true);
		}));
	});

	describe('selected tab', () => {
		it('should set loading flag', inject((suggestionsState, SuggestionsStateService) => {
			// given
			suggestionsState.tab = null;

			// when
			SuggestionsStateService.selectTab('column');

			// then
			expect(suggestionsState.tab).toBe(0);
		}));
	});

	describe('transformations', () => {
		it('should init line transformations', inject((suggestionsState) => {
			// then
			expect(suggestionsState.line).toEqual({
				allSuggestions: [],
				allTransformations: [],
				filteredTransformations: [],
				allCategories: null,
				searchActionString: '',
			});
		}));

		it('should set line transformations', inject((suggestionsState, SuggestionsStateService) => {
			// given
			const transformations = {
				allTransformations: [{ name: 'delete' }, { name: 'uppercase' }],
				filteredTransformations: [{ name: 'delete' }],
				allCategories: {
					clean: [{ name: 'delete' }],
					case: [{ name: 'uppercase' }],
				},
			};
			expect(suggestionsState.line).not.toBe(transformations);

			// when
			SuggestionsStateService.setTransformations('line', transformations);

			// then
			expect(suggestionsState.line).toBe(transformations);
		}));

		it('should init column transformations', inject((suggestionsState) => {
			// then
			expect(suggestionsState.column).toEqual({
				allSuggestions: [],
				allTransformations: [],
				filteredTransformations: [],
				allCategories: null,
				searchActionString: '',
			});
		}));

		it('should set column transformations', inject((suggestionsState, SuggestionsStateService) => {
			// given
			const transformations = {
				allTransformations: [{ name: 'delete' }, { name: 'uppercase' }],
				filteredTransformations: [{ name: 'delete' }],
				allSuggestions: [{ name: 'delete' }, { name: 'uppercase' }],
				allCategories: [{}],
				searchActionString: '',
			};
			expect(suggestionsState.column).not.toBe(transformations);

			// when
			SuggestionsStateService.setTransformations('column', transformations);

			// then
			expect(suggestionsState.column).toBe(transformations);
		}));

		it('should update filtered Transformations', inject((suggestionsState, SuggestionsStateService) => {
			// given
			suggestionsState.column.filteredTransformations = [{ name: 'delete' }];
			const filteredTransformations = [{ name: 'delete' }, { name: 'split' }];

			// when
			SuggestionsStateService.updateFilteredTransformations('column', filteredTransformations);

			// then
			expect(suggestionsState.column.filteredTransformations).toBe(filteredTransformations);
		}));

		it('should set transformations for empty cells', inject((suggestionsState, SuggestionsStateService) => {
			const transformations = {
				allTransformations: [
					{ name: 'delete' },
					{ name: 'uppercase', actionScope: ['empty'] },
					{ name: 'lowercase', actionScope: ['empty'] },
				],
			};
			suggestionsState.transformationsForEmptyCells = [];

			// when
			SuggestionsStateService.setTransformations('column', transformations);

			// then
			expect(suggestionsState.transformationsForEmptyCells).toEqual([
				{ name: 'uppercase', actionScope: ['empty'] },
				{ name: 'lowercase', actionScope: ['empty'] },
			]);
		}));

		it('should set transformations for invalid cells', inject((suggestionsState, SuggestionsStateService) => {
			const transformations = {
				allTransformations: [
					{ name: 'delete' },
					{ name: 'uppercase', actionScope: ['invalid'] },
					{ name: 'lowercase', actionScope: ['invalid'] },
				],
			};
			suggestionsState.transformationsForInvalidCells = [];

			// when
			SuggestionsStateService.setTransformations('column', transformations);

			// then
			expect(suggestionsState.transformationsForInvalidCells).toEqual([
				{ name: 'uppercase', actionScope: ['invalid'] },
				{ name: 'lowercase', actionScope: ['invalid'] },
			]);
		}));

		it('should NOT set transformations for invalid/empty cells when they are already set',
			inject((suggestionsState, SuggestionsStateService) => {
				const transformations = {
					allTransformations: [
						{ name: 'delete' },
						{ name: 'uppercase', actionScope: ['invalid', 'empty'] },
						{ name: 'lowercase', actionScope: ['invalid', 'empty'] },
					],
				};
				suggestionsState.transformationsForEmptyCells = [{ name: 'other' }];
				suggestionsState.transformationsForInvalidCells = [{ name: 'other' }];

				// when
				SuggestionsStateService.setTransformations('column', transformations);

				// then
				expect(suggestionsState.transformationsForEmptyCells).toEqual([{ name: 'other' }]);
				expect(suggestionsState.transformationsForInvalidCells).toEqual([{ name: 'other' }]);
			}));
	});

	describe('reset', () => {
		it('should reset loading flag', inject((suggestionsState, SuggestionsStateService) => {
			// given
			suggestionsState.isLoading = true;

			// when
			SuggestionsStateService.reset();

			// then
			expect(suggestionsState.isLoading).toBe(false);
		}));

		it('should reset selected tab', inject((suggestionsState, SuggestionsStateService) => {
			// given
			suggestionsState.tab = 2;

			// when
			SuggestionsStateService.reset();

			// then
			expect(suggestionsState.tab).toBe(null);
		}));

		it('should reset line transformations', inject((suggestionsState, SuggestionsStateService) => {
			// given
			suggestionsState.line = {
				allTransformations: [{ name: 'delete' }, { name: 'uppercase' }],
				filteredTransformations: [{ name: 'delete' }],
				allCategories: {
					clean: [{ name: 'delete' }],
					case: [{ name: 'uppercase' }],
				},
			};

			// when
			SuggestionsStateService.reset();

			// then
			expect(suggestionsState.line).toEqual({
				allSuggestions: [],
				allTransformations: [],
				filteredTransformations: [],
				allCategories: null,
				searchActionString: '',
			});
		}));

		it('should reset dataset transformations', inject((suggestionsState, SuggestionsStateService) => {
			// given
			suggestionsState.dataset = {
				allTransformations: [{ name: 'delete' }, { name: 'uppercase' }],
				filteredTransformations: [{ name: 'delete' }],
				allCategories: {
					clean: [{ name: 'delete' }],
					case: [{ name: 'uppercase' }],
				},
			};

			// when
			SuggestionsStateService.reset();

			// then
			expect(suggestionsState.dataset).toEqual({
				allSuggestions: [],
				allTransformations: [],
				filteredTransformations: [],
				allCategories: null,
				searchActionString: '',
			});
		}));

		it('should reset column transformations', inject((suggestionsState, SuggestionsStateService) => {
			// given
			suggestionsState.column = {
				allSuggestions: [{}],
				allTransformations: [{}],
				filteredTransformations: [{}],
				allCategories: [{}],
				searchActionString: 'ssdsdsd',
			};

			// when
			SuggestionsStateService.reset();

			// then
			expect(suggestionsState.column).toEqual({
				allSuggestions: [],
				allTransformations: [],
				filteredTransformations: [],
				allCategories: null,
				searchActionString: '',
			});
		}));

		it('should NOT reset transformations for invalid cells', inject((suggestionsState, SuggestionsStateService) => {
			// given
			suggestionsState.transformationsForInvalidCells = [{}];

			// when
			SuggestionsStateService.reset();

			// then
			expect(suggestionsState.transformationsForInvalidCells).toEqual([{}]);
		}));

		it('should NOT reset transformations for empty cells', inject((suggestionsState, SuggestionsStateService) => {
			// given
			suggestionsState.transformationsForEmptyCells = [{}];

			// when
			SuggestionsStateService.reset();

			// then
			expect(suggestionsState.transformationsForEmptyCells).toEqual([{}]);
		}));
	});
});
