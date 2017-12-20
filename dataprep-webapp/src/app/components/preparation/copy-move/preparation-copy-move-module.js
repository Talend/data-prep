/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import INVENTORY_COPY_MOVE_MODULE from '../../inventory/copy-move/inventory-copy-move-module';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_FOLDER_MODULE from '../../../services/folder/folder-module';
import SERVICES_PREPARATION_MODULE from '../../../services/preparation/preparation-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';
import SERVICES_MESSAGE_MODULE from '../../../services/message/message-module';

import PreparationCopyMoveContainer from './preparation-copy-move-container';

const MODULE_NAME = 'data-prep.preparation-copy-move';

/**
 * @ngdoc object
 * @name data-prep.preparation-copy-move
 * @description This module contains the entities to manage the preparation copy/move
 * @requires data-prep.inventory-copy-move
 * @requires talend.widget
 * @requires data-prep.services.folder
 * @requires data-prep.services.preparation
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME,
	[
		INVENTORY_COPY_MOVE_MODULE,
		TALEND_WIDGET_MODULE,
		SERVICES_FOLDER_MODULE,
		SERVICES_PREPARATION_MODULE,
		SERVICES_MESSAGE_MODULE,
		SERVICES_STATE_MODULE,
	])
    .component('preparationCopyMove', PreparationCopyMoveContainer);

export default MODULE_NAME;
