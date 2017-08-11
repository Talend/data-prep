/*  ============================================================================

 Copyright (C) 2006-2017 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const TYPES = {
	PROGRESSION: 'PROGRESSION',
	INFINITE: 'INFINITE',
};

export const STATES = {
	IN_PROGRESS: 'IN_PROGRESS',
	FUTURE: 'FUTURE',
	COMPLETE: 'COMPLETE',
};

export const SCHEMAS = {
	DATASET: {
		title: 'ADD_NEW_DATASET',
		steps: [
			{
				type: TYPES.PROGRESSION,
				state: STATES.IN_PROGRESS,
				label: 'UPLOADING_FILE',
			},
			{
				type: TYPES.INFINITE,
				state: STATES.FUTURE,
				label: 'PROFILING_DATA',
			},
		],
	},
};

export const progressState = {
	steps: [],
	title: '',
	progressionGetter: null,
};

export function ProgressStateService() {
	return {
		start,
		next,
		reset,
		getCurrentStep,
	};

	/**
	 * @ngdoc method
	 * @name start
	 * @methodOf data-prep.services.state.service:ProgressStateService
	 * @description Displays the step progress modal
	 * @param {Object} schema The steps and the modal title
	 * @param {Function} getter The getter to use to obtain the progress value
	 */
	function start(schema, getter) {
		progressState.title = schema.title;
		progressState.progressionGetter = getter || (() => 100);
		progressState.steps = [...schema.steps].map((s) => {
			return { ...s };
		});
	}

	/**
	 * @ngdoc method
	 * @name next
	 * @methodOf data-prep.services.state.service:ProgressStateService
	 * @description Displays the next step
	 */
	function next() {
		const index = progressState.steps.findIndex(step => step.state === STATES.IN_PROGRESS);

		if (progressState.steps[index + 1]) {
			progressState.steps[index].state = STATES.COMPLETE;
			progressState.steps[index + 1].state = STATES.IN_PROGRESS;
		}
		else {
			progressState.reset();
		}
	}

	/**
	 * @ngdoc method
	 * @name reset
	 * @methodOf data-prep.services.state.service:ProgressStateService
	 * @description Hide the modal and reset his attributes
	 */
	function reset() {
		progressState.progressionGetter = null;
		progressState.title = '';
		progressState.steps = [];
	}

	/**
	 * @ngdoc method
	 * @name getCurrentStep
	 * @methodOf data-prep.services.state.service:ProgressStateService
	 * @description Return the current step (IN_PROGRESS state)
	 */
	function getCurrentStep() {
		return progressState.steps.find(step => step.state === STATES.IN_PROGRESS);
	}
}
