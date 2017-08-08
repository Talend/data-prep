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
	constructor(PROGRESSION_STATES) {
		'ngInject';

		this.PROGRESSION_STATES = PROGRESSION_STATES;
		this._steps = [];
		this.title = '';
		this.progressionGetter = null;
	}

	// /**
	//  * @ngdoc method
	//  * @name fetchBuildDetails
	//  * @methodOf data-prep.services.progress.service:ProgressService
	//  * @description Fetches the build id of each backend service
	//  * @returns {Promise} The GET call promise
	//  */
	// set steps(steps) {
	// 	this._steps = steps;
	// }
	//
	// set title(title) {
	// 	this.title = this.$translate(title);
	// }
	//
	// set progressionGetter(getter) {
	// 	this.progressionGetter = getter;
	// }

	/**
	 * @ngdoc method
	 * @name loadBuilds
	 * @methodOf data-prep.services.progress.service:ProgressService
	 * @description sets the fetched builds in the state
	 */
	next() {
		const index = this._steps.findIndex(step => step.state === this.PROGRESSION_STATES.IN_PROGRESS);

		if (this._steps[index + 1]) {
			this._steps[index].state = this.PROGRESSION_STATES.COMPLETE;
			this._steps[index + 1].state = this.PROGRESSION_STATES.IN_PROGRESS;
		}
		else {
			this.progressionGetter = null;
			this.title = '';
			this._steps = [];
		}
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
		return this._steps.find(step => step.state === this.PROGRESSION_STATES.IN_PROGRESS);
	}
}
