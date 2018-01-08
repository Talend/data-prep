/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './badge.html';

/**
 * @ngdoc component
 * @name talend.widget.component:TalendBadge
 * @usage
 <talend-badge removable="true|false"
               on-remove="onRemove()"></talend-badge>
 * @param {boolean} removable If we provide a close button
 * @param {Function} onRemove The callback that is triggered on badge close
 */
const TalendBadge = {
	bindings: {
		removable: '<',
		onRemove: '&',
	},
	templateUrl: template,
	transclude: true,
};

export default TalendBadge;
