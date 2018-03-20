/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/


export const exportState = {
	exportTypes: [],
	defaultExportType: {},
};

export function ExportStateService() {
	return {
		setExportTypes,
		setDefaultExportType,
		reset,
	};

	function setExportTypes(exportTypes) {
		exportState.exportTypes = exportTypes;
	}

	function setDefaultExportType(exportType) {
		exportState.defaultExportType = exportType;
	}

	function reset() {
		exportState.exportTypes = [];
		exportState.defaultExportType = {};
	}
}
