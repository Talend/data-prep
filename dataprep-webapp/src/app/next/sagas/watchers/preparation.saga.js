import { all, call, take } from 'redux-saga/effects';
import {
	OPEN_FOLDER,
	PREPARATION_COPY,
	PREPARATION_MOVE,
	RENAME_PREPARATION,
	FETCH_PREPARATIONS,
	OPEN_COPY_MODAL,
	OPEN_MOVE_MODAL,
	CLOSE_COPY_MOVE_MODAL,
	PREPARATION_DUPLICATE,
	SET_TITLE_EDITION_MODE,
	OPEN_PREPARATION_CREATOR,
	CANCEL_RENAME_PREPARATION,
} from '../../constants/actions';
import * as effects from '../effects/preparation.effects';

function* cancelRename() {
	while (true) {
		const { payload } = yield take(CANCEL_RENAME_PREPARATION);
		yield call(effects.cancelRename, payload);
	}
}

function* duplicate() {
	while (true) {
		const prep = yield take(PREPARATION_DUPLICATE);
		yield call(effects.duplicate, prep);
	}
}

function* fetch() {
	while (true) {
		const { payload } = yield take(FETCH_PREPARATIONS);
		yield all([
			call(effects.fetch, payload),
			call(effects.fetchFolder, payload),
		]);
	}
}

function* openFolder() {
	while (true) {
		const { id } = yield take(OPEN_FOLDER);
		yield call(effects.openFolder, id);
	}
}

function* rename() {
	while (true) {
		const { payload } = yield take(RENAME_PREPARATION);
		yield call(effects.rename, payload);
	}
}

function* copy() {
	while (true) {
		const { payload } = yield take(PREPARATION_COPY);
		yield call(effects.copy, payload);
	}
}

function* move() {
	while (true) {
		const { payload } = yield take(PREPARATION_MOVE);
		yield call(effects.move, payload);
	}
}

function* setTitleEditionMode() {
	while (true) {
		const { payload } = yield take(SET_TITLE_EDITION_MODE);
		yield call(effects.setTitleEditionMode, payload);
	}
}

function* openPreparationCreatorModal() {
	while (true) {
		yield take(OPEN_PREPARATION_CREATOR);
		yield call(effects.openPreparationCreatorModal);
	}
}

function* openCopyModal() {
	while (true) {
		const { payload } = yield take(OPEN_COPY_MODAL);
		yield all([call(effects.fetchTree), call(effects.openCopyMoveModal, payload, 'copy')]);
	}
}

function* openMoveModal() {
	while (true) {
		const { payload } = yield take(OPEN_MOVE_MODAL);
		yield all([call(effects.fetchTree), call(effects.openCopyMoveModal, payload, 'move')]);
	}
}

function* closeCopyMoveModal() {
	while (true) {
		yield take(CLOSE_COPY_MOVE_MODAL);
		yield call(effects.closeCopyMoveModal);
	}
}

export default {
	'preparation:copy': copy,
	'preparation:move': move,
	'preparation:fetch': fetch,
	'preparation:duplicate': duplicate,
	'preparation:rename:submit': rename,
	'preparation:folder:open': openFolder,
	'preparation:rename:cancel': cancelRename,
	'preparation:rename': setTitleEditionMode,
	'preparation:copy:open': openCopyModal,
	'preparation:move:open': openMoveModal,
	'preparation:copy:move:cancel': closeCopyMoveModal,
	'preparation:creator:open': openPreparationCreatorModal,
};
