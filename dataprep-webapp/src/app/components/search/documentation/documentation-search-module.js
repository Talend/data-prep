/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SEARCH_BAR_MODULE from '../bar/search-bar-module';
import SERVICES_SEARCH_MODULE from '../../../services/search/search-module';

import DocumentationSearch from './documentation-search-component';

const MODULE_NAME = 'data-prep.documentation-search';

/**
 * @ngdoc object
 * @name data-prep.data-prep.documentation-search
 * @description This module contains the component to manage an documentation search
 * @requires talend.widget
 */
angular.module(MODULE_NAME,
	[
		SEARCH_BAR_MODULE,
		SERVICES_SEARCH_MODULE,
	])
    .component('documentationSearch', DocumentationSearch);

export default MODULE_NAME;
