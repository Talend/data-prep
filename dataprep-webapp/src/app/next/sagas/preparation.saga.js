import { call, take, put, select } from 'redux-saga/effects';
import http from '@talend/react-cmf/lib/sagas/http';
import api, { actions } from '@talend/react-cmf';
import {
	CANCEL_RENAME_PREPARATION,
	FETCH_PREPARATIONS,
	OPEN_FOLDER,
	OPEN_PREPARATION_CREATOR,
	PREPARATION_DUPLICATE,
	RENAME_PREPARATION,
	SET_TITLE_EDITION_MODE,
} from '../constants/actions';
import PreparationService from '../services/preparation.service';

const defaultHttpConfiguration = {
	headers: {
		Accept: 'application/json, text/plain, */*',
		'Content-Type': 'application/json',
	},
};

function* cancelRename() {
	while (true) {
		const { payload } = yield take(CANCEL_RENAME_PREPARATION);
		const preparations = yield select(state => state.cmf.collections.get('preparations'));
		const updated = preparations.update(
			preparations.findIndex(val => val.get('id') === payload),
			val => val.set('display', 'text'),
		);
		yield put(actions.collections.addOrReplace('preparations', updated));
	}
}

function* duplicate() {
	while (true) {
		const prep = yield take(PREPARATION_DUPLICATE);
		const newName = `test${Math.random()}`;

		yield call(
			http.post,
			`http://localhost:8888/api/preparations/${prep.payload.id}/copy?destination=Lw==&newName=${newName}`,
			{},
			defaultHttpConfiguration,
		);
		yield call(fetch);
	}
}

function* fetch() {
	while (true) {
		const { payload } = yield take(FETCH_PREPARATIONS);
		const defaultFolderId = 'Lw==';
		yield put(
			actions.http.get(`/api/folders/${payload.folderId || defaultFolderId}/preparations`, {
				cmf: {
					collectionId: 'preparations',
				},
				transform: PreparationService.transform,
			}),
		);
	}
}

function* openFolder() {
	while (true) {
		const { id } = yield take(OPEN_FOLDER);
		yield api.saga.putActionCreator('preparation:fetch', {
			folderId: id,
		});
	}
}

function* rename() {
	while (true) {
		const { payload } = yield take(RENAME_PREPARATION);

		yield call(
			http.put,
			`http://localhost:8888/api/preparations/${payload.id}`,
			{ name: payload.name },
			defaultHttpConfiguration,
		);
		yield call(fetch);
	}
}

function* setTitleEditionMode() {
	while (true) {
		const { payload } = yield take(SET_TITLE_EDITION_MODE);
		const preparations = yield select(state => state.cmf.collections.get('preparations'));
		const updated = preparations.update(
			preparations.findIndex(val => val.get('id') === payload),
			val => val.set('display', 'input'),
		);
		yield put(actions.collections.addOrReplace('preparations', updated));
	}
}

function* openAbout() {
	while (true) {
		yield take(OPEN_PREPARATION_CREATOR);
		yield put(actions.components.mergeState('PreparationCreatorModal', 'default', { show: true }));
	}
}

export default {
	'preparation:rename:cancel': cancelRename,
	'preparation:duplicate': duplicate,
	'preparation:fetch': fetch,
	'preparation:folder:open': openFolder,
	'preparation:rename:submit': rename,
	'preparation:rename': setTitleEditionMode,
	'preparation:about:open': openAbout,
};
