import { actions } from '@talend/react-cmf';
import { Map } from 'immutable';
import { DEFAULT_STATE } from './FolderCreatorModal.component';


export function show() {
	return actions.components.mergeState('Translate(FolderCreatorModal)', 'default', {
		...DEFAULT_STATE.toJS(),
		show: true,
	});
}

export function hide() {
	return actions.components.mergeState('Translate(FolderCreatorModal)', 'default', {
		...DEFAULT_STATE.toJS(),
		show: false,
	});
}

export default {
	'FolderCreatorModal#show': show,
	'FolderCreatorModal#hide': hide,
};
