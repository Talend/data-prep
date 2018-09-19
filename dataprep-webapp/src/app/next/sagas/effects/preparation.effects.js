import { all, call, put, select } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import { actions } from '@talend/react-cmf';
import { Map } from 'immutable';

import preparationWatcher from './../watchers/preparation.saga';
import i18next from '../../../i18n';

import http from './http';
import creators from '../../actions';
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
	const folderId = (payload && payload.folderId) || defaultFolderId;
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
				title: i18next.t('tdp-app:PREPARATION_COPY_NOTIFICATION_TITLE', {
					defaultValue: 'Preparation copied',
				}),
				message: i18next.t('tdp-app:PREPARATION_COPY_NOTIFICATION_MESSAGE', {
					defaultValue: 'The preparation has been copied.',
				}),
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
				title: i18next.t('tdp-app:PREPARATION_MOVE_NOTIFICATION_TITLE', {
					defaultValue: 'Preparation moved',
				}),
				message: i18next.t('tdp-app:PREPARATION_MOVE_NOTIFICATION_MESSAGE', {
					defaultValue: 'The preparation has been moved.',
				}),
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
	yield put(actions.components.mergeState('CMFContainer(ConfirmDialog)', 'ConfirmDialog', new Map({ show: false })));
}

export function* removeFolder() {
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const folderId = yield select(state => state.cmf.components.getIn(['CMFContainer(ConfirmDialog)', 'ConfirmDialog', 'folderId']));
	const { response } = yield call(http.delete, `${uris.get('apiFolders')}/${folderId}`);
	if (response.ok) {
		yield call(refreshCurrentFolder);
		const folderName = yield select(state => state.cmf.components.getIn(['CMFContainer(ConfirmDialog)', 'ConfirmDialog', 'folderName']));
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
	yield call(delay, 500); // necessary when multiple quick calls
	yield call(preparationWatcher['preparation:folder:remove']);
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
	yield put(actions.components.mergeState('FolderCreatorModal', 'add_folder_modal', new Map({ show: false })));
}

export function* addFolder() {
	let newFolderName = yield select(state =>
		state.cmf.components.getIn(['FolderCreatorModal', 'add_folder_modal', 'name']),
	);
	newFolderName = newFolderName.trim();
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
	const defaultFolderId = 'Lw==';
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	const { data } = yield call(
		http.get,
		`${uris.get('apiFolders')}/${(payload && payload.folderId) || defaultFolderId}`,
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
