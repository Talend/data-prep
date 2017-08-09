/*  ============================================================================

 Copyright (C) 2006-2017 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/


/**
 * @ngdoc service
 * @name data-prep.services.progress.service:ProgressService
 * @description Progress Service. This service manage the progressions
 */
export default class ProgressService {
	constructor(ProgressConstants) {
		'ngInject';

		this.ProgressConstants = ProgressConstants;
		this._steps = [];
		this.title = '';
		this.getProgression = null;
	}

	/**
	 * @ngdoc method
	 * @name start
	 * @methodOf data-prep.services.progress.service:ProgressService
	 * @description Displays the step progress modal
	 * @param {Object} schema The steps and the modal title
	 * @param {Function} getter The getter to use to obtain the progress value
	 */
	start(schema, getter) {
		this.title = schema.title;
		this.steps = schema.steps;
		this.getProgression = getter;
	}

	/**
	 * @ngdoc method
	 * @name next
	 * @methodOf data-prep.services.progress.service:ProgressService
	 * @description Displays the next step
	 */
	next() {
		const index = this._steps.findIndex(step => step.state === this.ProgressConstants.STATES.IN_PROGRESS);

		if (this._steps[index + 1]) {
			this._steps[index].state = this.ProgressConstants.STATES.COMPLETE;
			this._steps[index + 1].state = this.ProgressConstants.STATES.IN_PROGRESS;
		}
		else {
			this.reset();
		}
	}

	/**
	 * @ngdoc method
	 * @name reset
	 * @methodOf data-prep.services.progress.service:ProgressService
	 * @description Hide the modal and reset his attributes
	 */
	reset() {
		this.progressionGetter = null;
		this.title = '';
		this._steps = [];
	}

	set steps(steps) {
		this._steps = [...steps].map((s) => {
			return { ...s };
		});
	}

	get steps() {
		return this._steps;
	}

	get current() {
		return this._steps.find(step => step.state === this.ProgressConstants.STATES.IN_PROGRESS);
	}
}
