import { actions } from '@talend/react-cmf';
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

export function setError(event, error) {
	return actions.components.mergeState('Translate(FolderCreatorModal)', 'default', {
		error,
	});
}

export default {
	'FolderCreatorModal#show': show,
	'FolderCreatorModal#hide': hide,
	'FolderCreatorModal#setError': setError,
};
