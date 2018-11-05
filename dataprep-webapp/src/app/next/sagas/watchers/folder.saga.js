import { call, take } from 'redux-saga/effects';
import * as actions from '../../constants/actions';
import * as effects from '../effects/folder.effects';


function* removeFolder() {
	while (true) {
		yield take(actions.REMOVE_FOLDER);
		yield call(effects.removeFolder);
	}
}

function* openRemoveFolderModal() {
	while (true) {
		const { payload } = yield take(actions.OPEN_REMOVE_FOLDER_MODAL);
		yield call(effects.openRemoveFolderModal, payload);
	}
}

function* closeRemoveFolderModal() {
	while (true) {
		yield take(actions.CLOSE_REMOVE_FOLDER_MODAL);
		yield call(effects.closeRemoveFolderModal);
	}
}


function* openAddFolderModal() {
	while (true) {
		yield take(actions.OPEN_ADD_FOLDER_MODAL);
		yield call(effects.openAddFolderModal);
	}
}

function* closeAddFolderModal() {
	while (true) {
		yield take(actions.CLOSE_ADD_FOLDER_MODAL);
		yield call(effects.closeAddFolderModal);
	}
}

function* addFolder() {
	while (true) {
		yield take(actions.ADD_FOLDER);
		yield call(effects.addFolder);
	}
}

export default {
	'folder:closeRemoveFolderConfirmDialog': closeRemoveFolderModal,
	'folder:openRemoveFolderConfirmDialog': openRemoveFolderModal,
	'folder:closeAddFolderConfirmDialog': closeAddFolderModal,
	'folder:openAddFolderConfirmDialog': openAddFolderModal,
	'folder:remove': removeFolder,
	'folder:add': addFolder,
};
