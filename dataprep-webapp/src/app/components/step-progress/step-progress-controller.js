/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class StepProgressCtrl {
	constructor(state, StateService, PROGRESS_STATES, PROGRESS_TYPES) {
		'ngInject';

		this.state = state;
		this.StateService = StateService;
		this.PROGRESS_STATES = PROGRESS_STATES;
		this.PROGRESS_TYPES = PROGRESS_TYPES;
	}

	getStepClass(state) {
		return {
			[this.PROGRESS_STATES.IN_PROGRESS]: 'in-progress',
			[this.PROGRESS_STATES.COMPLETE]: 'complete',
			[this.PROGRESS_STATES.FUTURE]: 'future',
		}[state];
	}
}
