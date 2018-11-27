/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const confirmState = {
	visible: false,
	deletion: false,
	title: null,
	texts: [],
};

export class ConfirmStateService {
	show(title, texts, deletion = false) {
		confirmState.visible = true;
		confirmState.deletion = deletion;
		confirmState.texts = texts;
		confirmState.title = title;
	}

	hide() {
		confirmState.visible = false;
		confirmState.texts = [];
		confirmState.title = null;
	}

	reset() {
		confirmState.visible = false;
		confirmState.deletion = false;
		confirmState.texts = [];
		confirmState.title = null;
	}
}
