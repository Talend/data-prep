/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.lookup.controller:LookupDatagridCtrl
 * @description Dataset grid controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.lookup.service:DatagridTooltipService
 */
export default function LookupDatagridCtrl(state) {
	'ngInject';
	this.state = state;
}
