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
		header: i18next.t('tdp-app:ADD_FOLDER_HEADER', {
			defaultValue: 'Add a folder',
		}),
		show: true,
		name: '',
		error: '',
		validateAction: {
			label: i18next.t('tdp-app:ADD', {
				defaultValue: 'Add',
			}),
			id: 'folder:add',
			disabled: true,
			bsStyle: 'primary',
			actionCreator: 'folder:add',
		},
		cancelAction: {
			label: i18next.t('tdp-app:CANCEL', {
				defaultValue: 'Cancel',
			}),
			id: 'folder:add:close',
			bsStyle: 'default btn-inverse',
			actionCreator: 'folder:add:close',
		},
	});
	yield put(actions.components.mergeState('FolderCreatorModal', 'add_folder_modal', state));
}

export function* closeAddFolderModal() {
	yield put(
		actions.components.mergeState(
			'FolderCreatorModal',
			'add_folder_modal',
			new Map({ show: false }),
		),
	);
}

export function* addFolder() {
	let newFolderName = yield select(state =>
		state.cmf.components.getIn(['FolderCreatorModal', 'add_folder_modal', 'name']),
	);
	newFolderName = TextService.sanitize(newFolderName);
	if (!newFolderName.length) {
		const error = i18next.t('tdp-app:FOLDER_EMPTY_MESSAGE', {
			defaultValue: 'Folder name is empty',
		});
		yield put(actions.components.mergeState('FolderCreatorModal', 'add_folder_modal', { error }));
	}
	else {
		const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
		const currentFolderId = yield select(state => state.cmf.collections.get('currentFolderId'));
		const { data } = yield call(
			http.get,
			`${uris.get('apiFolders')}/${currentFolderId}/preparations`,
		);
		const existingFolder = data.folders.filter(folder => folder.name === newFolderName).length;
		if (existingFolder) {
			const error = i18next.t('tdp-app:FOLDER_EXIST_MESSAGE', {
				name: newFolderName,
				defaultValue: 'Folder exists already',
			});
			yield put(actions.components.mergeState('FolderCreatorModal', 'add_folder_modal', { error }));
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
						title: i18next.t('tdp-app:FOLDER_ADD_NOTIFICATION_TITLE', {
							defaultValue: 'Folder Added',
						}),
						message: i18next.t('tdp-app:FOLDER_ADD_NOTIFICATION_MESSAGE', {
							defaultValue: `The folder "${newFolderName}" has been added.`,
							name: newFolderName,
						}),
					}),
				);
			}
			yield call(closeAddFolderModal);
		}
	}
}

export function* openRemoveFolderModal(payload) {
	const message = i18next.t('tdp-app:REMOVE_FOLDER_MODAL_CONTENT', {
		name: payload.name,
	});
	const state = new Map({
		header: i18next.t('tdp-app:REMOVE_FOLDER_MODAL_HEADER', {
			defaultValue: 'Remove a folder',
		}),
		show: true,
		children: message,
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
	const { response } = yield call(http.delete, `${uris.get('apiFolders')}/${folderId}`);
	if (response.ok) {
		yield call(refreshCurrentFolder);
		const folderName = yield select(state =>
			state.cmf.components.getIn(['CMFContainer(ConfirmDialog)', 'ConfirmDialog', 'folderName']),
		);
		yield put(
			creators.notification.success(null, {
				title: i18next.t('tdp-app:FOLDER_REMOVE_NOTIFICATION_TITLE', {
					defaultValue: 'Folder Remove',
				}),
				message: i18next.t('tdp-app:FOLDER_REMOVE_NOTIFICATION_MESSAGE', {
					name: folderName,
					defaultValue: `The folder ${folderName} has been removed.`,
				}),
			}),
		);
	}
	yield call(closeRemoveFolderModal);
}
