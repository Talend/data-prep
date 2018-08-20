import { actions, sagaRouter } from '@talend/react-cmf';
import { browserHistory as history } from 'react-router';
import { call, fork, put } from 'redux-saga/effects';
import store from 'store';
import { refresh } from './preparation.effects';
/**
 * Fetch app settings
 * @returns {IterableIterator<*>}
 */
export function* bootstrap() {
	yield call(fetchSettings);
	// this should be called here because refresh use settings in the store
	yield call(initializeRouter);
}

export function* fetchSettings() {
	const data = yield store.get('settings');
	yield put(actions.collections.addOrReplace('settings', data));
}

export function* initializeRouter() {
	const routes = {
		'/preparations': { saga: refresh, runOnExactMatch: true },
		'/preparations/:folderId': { saga: refresh, runOnExactMatch: true },
	};
	yield fork(sagaRouter, history, routes);
}
