import { actions } from '@talend/react-cmf';
import { put } from 'redux-saga/effects';
import store from 'store';

/**
 * Fetch app settings
 * @returns {IterableIterator<*>}
 */
export function* fetch() {
	const data = yield store.get('settings');
	yield put(actions.collections.addOrReplace('settings', data));
}
