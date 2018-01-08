/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './loader.html';

/**
 * @ngdoc component
 * @name talend.component:Loader
 * @description Simple refresh loader, its parent should have 'position: relative;'
 * @usage
 <loader translate-once-key='translateOnceKey'></loader>
 * @param {String}    translateOnceKey      Key to translate
 */
const Loader = {
	bindings: {
		translateOnceKey: '<',
	},
	templateUrl: template,
};

export default Loader;
