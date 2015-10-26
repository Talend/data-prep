(function() {
	'use strict';
	function FilterListCtrl(){
		var vm = this;

		vm.changeFilter = function changeFilter(filter, value) {
			vm.onFilterChange({
				filter: filter,
				value: value
			});
		};

		vm.removeFilter = function removeFilter(filter) {
			vm.onFilterRemove({
				filter: filter
			});
		};
	}

	angular.module('data-prep.filter-list')
		.controller('FilterListCtrl', FilterListCtrl);
})();