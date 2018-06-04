import { call, put, select } from 'redux-saga/effects';
import api, { actions } from '@talend/react-cmf';
import http from './http';
import PreparationService from '../../services/preparation.service';

const defaultHttpConfiguration = {
	headers: {
		Accept: 'application/json, text/plain, */*',
		'Content-Type': 'application/json',
	},
};


export function* cancelRename(payload) {
	const preparations = yield select(state => state.cmf.collections.get('preparations'));
	const updated = preparations.update(
		preparations.findIndex(val => val.get('id') === payload),
		val => val.set('display', 'text'),
	);
	yield put(actions.collections.addOrReplace('preparations', updated));
}

export function* duplicate(prep) {
	const newName = `test${Math.random()}`;

	yield call(
		http.post,
		`/api/preparations/${prep.payload.id}/copy?destination=Lw==&newName=${newName}`,
		{},
		defaultHttpConfiguration,
	);
	yield call(fetch);
}

export function* fetch(payload) {
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

export function* openFolder(id) {
	yield api.saga.putActionCreator('preparation:fetch', {
		folderId: id,
	});
}

export function* rename(payload) {
	yield call(
		http.put,
		`/api/preparations/${payload.id}`,
		{ name: payload.name },
		defaultHttpConfiguration,
	);
	yield call(fetch);
}

export function* setTitleEditionMode(payload) {
	const preparations = yield select(state => state.cmf.collections.get('preparations'));
	const updated = preparations.update(
		preparations.findIndex(val => val.get('id') === payload),
		val => val.set('display', 'input'),
	);
	yield put(actions.collections.addOrReplace('preparations', updated));
}

export function* openAbout() {
	yield put(actions.components.mergeState('PreparationCreatorModal', 'default', { show: true }));
}
