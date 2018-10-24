import { call, put, select } from 'redux-saga/effects';
import { actions } from '@talend/react-cmf';
import { ConfirmDialog } from '@talend/react-containers';
import { Map } from 'immutable';
import { refreshCurrentFolder } from './preparation.effects';
import i18next from '../../../i18n';
import http from './http';
import creators from '../../actions';
import TextService from '../../services/text.service';

export function* openAddFolderModal() {
	const state = new Map({
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
	});
	yield put(actions.components.mergeState('Translate(FolderCreatorModal)', 'default', state));
}

export function* closeAddFolderModal() {
	yield put(
		actions.components.mergeState(
			'Translate(FolderCreatorModal)',
			'default',
			new Map({ show: false }),
		),
	);
}

export function* addFolder() {
	let newFolderName = yield select(state =>
		state.cmf.components.getIn(['Translate(FolderCreatorModal)', 'default', 'name']),
	);
	newFolderName = TextService.sanitize(newFolderName);
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const currentFolderId = yield select(state => state.cmf.collections.get('currentFolderId'));

	// let action = yield select(state =>
	// 	state.cmf.components.getIn(['Translate(FolderCreatorModal)', 'default', 'validateAction']),
	// );
	// yield put(
	// 	actions.components.mergeState('Translate(FolderCreatorModal)', 'default', {
	// 		validateAction: { ...action.toJS(), inProgress: true },
	// 	}),
	// );
	const { data } = yield call(
		http.get,
		`${uris.get('apiFolders')}/${currentFolderId}/preparations`,
	);
	const existingFolder = data.folders.filter(folder => folder.name === newFolderName).length;
	if (existingFolder) {
		const error = i18next.t('tdp-app:FOLDER_EXIST_MESSAGE', {
			name: newFolderName,
		});
		yield put(actions.components.mergeState('Translate(FolderCreatorModal)', 'default', { error }));
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

	// const action = yield select(state =>
	// 	state.cmf.components.getIn(['Translate(FolderCreatorModal)', 'default', 'validateAction']),
	// );
	// yield put(
	// 	actions.components.mergeState('Translate(FolderCreatorModal)', 'default', {
	// 		validateAction: { ...action.toJS(), inProgress: false },
	// 	}),
	// );
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

	yield select(state => ConfirmDialog.setDialogLoadingMode(state, true));

	// yield put(
	// 	actions.components.mergeState(
	// 		'CMFContainer(ConfirmDialog)',
	// 		'ConfirmDialog',
	// 		new Map({ loading: true }),
	// 	),
	// );
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
