/*  ============================================================================

  Copyright (C) 2006-2017 Talend Inc. - www.talend.com

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

	beforeEach(angular.mock.module('data-prep.services.progress'));

	it('should start the progress sequence', inject((ProgressService) => {
		//when
		ProgressService.start(schema);

		//then
		// expect(ProgressService.getProgression()).toBe(66);
		expect(ProgressService.current).toEqual(schema.steps[0]);
	}));

	it('should go to the next progress step and update states', inject((ProgressService) => {
		//given
		ProgressService.start(schema);

		expect(ProgressService.current).toEqual(schema.steps[0]);

		//when
		ProgressService.next();

		//then
		expect(ProgressService.steps[0]).toEqual(
			{
				type: 'PROGRESSION',
				state: 'COMPLETE',
				label: 'STEP_1',
			}
		);
		expect(ProgressService.current).toEqual(
			{
				type: 'INFINITE',
				state: 'IN_PROGRESS',
				label: 'STEP_2',
			}
		);
	}));

	it('should reset steps', inject((ProgressService) => {
		//given
		ProgressService.start(schema);
		expect(ProgressService.steps).toEqual(schema.steps);

		//when
		ProgressService.reset();

		//then
		expect(ProgressService.steps).toEqual([]);
	}));

	it('should return value', inject((ProgressService) => {
		//given
		ProgressService.start(schema, () => 42);

		//then
		expect(ProgressService.getProgression()).toBe(42);
	}));

	it('should set the title', inject((ProgressService) => {
		//given
		ProgressService.start(schema);

		//then
		expect(ProgressService.title).toBe('TEST_TITLE');
	}));
});
