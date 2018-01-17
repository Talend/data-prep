/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class StepProgressCtrl {
	constructor(state, StateService) {
		'ngInject';

		this.state = state;
		this.StateService = StateService;
	}

	getStepClass(state) {
		return {
			[this.state.progress.states.inProgress]: 'in-progress',
			[this.state.progress.states.complete]: 'complete',
			[this.state.progress.states.future]: 'future',
		}[state];
	}
}
