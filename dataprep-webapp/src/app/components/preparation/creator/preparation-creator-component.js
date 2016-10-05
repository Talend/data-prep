/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc component
 * @name data-prep.preparation-creator:preparationCreator
 * @description This component renders add preparation modal content
 * @usage
 *      <preparation-creator
 *      <preparation-creator
 *          show-add-prep-modal="$ctrl.showAddPrepModal">
 *       </preparation-creator>
 * @param {Boolean} showAddPrepModal show/hide the whole modal
 * */

import PreparationCreatorCtrl from './preparation-creator-controller';

import template from './preparation-creator.html';

export default {
	templateUrl: template,
	bindings: { onCreation: '&' },
	controller: PreparationCreatorCtrl,
};
