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

	describe('step class getter', () => {
		it('should return the appropriate class', inject((ProgressConstants) => {
			//given
			const ctrl = createController();

			//then
			expect(ctrl.getStepClass(ProgressConstants.STATES.IN_PROGRESS)).toBe('in-progress');
			expect(ctrl.getStepClass(ProgressConstants.STATES.COMPLETE)).toBe('complete');
			expect(ctrl.getStepClass(ProgressConstants.STATES.FUTURE)).toBe('future');
		}));
	});
});
