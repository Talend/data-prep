/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import FeedbackCtrl from './feedback-controller';
import Feedback from './feedback-directive';

(() => {
    'use strict';

    angular.module('data-prep.feedback',
        [
            'talend.widget',
            'data-prep.services.utils',
            'data-prep.services.feedback',
            'data-prep.services.state'
        ])
        .controller('FeedbackCtrl', FeedbackCtrl)
        .directive('feedback', Feedback);
})();