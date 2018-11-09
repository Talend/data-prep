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

function* create() {
	while (true) {
		const { payload } = yield take(actions.CREATE_PREPARATIONS);
		yield call(effects.create, payload);
	}
}

function* rename() {
	while (true) {
		const { payload } = yield take(actions.RENAME_PREPARATION);
		yield call(effects.rename, payload);
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

function* openPreparationCreatorModal() {
	while (true) {
		yield take(actions.OPEN_PREPARATION_CREATOR);
		yield call(effects.openPreparationCreatorModal);
	}
}

export default {
	'preparation:copy': copy,
	'preparation:move': move,
	'preparation:create': create,
	'preparation:fetch': fetch,
	'preparation:rename:submit': rename,
	'preparation:copy:open': openCopyModal,
	'preparation:move:open': openMoveModal,
	'preparation:rename:cancel': cancelRename,
	'preparation:rename': setTitleEditionMode,
	'preparation:copy:move:cancel': closeCopyMoveModal,
	'preparation:creator:open': openPreparationCreatorModal,
};
