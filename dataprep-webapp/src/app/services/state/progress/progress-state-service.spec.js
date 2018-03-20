/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('progress service', function () {
	const schema = {
		title: 'TEST_TITLE',
		steps: [
			{
				type: 'PROGRESSION',
				state: 'IN_PROGRESS',
				label: 'STEP_1',
			},
			{
				type: 'INFINITE',
				state: 'FUTURE',
				label: 'STEP_2',
			},
		],
	};


	beforeEach(angular.mock.module('data-prep.services.state'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.preferredLanguage('en');
	}));

	it('should start the progress sequence', inject((StateService) => {
		// when
		StateService.startProgress(schema);

		// then
		expect(StateService.getCurrentProgressStep()).toEqual(schema.steps[0]);
	}));

	it('should go to the next progress step and update states', inject((StateService, progressState) => {
		// given
		StateService.startProgress(schema);

		expect(StateService.getCurrentProgressStep()).toEqual(schema.steps[0]);

		// when
		StateService.nextProgress();

		// then
		expect(progressState.steps[0]).toEqual(
			{
				type: 'PROGRESSION',
				state: 'COMPLETE',
				label: 'STEP_1',
			}
		);
		expect(StateService.getCurrentProgressStep()).toEqual(
			{
				type: 'INFINITE',
				state: 'IN_PROGRESS',
				label: 'STEP_2',
			}
		);
	}));

	it('should reset steps', inject((StateService, progressState) => {
		// given
		StateService.startProgress(schema);
		expect(progressState.steps).toEqual(schema.steps);

		// when
		StateService.resetProgress();

		// then
		expect(progressState.steps).toEqual([]);
	}));

	it('should return value', inject((StateService, progressState) => {
		// given
		StateService.startProgress(schema, () => 42);

		// then
		expect(progressState.progressionGetter()).toBe(42);
	}));

	it('should set the title', inject((StateService, progressState) => {
		// given
		StateService.startProgress(schema);

		// then
		expect(progressState.title).toBe('TEST_TITLE');
	}));

	it('should add the given schema', inject((StateService, progressState) => {
		// given
		const actual = Object.assign({}, progressState.schemas);
		const add = {
			title: 'A',
			steps: [{ label: 'A1' }, { label: 'A2' }],
		};

		// when
		StateService.addProgressSchema('new', add);

		// then
		expect(progressState.schemas).toEqual({
			...actual,
			new: add,
		});
	}));
});
