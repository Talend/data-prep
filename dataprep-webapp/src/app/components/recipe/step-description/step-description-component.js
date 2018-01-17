/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import StepDescriptionCtrl from './step-description-controller';

const StepDescription = {
	bindings: {
		index: '<',
		step: '<',
	},
	controller: StepDescriptionCtrl,
	template: '<span ng-bind-html="$ctrl.stepDescription" class="step-description"></span>',
};

export default StepDescription;
