import { call, put, select } from 'redux-saga/effects';
import { actions } from '@talend/react-cmf';
import http from './http';
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
			dataSetId: payload.id || payload.datasetId || payload.model.id,
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

export function* openPreparationCreatorModal() {
	yield put(actions.components.mergeState('PreparationCreatorModal', 'default', { show: true }));
}
