/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class StepProgressCtrl {
	constructor($translate) {
		'ngInject';

		this.translations = [
			'UPLOADING_FILE',
			'PROFILING_DATA',
		];

		$translate(this.translations)
		.then(translations => this.translations = this.translations.map(t => translations[t]));
	}

	get currentStep() {
		return this.steps.find(step => step.state === 'IN_PROGRESS');
	}
}
