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

export function* fetch({ folderId = 'Lw==' }) {
	yield put(actions.collections.addOrReplace('currentFolderId', folderId));
	yield put(
		actions.http.get(`/api/folders/${folderId}/preparations`, {
			cmf: {
				collectionId: 'preparations',
			},
			transform: PreparationService.transform,
		}),
	);
}

export function* copy({ id, folderId, destination, title }) {
	const dest = destination || folderId;
	const url = `/api/preparations/${id}/copy?destination=${dest}&newName=${title}`;

	const action = yield call(http.post, url);
	if (action instanceof Error) {
		yield put(
			actions.components.mergeState('PreparationCopyMoveModal', 'default', {
				error: action.message,
			}),
		);
	}
	else {
		yield call(fetch, { folderId });
		yield call(closeCopyMoveModal);
	}
}

export function* move({ id, folderId, destination, title }) {
	const dest = destination || folderId;
	const url = `/api/preparations/${id}/move?folder=${folderId}&destination=${dest}&newName=${title}`;

	const action = yield call(http.put, url);
	if (!(action instanceof Error)) {
		yield call(fetch, { folderId });
		yield call(closeCopyMoveModal);
	}
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
	yield call(http.put, `/api/preparations/${payload.id}`, { name: payload.name });
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

export function* openCopyModal(model) {
	const folderId = yield select(state => state.cmf.collections.get('currentFolderId'));
	yield put(
		actions.components.mergeState('PreparationCopyMoveModal', 'default', {
			show: true,
			model: {
				...model,
				folderId,
			},
		}),
	);
}

export function* openCopyMoveModal(model, action) {
	const folderId = yield select(state => state.cmf.collections.get('currentFolderId'));
	yield put(
		actions.components.mergeState('PreparationCopyMoveModal', 'default', {
			show: true,
			action,
			model: {
				...model,
				folderId,
			},
		}),
	);
}

export function* closeCopyMoveModal() {
	yield put(actions.components.mergeState('PreparationCopyMoveModal', 'default', { show: false }));
}
