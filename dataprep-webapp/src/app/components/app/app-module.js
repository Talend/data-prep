/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import DataPrepApp from './app-directive';

(() => {
    'use strict';

    angular.module('data-prep.app',
        [
            'data-prep.navbar',
            'data-prep.home',
            'data-prep.easter-eggs',
            'data-prep.feedback'
        ])
        .directive('dataprepApp', DataPrepApp);
})();