/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Dataset progress controller', () => {
	let createController;
	let scope;

	beforeEach(angular.mock.module('data-prep.step-progress'));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();

		createController = () => {
			return $componentController(
				'stepProgress',
				{ $scope: scope }
			);
		};
	}));

	describe('current step getter', () => {
		it('should return the actual step', inject(() => {
			//given
			const ctrl = createController();
			ctrl.steps = [
				{label: 'complete', state: 'COMPLETE'},
				{label: 'future', state: 'FUTURE'},
				{label: 'in progress', state: 'IN_PROGRESS'},
			];

			//then
			expect(ctrl.currentStep).toEqual({label: 'in progress', state: 'IN_PROGRESS'});
		}));
	});
});
