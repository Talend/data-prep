import { actions } from '@talend/react-cmf';
import { call, put } from 'redux-saga/effects';
import store from 'store';

/**
 * Fetch app settings
 * @returns {IterableIterator<*>}
 */
function* fetchSettings() {
	const data = yield store.get('settings');
	yield put(actions.collections.addOrReplace('settings', data));
}

export function* fetchAll() {
	yield call(fetchSettings);
}

export default {
	bootstrap: fetchAll,
};
