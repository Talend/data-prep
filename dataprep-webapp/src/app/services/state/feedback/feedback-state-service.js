/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

export const feedbackState = {
	visible: false,
};

/**
 * @ngdoc service
 * @name data-prep.services.state.service:FeedbackStateService
 * @description Manage the state of the feedback
 */
export function FeedbackStateService() {
	return {
		show,
		hide,
	};

    /**
     * @ngdoc method
     * @name show
     * @methodOf data-prep.services.state.service:FeedbackStateService
     * @description Display the feedback
     */
	function show() {
		feedbackState.visible = true;
	}

    /**
     * @ngdoc method
     * @name hide
     * @methodOf data-prep.services.state.service:FeedbackStateService
     * @description Hide the feedback
     */
	function hide() {
		feedbackState.visible = false;
	}
}
