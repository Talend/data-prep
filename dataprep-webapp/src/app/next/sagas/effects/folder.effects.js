import { call, put, select } from 'redux-saga/effects';
import { actions } from '@talend/react-cmf';
import { Map } from 'immutable';
import { refreshCurrentFolder } from './preparation.effects';
import i18next from '../../../i18n';
import http from './http';
import creators from '../../actions';
import TextService from '../../services/text.service';
import {
	hide as hideFolderAddModal,
	setError as setFolderAddModalError,
} from '../../components/FolderCreatorModal/actions';


export function* addFolder() {
	let name = yield select(state =>
		state.cmf.components.getIn(['Translate(FolderCreatorModal)', 'default', 'name']),
	);
	name = TextService.sanitize(name);
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const currentFolderId = yield select(state => state.cmf.collections.get('currentFolderId'));

	const { data } = yield call(
		http.get,
		`${uris.get('apiFolders')}/${currentFolderId}/preparations`,
	);

	if (data.folders.find(folder => folder.name === name)) {
		yield put(setFolderAddModalError(
			null,
			i18next.t('tdp-app:FOLDER_EXIST_MESSAGE', { name }),
		));
	}
	else {
		const { response } = yield call(
			http.put,
			`${uris.get('apiFolders')}?parentId=${currentFolderId}&path=${name}`,
		);
		if (response.ok) {
			yield call(refreshCurrentFolder);
			yield put(
				creators.notification.success(null, {
					title: i18next.t('tdp-app:FOLDER_ADD_NOTIFICATION_TITLE'),
					message: i18next.t('tdp-app:FOLDER_ADD_NOTIFICATION_MESSAGE', { name }),
				}),
			);
		}
		yield put(hideFolderAddModal());
	}
}

export function* openRemoveFolderModal(payload) {
	const state = new Map({
		header: i18next.t('tdp-app:REMOVE_FOLDER_MODAL_HEADER'),
		children: i18next.t('tdp-app:REMOVE_FOLDER_MODAL_CONTENT', {
			name: payload.name,
		}),
		show: true,
		validateAction: 'folder:remove',
		cancelAction: 'folder:remove:close',
		folderId: payload.id,
		folderName: payload.name,
	});
	yield put(actions.components.mergeState('CMFContainer(ConfirmDialog)', 'ConfirmDialog', state));
}

export function* closeRemoveFolderModal() {
	yield put(
		actions.components.mergeState(
			'CMFContainer(ConfirmDialog)',
			'ConfirmDialog',
			new Map({ show: false }),
		),
	);
}

export function* removeFolder() {
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const folderId = yield select(state =>
		state.cmf.components.getIn(['CMFContainer(ConfirmDialog)', 'ConfirmDialog', 'folderId']),
	);

	yield put(
		actions.components.mergeState(
			'CMFContainer(ConfirmDialog)',
			'ConfirmDialog',
			new Map({ loading: true }),
		),
	);
	const { response } = yield call(http.delete, `${uris.get('apiFolders')}/${folderId}`);
	if (response.ok) {
		yield call(refreshCurrentFolder);
		const folderName = yield select(state =>
			state.cmf.components.getIn(['CMFContainer(ConfirmDialog)', 'ConfirmDialog', 'folderName']),
		);
		yield put(
			creators.notification.success(null, {
				title: i18next.t('tdp-app:FOLDER_REMOVE_NOTIFICATION_TITLE'),
				message: i18next.t('tdp-app:FOLDER_REMOVE_NOTIFICATION_MESSAGE', {
					name: folderName,
				}),
			}),
		);
	}
	yield call(closeRemoveFolderModal);
	yield put(
		actions.components.mergeState(
			'CMFContainer(ConfirmDialog)',
			'ConfirmDialog',
			new Map({ loading: false }),
		),
	);
}
