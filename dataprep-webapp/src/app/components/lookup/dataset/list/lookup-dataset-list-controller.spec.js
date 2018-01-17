/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Lookup Dataset list controller', () => {
	let createController;
	let scope;

	const datasets = [
		{ model: { id: 'ec4834d9bc2af8', name: 'Customers (50 lines)' } },
		{ model: { id: 'ab45f893d8e923', name: 'Us states' } },
		{ model: { id: 'cf98d83dcb9437', name: 'Customers (1K lines)' } },
	];

	beforeEach(angular.mock.module('data-prep.lookup'));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();

		createController = () => $componentController('lookupDatasetList', { $scope: scope });
	}));

	it('should toogle the selection of lookup dataset', () => {
		//given
		var dataset = { enableToAddToLookup: true, addedToLookup: true };
		var ctrl = createController();

		//when
		ctrl.toogleSelect(dataset);

		//then
		expect(dataset.addedToLookup).toBe(false);
	});

	it('should not toogle the selection of the lookup dataset if dataset is disabled', () => {
		//given
		var dataset = { enableToAddToLookup: false, addedToLookup: true };
		var ctrl = createController();

		//when
		ctrl.toogleSelect(dataset);

		//then
		expect(dataset.addedToLookup).toBe(true);
	});

	it('should not filter when search-text is empty', () => {
		//given
		var ctrl = createController();
		ctrl.searchText = '';
		ctrl.datasets = datasets;

		//when
		ctrl.filterDatasets();

		//then
		expect(ctrl.filteredDatasets.length).toBe(3);
	});

	it('should filter datasets', () => {
		//given
		var ctrl = createController();
		ctrl.searchText = 'Us states';
		ctrl.datasets = datasets;

		//when
		ctrl.filterDatasets();

		//then
		expect(ctrl.filteredDatasets.length).toBe(1);
		expect(ctrl.filteredDatasets[0]).toBe(datasets[1]);
	});

	it('should call filterDatasets on $onChanges', () => {
		//given
		var ctrl = createController();
		spyOn(ctrl, 'filterDatasets').and.returnValue();

		//when
		ctrl.$onChanges();

		//then
		expect(ctrl.filterDatasets).toHaveBeenCalled();
	});
});
