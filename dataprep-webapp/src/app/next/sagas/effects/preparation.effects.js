import { all, call, put, select } from 'redux-saga/effects';
import { actions } from '@talend/react-cmf';
import { Map } from 'immutable';
import i18next from '../../../i18n';
import http from './http';
import creators from '../../actions';
import PreparationService from '../../services/preparation.service';
import PreparationCopyMoveModal from '../../components/PreparationCopyMoveModal';
import { REDIRECT_WINDOW } from '../../constants/actions';

export const DEFAULT_FOLDER_ID = 'Lw==';

export function* cancelRename(payload) {
	const preparations = yield select(state => state.cmf.collections.get('preparations'));
	const updated = preparations.update(
		preparations.findIndex(val => val.get('id') === payload),
		val => val.set('display', 'text'),
	);
	yield put(actions.collections.addOrReplace('preparations', updated));
}

export function* create(payload) {
	const folderId = (payload && payload.folderId) || DEFAULT_FOLDER_ID;
	yield put(actions.collections.addOrReplace('currentFolderId', folderId));
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const response = yield call(
		http.post,
		`${uris.get('apiPreparations')}?folder=${folderId}`,
		{
			dataSetId: payload.id || payload.model.id,
		},
	);
	if (!(response instanceof Error)) {
		const preparationId = response.data;
		yield put({
			type: REDIRECT_WINDOW,
			payload: { url: `/#/playground/preparation?prepid=${preparationId}` },
		});
	}
}

export function* fetch(payload) {
	const folderId = (payload && payload.folderId) || DEFAULT_FOLDER_ID;
	yield put(actions.collections.addOrReplace('currentFolderId', folderId));
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const { data } = yield call(http.get, `${uris.get('apiFolders')}/${folderId}/preparations`);
	yield put(actions.collections.addOrReplace('preparations', PreparationService.transform(data)));
}

export function* setCopyMoveErrorMode(message) {
	yield put(
		actions.components.mergeState(PreparationCopyMoveModal.DISPLAY_NAME, 'default', {
			error: message,
		}),
	);
	yield put(
		actions.components.mergeState(
			'Container(EditableText)',
			PreparationCopyMoveModal.EDITABLE_TEXT_ID,
			{ editMode: true },
		),
	);
}

export function* copy({ id, folderId, destination, title }) {
	const dest = destination || folderId;
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const action = yield call(
		http.post,
		`${uris.get('apiPreparations')}/${id}/copy?destination=${dest}&newName=${title}`,
		{},
		{},
		{ silent: true },
	);

	if (action instanceof Error && action.data) {
		yield call(setCopyMoveErrorMode, action.data.message);
	}
	else {
		yield call(fetch, { folderId });
		yield call(closeCopyMoveModal);
		yield put(
			creators.notification.success(null, {
				title: i18next.t('tdp-app:PREPARATION_COPY_NOTIFICATION_TITLE'),
				message: i18next.t('tdp-app:PREPARATION_COPY_NOTIFICATION_MESSAGE'),
			}),
		);
	}
}

export function* move({ id, folderId, destination, title }) {
	const dest = destination || folderId;
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const action = yield call(
		http.put,
		`${uris.get(
			'apiPreparations',
		)}/${id}/move?folder=${folderId}&destination=${dest}&newName=${title}`,
		{},
		{},
		{ silent: true },
	);

	if (action instanceof Error && action.data) {
		yield call(setCopyMoveErrorMode, action.data.message);
	}
	else {
		yield call(fetch, { folderId });
		yield call(closeCopyMoveModal);
		yield put(
			creators.notification.success(null, {
				title: i18next.t('tdp-app:PREPARATION_MOVE_NOTIFICATION_TITLE'),
				message: i18next.t('tdp-app:PREPARATION_MOVE_NOTIFICATION_MESSAGE'),
			}),
		);
	}
}

export function* fetchTree() {
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));

	yield put(
		actions.http.get(`${uris.get('apiFolders')}/tree`, {
			cmf: {
				collectionId: 'folders',
			},
			transform: PreparationService.transformTree,
		}),
	);
}

export function* refresh(payload) {
	yield all([
		call(fetch, payload),
		call(fetchFolder, payload),
	]);
}

export function* refreshCurrentFolder() {
	const currentFolderId = yield select(state => state.cmf.collections.get('currentFolderId'));
	yield call(refresh, { folderId: currentFolderId });
}

export function* rename(payload) {
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	yield call(http.put, `${uris.get('apiPreparations')}/${payload.id}`, { name: payload.name });
	yield call(refreshCurrentFolder);
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
		actions.components.mergeState(PreparationCopyMoveModal.DISPLAY_NAME, 'default', {
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
	yield put(
		actions.components.mergeState(PreparationCopyMoveModal.DISPLAY_NAME, 'default', {
			show: false,
		}),
	);
}

export function* fetchFolder(payload) {
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const { data } = yield call(
		http.get,
		`${uris.get('apiFolders')}/${(payload && payload.folderId) || DEFAULT_FOLDER_ID}`,
	);
	yield put(
		actions.components.mergeState(
			'Breadcrumbs',
			'default',
			new Map({
				items: PreparationService.transformFolder(data),
				maxItems: 5,
			}),
		),
	);
}
