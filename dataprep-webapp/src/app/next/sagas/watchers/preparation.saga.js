import { all, call, take } from 'redux-saga/effects';
import {
	CANCEL_RENAME_PREPARATION,
	FETCH_PREPARATIONS,
	OPEN_FOLDER,
	PREPARATION_DUPLICATE,
	RENAME_PREPARATION,
	SET_TITLE_EDITION_MODE,
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

function* setTitleEditionMode() {
	while (true) {
		const { payload } = yield take(SET_TITLE_EDITION_MODE);
		yield call(effects.setTitleEditionMode, payload);
	}
}

export default {
	'preparation:rename:cancel': cancelRename,
	'preparation:duplicate': duplicate,
	'preparation:fetch': fetch,
	'preparation:folder:open': openFolder,
	'preparation:rename:submit': rename,
	'preparation:rename': setTitleEditionMode,
};
