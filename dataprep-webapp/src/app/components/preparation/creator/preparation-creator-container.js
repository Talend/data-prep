/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './preparation-creator.html';

/**
 * @ngdoc component
 * @name data-prep.preparation-creator:preparationCreator
 * @description This component renders add preparation modal content
 * @usage
 *      <preparation-creator></preparation-creator>
 * */

export default {
	templateUrl: template,
	controller(state) {
		'ngInject';
		this.state = state;

		this.canBeClosed = () => {
			return !this.state.dataset.uploadingDataset;
		};
	},
};
