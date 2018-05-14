import { call, take, put, select } from 'redux-saga/effects';
import http from '@talend/react-cmf/lib/sagas/http';
import { api, actions } from '@talend/react-cmf';
import {
	CANCEL_RENAME_PREPARATION,
	FETCH_PREPARATIONS,
	OPEN_PREPARATION_CREATOR,
	PREPARATION_DUPLICATE,
	RENAME_PREPARATION,
	SET_TITLE_EDITION_MODE,
} from '../constants';

const defaultHttpConfiguration = {
	headers: {
		Accept: 'application/json, text/plain, */*',
		'Content-Type': 'application/json',
	},
};

function* cancelRename() {
	while (true) {
		const { payload } = yield take(CANCEL_RENAME_PREPARATION);
		const preparations = yield select(state => state.cmf.collections.get('preparations'));
		const updated = preparations.update(
			preparations.findIndex(val => val.get('id') === payload),
			val => val.set('display', 'text'),
		);
		yield put(actions.collections.addOrReplace('preparations', updated));
	}
}

function* duplicate() {
	while (true) {
		const prep = yield take(PREPARATION_DUPLICATE);
		const newName = `test${Math.random()}`;

		yield call(
			http.post,
			`http://localhost:8888/api/preparations/${prep.payload.id}/copy?destination=Lw==&newName=${newName}`,
			{},
			defaultHttpConfiguration,
		);
		yield api.saga.putActionCreator('preparation:fetchAll');
	}
}

function* fetchPreparations() {
	while (true) {
		const { folderId = 'Lw==' } = yield take(FETCH_PREPARATIONS);
		yield put(
			actions.http.get(`http://localhost:8888/api/folders/${folderId}/preparations`, {
				cmf: {
					collectionId: 'preparations',
				},
				transform({ folders, preparations }) {
					const adaptedFolders = folders.map(folder => ({
						author: folder.ownerId,
						className: 'list-item-folder',
						icon: 'talend-folder',
						id: folder.id,
						name: folder.name,
						type: 'folder',
					}));
					const adaptedPreparations = preparations.map(prep => ({
						author: prep.author,
						className: 'list-item-preparation',
						datasetName: prep.dataset.dataSetName,
						icon: 'talend-dataprep',
						id: prep.id,
						name: prep.name,
						nbSteps: prep.steps.length - 1,
						type: 'preparation',
					}));

					return adaptedFolders.concat(adaptedPreparations);
				},
			}),
		);
	}
}

function* rename() {
	while (true) {
		const { payload } = yield take(RENAME_PREPARATION);

		yield call(
			http.put,
			`http://localhost:8888/api/preparations/${payload.id}`,
			{ name: payload.name },
			defaultHttpConfiguration,
		);
		yield api.saga.putActionCreator('preparation:fetchAll');
	}
}

function* setTitleEditionMode() {
	while (true) {
		const { payload } = yield take(SET_TITLE_EDITION_MODE);
		const preparations = yield select(state => state.cmf.collections.get('preparations'));
		const updated = preparations.update(
			preparations.findIndex(val => val.get('id') === payload),
			val => val.set('display', 'input'),
		);
		yield put(actions.collections.addOrReplace('preparations', updated));
	}
}

function* openAbout() {
	while (true) {
		yield take(OPEN_PREPARATION_CREATOR);
		yield put(actions.components.mergeState('PreparationCreatorModal', 'default', { show: true }));
	}
}

export default {
	cancelRename,
	duplicate,
	fetchPreparations,
	rename,
	setTitleEditionMode,
	openAbout,
};
