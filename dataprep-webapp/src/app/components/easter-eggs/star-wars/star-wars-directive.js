/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './star-wars.html';

/**
 * @ngdoc directive
 * @name data-prep.star-wars.directive:StarWars
 * @description StarWars easter eggs
 * @restrict E
 * @usage <star-wars></star-wars>
 */
export default function StarWars() {
	return {
		restrict: 'E',
		templateUrl: template,
	};
}
