import { call, put, select } from 'redux-saga/effects';
import api, { actions } from '@talend/react-cmf';
import { Map } from 'immutable';

import http from './http';
import PreparationService from '../../services/preparation.service';
import PreparationCopyMoveModal from '../../components/PreparationCopyMoveModal';

export function* cancelRename(payload) {
	const preparations = yield select(state => state.cmf.collections.get('preparations'));
	const updated = preparations.update(
		preparations.findIndex(val => val.get('id') === payload),
		val => val.set('display', 'text'),
	);
	yield put(actions.collections.addOrReplace('preparations', updated));
}

export function* fetch(payload) {
	const defaultFolderId = 'Lw==';
	const folderId = payload.folderId || defaultFolderId;
	yield put(actions.collections.addOrReplace('currentFolderId', folderId));
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const { data } = yield call(http.get, `${uris.get('apiFolders')}/${folderId}/preparations`);
	yield put(actions.collections.addOrReplace('preparations', PreparationService.transform(data)));
}

function* setCopyMoveErrorMode(message) {
	yield put(
			actions.components.mergeState(
				'PreparationCopyMoveModal',
				'default',
				{ error: message },
			),
		);
	yield put(
		actions.components.mergeState(
			'Container(EditableText)',
			PreparationCopyMoveModal.EDITABLE_TEXT_ID,
			{ editMode: true }
		),
	);
}

export function* copy({ id, folderId, destination, title }) {
	const dest = destination || folderId;
	const url = `/api/preparations/${id}/copy?destination=${dest}&newName=${title}`;

	const action = yield call(http.post, url, {}, {}, { silent: true });
	if (action instanceof Error && action.data) {
		yield setCopyMoveErrorMode(action.data.message);
	}
	else {
		yield call(fetch, { folderId });
		yield call(closeCopyMoveModal);
	}
}

export function* move({ id, folderId, destination, title }) {
	const dest = destination || folderId;
	const url = `/api/preparations/${id}/move?folder=${folderId}&destination=${dest}&newName=${title}`;

	const action = yield call(http.put, url, {}, {}, { silent: true });
	if (action instanceof Error && action.data) {
		yield setCopyMoveErrorMode(action.data.message);
	}
	else {
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

export function* openCopyMoveModal(model, action) {
	const folderId = yield select(state => state.cmf.collections.get('currentFolderId'));
	yield put(
		actions.components.mergeState('PreparationCopyMoveModal', 'default', {
			action,
			show: true,
			error: null,
			name: model.name,
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

export function* fetchFolder(payload) {
	const defaultFolderId = 'Lw==';
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const { data } = yield call(http.get, `${uris.get('apiFolders')}/${(payload.folderId || defaultFolderId)}`);
	yield put(actions.components.mergeState('Breadcrumbs', 'default', new Map({
		items: PreparationService.transformFolder(data),
		maxItems: 5,
	})));
}
