/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.filter-search.controller:FilterSearchCtrl
 * @description Filter search controller.
 */
export default function FilterSearchCtrl($translate, FilterManagerService, DatagridService) {
	'ngInject';

	const vm = this;

    /**
     * @ngdoc method
     * @name createSuggestionItem
     * @methodOf data-prep.filter-search.controller:FilterSearchCtrl
     * @param {string} term - the filter phrase
     * @description [PRIVATE] Return a closure function that create a suggestion item from column id
     * @returns {function} the item creation closure
     */
	function createSuggestionItem(term) {
		return (col) => {
			return {
				label: `${term} ${$translate.instant('IN')} <b>${col.name}</b>`,
				value: term,
				columnId: col.id,
				columnName: col.name,
			};
		};
	}

    /**
     * @ngdoc method
     * @name filterSuggestion
     * @methodOf data-prep.filter-search.controller:FilterSearchCtrl
     * @param {string} term - the searched term
     * @description [PRIVATE] Create filter suggestions based on the typed term
     * @returns {function[]} the suggestion list
     */
	function filterSuggestion(term) {
		const cleanTerm = term.toLowerCase().trim();
		const colContainingTerm = DatagridService.getColumnsContaining(cleanTerm);

		return _.chain(colContainingTerm)
            .sortBy(col => col.name.toLowerCase())
            .map(createSuggestionItem(cleanTerm))
            .value();
	}

    /**
     * @ngdoc method
     * @name suggestionSelect
     * @methodOf data-prep.filter-search.controller:FilterSearchCtrl
     * @param {object} item - the filter infos
     * @description Action when user select a suggestion:  create the filter and reset the input
     */
	function suggestionSelect(item) {
		FilterManagerService.addFilter('contains', item.columnId, item.columnName, {
			phrase: [{ value: item.value }],
		});
		vm.filterSearch = '';
	}

    /**
     * @ngdoc property
     * @name filterSuggestOptions
     * @propertyOf data-prep.filter-search.controller:FilterSearchCtrl
     * @description Mass auto complete suggestions options and callbacks
     */
	vm.filterSuggestOptions = {
		suggest: filterSuggestion,
		on_select: suggestionSelect,
	};
}
