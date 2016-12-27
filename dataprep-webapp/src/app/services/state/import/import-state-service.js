/*  ============================================================================
 Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE
 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France
 ============================================================================*/

export const importState = {
	importTypes: [],
	showImportModal: false,
};

export function ImportStateService() {
	return {
		setImportTypes,
		setShowImportModal,
	};

	function setImportTypes(importTypes) {
		importState.importTypes = importTypes;
	}

	function setShowImportModal(bool) {
		importState.showImportModal = bool;
	}
}
