import { actions } from '@talend/react-cmf';
import { call, put } from 'redux-saga/effects';
import http from './http';

/**
 * Fetch app settings
 * @returns {IterableIterator<*>}
 */
function* fetchSettings() {
	const { data } = yield call(http.get, '/api/settings');
	yield put(actions.collections.addOrReplace('settings', data));
}

export function* fetchAll() {
	yield call(fetchSettings);
}

export default {
	bootstrap: fetchAll,
};
