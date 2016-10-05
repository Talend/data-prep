/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './playground-header.html';

/**
 * @ngdoc directive
 * @name talend.sunchoke.accordion.directive:ScAccordionAnimation
 * @description Accordion animation on open/close
 * @restrict A
 * @usage
 <playground-header
         enable-export="true"
         feedback-visible="true"
         lookup-visible="lookupInProgress"
         on-close="close()"
         on-feedback="openFeedback()"
         on-lookup="toggleLookup()"
         on-onboarding="startOnBoarding(')"
         on-parameters="toggleParameters()"
         on-preparation-picker="onPreparationPicker()"
         parameters-visible="hasParametersVisible"
         preparation-picker="true"
         preview="previewInProgress"
 ></playground-header>
 * @param {boolean} enableExport show export component
 * @param {boolean} feedbackVisible feedback form is visible
 * @param {boolean} lookupVisible A lookup is in progress
 * @param {function} onClose Callback on close icon click
 * @param {function} onFeedback Callback on feedback icon click
 * @param {function} onLookup Callback on lookup icon click
 * @param {function} onOnboarding Callback on onboarding icon click
 * @param {function} onParameters Callback on gear icon click
 * @param {function} onPreparationPicker Callback on open preparation picker
 * @param {boolean} parametersVisible Dataset parameters window is visible
 * @param {Boolean} preparationPicker display of the button
 * @param {boolean} preview A preview is in progress
 */
const PlaygroundHeader = {
	templateUrl: template,
	bindings: {
		enableExport: '<',
		feedbackVisible: '<',
		lookupVisible: '<',
		onClose: '&',
		onFeedback: '&',
		onLookup: '&',
		onOnboarding: '&',
		onParameters: '&',
		onPreparationPicker: '&',
		parametersVisible: '<',
		preparationPicker: '<',
		preview: '<',
	},
};

export default PlaygroundHeader;
