import { call, put, select } from 'redux-saga/effects';
import { actions } from '@talend/react-cmf';
import { Map } from 'immutable';
import { refreshCurrentFolder } from './preparation.effects';
import i18next from '../../../i18n';
import http from './http';
import creators from '../../actions';
import TextService from '../../services/text.service';

export function* openAddFolderModal() {
	const state = new Map({
		header: i18next.t('tdp-app:ADD_FOLDER_HEADER'),
		show: true,
		name: '',
		error: '',
		validateAction: {
			label: i18next.t('tdp-app:ADD'),
			id: 'folder:add',
			disabled: true,
			bsStyle: 'primary',
			actionCreator: 'folder:add',
		},
		cancelAction: {
			label: i18next.t('tdp-app:CANCEL'),
			id: 'folder:add:close',
			bsStyle: 'default btn-inverse',
			actionCreator: 'folder:add:close',
		},
	});
	yield put(actions.components.mergeState('FolderCreatorModal', 'add:folder:modal', state));
}

export function* closeAddFolderModal() {
	yield put(
		actions.components.mergeState(
			'FolderCreatorModal',
			'add:folder:modal',
			new Map({ show: false }),
		),
	);
}

export function* addFolder() {
	let newFolderName = yield select(state =>
		state.cmf.components.getIn(['FolderCreatorModal', 'add:folder:modal', 'name']),
	);
	newFolderName = TextService.sanitize(newFolderName);
	if (!newFolderName.length) {
		const error = i18next.t('tdp-app:FOLDER_EMPTY_MESSAGE');
		yield put(actions.components.mergeState('FolderCreatorModal', 'add:folder:modal', { error }));
	}
	else {
		const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
		const currentFolderId = yield select(state => state.cmf.collections.get('currentFolderId'));

		let action = yield select(state =>
			state.cmf.components.getIn(['FolderCreatorModal', 'add:folder:modal', 'validateAction']),
		);
		yield put(
			actions.components.mergeState('FolderCreatorModal', 'add:folder:modal', {
				validateAction: { ...action.toJS(), inProgress: true },
			}),
		);
		const { data } = yield call(
			http.get,
			`${uris.get('apiFolders')}/${currentFolderId}/preparations`,
		);
		const existingFolder = data.folders.filter(folder => folder.name === newFolderName).length;
		if (existingFolder) {
			const error = i18next.t('tdp-app:FOLDER_EXIST_MESSAGE', {
				name: newFolderName,
			});
			yield put(actions.components.mergeState('FolderCreatorModal', 'add:folder:modal', { error }));
		}
		else {
			const { response } = yield call(
				http.put,
				`${uris.get('apiFolders')}?parentId=${currentFolderId}&path=${newFolderName}`,
			);
			if (response.ok) {
				yield call(refreshCurrentFolder);
				yield put(
					creators.notification.success(null, {
						title: i18next.t('tdp-app:FOLDER_ADD_NOTIFICATION_TITLE'),
						message: i18next.t('tdp-app:FOLDER_ADD_NOTIFICATION_MESSAGE', {
							name: newFolderName,
						}),
					}),
				);
			}
			yield call(closeAddFolderModal);
		}

		action = yield select(state =>
			state.cmf.components.getIn(['FolderCreatorModal', 'add:folder:modal', 'validateAction']),
		);
		yield put(
			actions.components.mergeState('FolderCreatorModal', 'add:folder:modal', {
				validateAction: { ...action.toJS(), inProgress: false },
			}),
		);
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
