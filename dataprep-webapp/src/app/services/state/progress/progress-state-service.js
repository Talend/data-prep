/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const types = {
	progression: 'PROGRESSION',
	infinite: 'INFINITE',
};

const states = {
	inProgress: 'IN_PROGRESS',
	future: 'FUTURE',
	complete: 'COMPLETE',
};

const schemas = {
	dataset: {
		title: 'ADD_NEW_DATASET',
		steps: [
			{
				type: types.progression,
				state: states.inProgress,
				label: 'UPLOADING_FILE',
			},
			{
				type: types.infinite,
				state: states.future,
				label: 'PROFILING_DATA',
			},
		],
	},
};

export const progressState = {
	steps: [],
	title: '',
	progressionGetter: null,
	types,
	states,
	schemas,
};

export function ProgressStateService() {
	return {
		start,
		next,
		reset,
		getCurrentStep,
		addSchema,
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
		progressState.steps = [...schema.steps].map((step) => {
			return { ...step };
		});
	}

	/**
	 * @ngdoc method
	 * @name next
	 * @methodOf data-prep.services.state.service:ProgressStateService
	 * @description Displays the next step
	 */
	function next() {
		const index = progressState.steps.findIndex(step => step.state === states.inProgress);

		if (progressState.steps[index + 1]) {
			progressState.steps[index].state = states.complete;
			progressState.steps[index + 1].state = states.inProgress;
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
		return progressState.steps.find(step => step.state === states.inProgress);
	}

	/**
	 * @ngdoc method
	 * @name addSchema
	 * @methodOf data-prep.services.state.service:ProgressStateService
	 * @description Adds the given schema to the schemas array
	 */
	function addSchema(key, schema) {
		progressState.schemas[key] = schema;
	}
}
