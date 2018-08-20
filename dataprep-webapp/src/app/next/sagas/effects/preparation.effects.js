import { call, put, select } from 'redux-saga/effects';
import api, { actions } from '@talend/react-cmf';
import { Map } from 'immutable';

import http from './http';
import PreparationService from '../../services/preparation.service';


export function* cancelRename(payload) {
	const preparations = yield select(state => state.cmf.collections.get('preparations'));
	const updated = preparations.update(
		preparations.findIndex(val => val.get('id') === payload),
		val => val.set('display', 'text'),
	);
	yield put(actions.collections.addOrReplace('preparations', updated));
}

export function* duplicate(prep) {
	// FIXME: generate unique names
	const newName = `test${Math.random()}`;

	yield call(
		http.post,
		`/api/preparations/${prep.payload.id}/copy?destination=Lw==&newName=${newName}`,
		{},
	);
	yield call(fetch);
}

export function* fetch(payload) {
	const defaultFolderId = 'Lw==';
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const { data } = yield call(http.get, `${uris.get('apiFolders')}/${(payload.folderId || defaultFolderId)}/preparations`);
	yield put(actions.collections.addOrReplace('preparations', PreparationService.transform(data)));
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

export function* fetchFolder(payload) {
	const defaultFolderId = 'Lw==';
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const { data } = yield call(http.get, `${uris.get('apiFolders')}/${(payload.folderId || defaultFolderId)}`);
	yield put(actions.components.mergeState('Breadcrumbs', 'default', new Map({
		items: PreparationService.transformFolder(data),
		maxItems: 5,
	})));
}
