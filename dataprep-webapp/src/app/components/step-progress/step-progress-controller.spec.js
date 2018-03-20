/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Dataset progress controller', () => {
	let createController;
	let scope;
	let stateMock;

	beforeEach(angular.mock.module('data-prep.step-progress', ($provide) => {
		stateMock = {
			home: {
				preparations: {
					creator: {
						isVisible: false,
					},
				},
			},
			progress: {
				steps: [],
				types: {
					progression: 'PROGRESSION',
					infinite: 'INFINITE',
				},
				states: {
					inProgress: 'IN_PROGRESS',
					future: 'FUTURE',
					complete: 'COMPLETE',
				},
				schemas: {
					dataset: {
						title: 'ADD_NEW_DATASET',
						steps: [
							{
								type: 'PROGRESSION',
								state: 'IN_PROGRESS',
								label: 'UPLOADING_FILE',
							},
							{
								type: 'INFINITE',
								state: 'FUTURE',
								label: 'PROFILING_DATA',
							},
						],
					},
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.preferredLanguage('en');
	}));

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
		it('should return the appropriate class', inject(() => {
			//given
			const ctrl = createController();

			//then
			expect(ctrl.getStepClass('IN_PROGRESS')).toBe('in-progress');
			expect(ctrl.getStepClass('COMPLETE')).toBe('complete');
			expect(ctrl.getStepClass('FUTURE')).toBe('future');
		}));
	});
});
