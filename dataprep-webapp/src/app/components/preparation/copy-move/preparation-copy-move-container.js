/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './preparation-copy-move.html';
import PreparationCopyMoveCtrl from './preparation-copy-move-controller';

/**
 * @ngdoc directive
 * @name data-prep.preparation-copy-move.component:PreparationCopyMoveContainer
 * @description This component display the inventory copy/move modal for preparations.
 * @restrict E
 */
const PreparationCopyMoveContainer = {
	templateUrl: template,
	controller: PreparationCopyMoveCtrl,
};

export default PreparationCopyMoveContainer;
