/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import LookupDatagridHeaderCtrl from './lookup-datagrid-header-controller';
import LookupDatagridHeader from './lookup-datagrid-header-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.lookup-datagrid-header
     * @description This module contains the controller and directives to manage the lookup-datagrid header with transformation menu
     * @requires talend.module:widget
     * @requires data-prep.transformation-menu
     * @requires data-prep.services.utils
     * @requires data-prep.services.playground
     * @requires data-prep.services.transformation
     * @requires data-prep.quality-bar
     */
    angular.module('data-prep.lookup-datagrid-header',
        [
            'talend.widget',
            'data-prep.transformation-menu',
            'data-prep.services.utils',
            'data-prep.services.playground',
            'data-prep.services.transformation'
        ])
        .controller('LookupDatagridHeaderCtrl', LookupDatagridHeaderCtrl)
        .directive('lookupDatagridHeader', LookupDatagridHeader);
})();