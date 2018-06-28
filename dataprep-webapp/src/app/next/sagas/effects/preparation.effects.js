import { call, put, select } from 'redux-saga/effects';
import api, { actions } from '@talend/react-cmf';
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
	yield put(
		actions.http.get(`/api/folders/${payload.folderId || defaultFolderId}/preparations`, {
			cmf: {
				collectionId: 'preparations',
			},
			transform: PreparationService.transform,
		}),
	);
}

export function* copy(payload) {
	console.log('[NC] COPY payload: ', payload);
	// POST
	// /api/preparations/064c7a0f-c089-4730-a0a8-0cfc823f4e83/
	// copy?destination=5a781bcfcff47e000b0917d4
	// &newName=20000L8C-TDP1489%20Preparations
	yield call(
		http.post,
		`/api/preparations/${payload.id}/copy?destination=${payload.destination}&newName=${payload.title}`,
		{},
	);
}

export function* move(payload) {
	console.log('[NC] MOVE payload: ', payload);
	// PUT
	// /api/preparations/064c7a0f-c089-4730-a0a8-0cfc823f4e83/
	// move?folder=5b22237fcff47e000fda8244&destination=5a781bcfcff47e000b0917d4
	// &newName=20000L8C-TDP1489%20Preparationn
	yield call(
		http.put,
		`/api/preparations/${payload.id}/move?folder=XXX&destination=${payload.destination}&newName=${payload.title}`,
		{},
	);
}

export function* fetchTree() {
	yield put(
		actions.http.get('/api/folders/tree', {
			cmf: {
				collectionId: 'folders',
			},
			transform: PreparationService.transformTree,
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

export function* openPreparationCreatorModal() {
	yield put(actions.components.mergeState('PreparationCreatorModal', 'default', { show: true }));
}

export function* openCopyMoveModal(model) {
	yield put(
		actions.components.mergeState('PreparationCopyMoveModal', 'default',
			{
				show: true,
				model,
			},
		)
	);
}

export function* closeCopyMoveModal() {
	yield put(
		actions.components.mergeState('PreparationCopyMoveModal', 'default',
			{ show: false },
		)
	);
}
