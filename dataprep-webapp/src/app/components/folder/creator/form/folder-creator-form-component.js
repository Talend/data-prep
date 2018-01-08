/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc component
 * @name data-prep.preparation-creator:preparationCreatorForm
 * @description This component renders add preparation modal content
 * @usage
 *      <preparation-creator-form></preparation-creator-form>
 * */

import PreparationCreatorCtrl from './folder-creator-form-controller';

import template from './folder-creator-form.html';

export default {
	templateUrl: template,
	controller: PreparationCreatorCtrl,
};
