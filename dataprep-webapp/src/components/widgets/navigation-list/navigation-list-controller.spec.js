describe('Navigation List controller', function () {
	'use strict';

	var createController, scope;
	var listToNavigate = [{}, {}];

	beforeEach(module('talend.widget'));

	beforeEach(inject(function ($rootScope, $controller) {
		scope = $rootScope.$new();

		createController = function () {
			return $controller('NavigationListCtrl', {
				$scope: scope
			});
		};
	}));

	it('should decrement borders', inject(function () {
		//given
		var ctrl = createController();
		ctrl.lastLabelIndex = 4;

		//when
		ctrl.showBack();

		//then
		expect(ctrl.firstLabelIndex).toBe(-1);
		expect(ctrl.lastLabelIndex).toBe(3);
	}));

	it('should increment borders', inject(function () {
		//given
		var ctrl      = createController();
		ctrl.lastLabelIndex = 4;

		//when
		ctrl.showForth();

		//then
		expect(ctrl.firstLabelIndex).toBe(1);
		expect(ctrl.lastLabelIndex).toBe(5);
	}));

	it('should reset navigation list limits', inject(function () {
		//given
		var ctrl = createController();
		ctrl.nbreLabelsToShow = '4';

		ctrl.firstLabelIndex = 3;
		ctrl.lastLabelIndex = 8;

		//when
		ctrl.list = listToNavigate;
		scope.$digest();

		//then
		expect(ctrl.firstLabelIndex).toBe(0);
		expect(ctrl.lastLabelIndex).toBe(4);
	}));

	it('should not reset navigation list limits', inject(function () {
		//given
		var ctrl = createController();
		ctrl.list = listToNavigate;
		ctrl.nbreLabelsToShow = 4;
		ctrl.firstLabelIndex = 3;
		ctrl.lastLabelIndex = 8;
		scope.$digest();
		expect(ctrl.firstLabelIndex).toBe(0);
		expect(ctrl.lastLabelIndex).toBe(4);

		//when
		ctrl.firstLabelIndex = 3;
		ctrl.lastLabelIndex = 8;
		ctrl.list = listToNavigate;
		scope.$digest();

		//then
		expect(ctrl.firstLabelIndex).toBe(3);
		expect(ctrl.lastLabelIndex).toBe(8);
	}));

	it('should reset navigation list limits with selected item', inject(function () {
		//given
		var ctrl = createController();
		ctrl.nbreLabelsToShow = 4;

		ctrl.firstLabelIndex = 0;
		ctrl.lastLabelIndex = 4;

		//when
		ctrl.list = [{item : 1}, {item : 2}, {item : 3}, {item : 4}, {item : 5}, {item : 6}, {item : 7}];

		ctrl.selectedItem = ctrl.list[4]; // {item : 5}
		scope.$digest();

		//then
		expect(ctrl.firstLabelIndex).toBe(1);
		expect(ctrl.lastLabelIndex).toBe(5);

		//when
		ctrl.selectedItem = ctrl.list[0]; //{item : 1}
		scope.$digest();

		//then
		expect(ctrl.firstLabelIndex).toBe(0);
		expect(ctrl.lastLabelIndex).toBe(4);
	}));

});