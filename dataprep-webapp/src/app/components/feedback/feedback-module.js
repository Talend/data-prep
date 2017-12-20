/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import TALEND_WIDGET_MODULE from '../widgets/widget-module';
import SERVICES_FEEDBACK_MODULE from '../../services/feedback/feedback-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';
import SERVICES_MESSAGE_MODULE from '../../services/message/message-module';

import FeedbackCtrl from './feedback-controller';
import Feedback from './feedback-directive';

const MODULE_NAME = 'data-prep.feedback';

angular.module(MODULE_NAME,
	[
		TALEND_WIDGET_MODULE,
		SERVICES_FEEDBACK_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_MESSAGE_MODULE,
	])
    .controller('FeedbackCtrl', FeedbackCtrl)
    .directive('feedback', Feedback);

export default MODULE_NAME;
