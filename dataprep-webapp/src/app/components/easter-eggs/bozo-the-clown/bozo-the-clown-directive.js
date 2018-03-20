/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './bozo-the-clown.html';

/**
 * @ngdoc directive
 * @name data-prep.bozo.directive:BozoTheClown
 * @description Bozo the famous clown easter eggs
 * @restrict E
 * @usage <bozo-the-clown></bozo-the-clown>
 */
export default function BozoTheClown() {
	return {
		restrict: 'E',
		templateUrl: template,
	};
}
