/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import ActionsSuggestionsCtrl from './actions-suggestions-controller';
import ActionsSuggestions from './actions-suggestions-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.actions-suggestions
     * @description This module contains the controller and directives to manage suggested transformation list
     * @requires talend.widget
     * @requires data-prep.services.transformation
     * @requires data-prep.services.state
     */
    angular.module('data-prep.actions-suggestions',
        [
            'talend.widget',
            'data-prep.services.transformation',
            'data-prep.services.state'
        ])
        .controller('ActionsSuggestionsCtrl', ActionsSuggestionsCtrl)
        .directive('actionsSuggestions', ActionsSuggestions);
})();