describe('Lookup Datagrid controller', function () {
	'use strict';

	var createController, scope;

	beforeEach(module('data-prep.lookup'));

	beforeEach(inject(function($rootScope, $controller) {
		scope = $rootScope.$new();

		createController = function () {
			var ctrl = $controller('LookupDatagridCtrl', {
				$scope: scope
			});
			return ctrl;
		};
	}));

	it('should bind tooltip getter to LookupDatagridTooltipService', inject(function(LookupDatagridTooltipService) {
		//given
		var newTooltip = {colId: '0000'};

		var ctrl = createController();
		expect(ctrl.tooltip).toEqual({});

		//when
		LookupDatagridTooltipService.tooltip = newTooltip;

		//then
		expect(ctrl.tooltip).toEqual(newTooltip);
	}));

	it('should bind showTooltip getter to LookupDatagridTooltipService', inject(function(LookupDatagridTooltipService) {
		//given
		var ctrl = createController();
		expect(ctrl.showTooltip).toEqual(false);

		//when
		LookupDatagridTooltipService.showTooltip = true;

		//then
		expect(ctrl.showTooltip).toEqual(true);
	}));
});
