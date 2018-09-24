import { all, call, take } from 'redux-saga/effects';
import * as actions from '../../constants/actions';
import * as effects from '../effects/preparation.effects';

function* cancelRename() {
	while (true) {
		const { payload } = yield take(actions.CANCEL_RENAME_PREPARATION);
		yield call(effects.cancelRename, payload);
	}
}

function* fetch() {
	while (true) {
		const { payload } = yield take(actions.FETCH_PREPARATIONS);
		yield call(effects.refresh, payload);
	}
}

function* rename() {
	while (true) {
		const { payload } = yield take(actions.RENAME_PREPARATION);
		yield call(effects.rename, payload);
	}
}

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

function* copy() {
	while (true) {
		const { payload } = yield take(actions.PREPARATION_COPY);
		yield call(effects.copy, payload);
	}
}

function* move() {
	while (true) {
		const { payload } = yield take(actions.PREPARATION_MOVE);
		yield call(effects.move, payload);
	}
}

function* setTitleEditionMode() {
	while (true) {
		const { payload } = yield take(actions.SET_TITLE_EDITION_MODE);
		yield call(effects.setTitleEditionMode, payload);
	}
}

function* openCopyModal() {
	while (true) {
		const { payload } = yield take(actions.OPEN_COPY_MODAL);
		yield all([call(effects.fetchTree), call(effects.openCopyMoveModal, payload, 'copy')]);
	}
}

function* openMoveModal() {
	while (true) {
		const { payload } = yield take(actions.OPEN_MOVE_MODAL);
		yield all([call(effects.fetchTree), call(effects.openCopyMoveModal, payload, 'move')]);
	}
}

function* closeCopyMoveModal() {
	while (true) {
		yield take(actions.CLOSE_COPY_MOVE_MODAL);
		yield call(effects.closeCopyMoveModal);
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

function* openPreparationCreatorModal() {
	while (true) {
		yield take(actions.OPEN_PREPARATION_CREATOR);
		yield call(effects.openPreparationCreatorModal);
	}
}

export default {
	'preparation:copy': copy,
	'preparation:move': move,
	'preparation:fetch': fetch,
	'preparation:closeRemoveFolderConfirmDialog': closeRemoveFolderModal,
	'preparation:openRemoveFolderConfirmDialog': openRemoveFolderModal,
	'preparation:folder:add': addFolder,
	'preparation:folder:closeAddFolderConfirmDialog': closeAddFolderModal,
	'preparation:folder:openAddFolderConfirmDialog': openAddFolderModal,
	'preparation:folder:remove': removeFolder,
	'preparation:rename:submit': rename,
	'preparation:rename:cancel': cancelRename,
	'preparation:rename': setTitleEditionMode,
	'preparation:copy:open': openCopyModal,
	'preparation:move:open': openMoveModal,
	'preparation:copy:move:cancel': closeCopyMoveModal,
	'preparation:creator:open': openPreparationCreatorModal,
};
