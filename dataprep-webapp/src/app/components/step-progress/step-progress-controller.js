/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class StepProgressCtrl {
	constructor(ProgressService, PROGRESSION_TYPES, PROGRESSION_STATES) {
		'ngInject';
		this.ProgressService = ProgressService;
		this.PROGRESSION_TYPES = PROGRESSION_TYPES;
		this.PROGRESSION_STATES = PROGRESSION_STATES;
	}

	getSpinnerClass(state) {
		return {
			IN_PROGRESS: 'in-progress',
			COMPLETE: 'complete',
			FUTURE: 'future',
		}[state];
	}
}
