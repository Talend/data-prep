/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './preparation-picker.html';

import PreparationPickerCtrl from './preparation-picker-controller';

const PreparationPicker = {
	templateUrl: template,
	bindings: {
		dataset: '<',
		fetchPreparations: '&',
		onSelect: '&',
	},
	controller: PreparationPickerCtrl,
};
export default PreparationPicker;
