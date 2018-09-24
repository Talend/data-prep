import { actions, sagaRouter } from '@talend/react-cmf';
import { browserHistory as history } from 'react-router';
import { call, fork, put } from 'redux-saga/effects';
import localeStorage from 'store';
import { refresh } from './preparation.effects';
import i18n from './../../../i18n';

/**
 * Fetch app settings
 * @returns {IterableIterator<*>}
 */
export function* bootstrap() {
	yield call(fetchSettings);
	yield call(setLanguage);
	// this should be called here because refresh use settings in the store
	yield call(initializeRouter);
}

export function* fetchSettings() {
	const data = yield localeStorage.get('settings');
	yield put(actions.collections.addOrReplace('settings', data));
}

export function* initializeRouter() {
	const routes = {
		'/preparations': { saga: refresh, runOnExactMatch: true },
		'/preparations/:folderId': { saga: refresh, runOnExactMatch: true },
	};
	yield fork(sagaRouter, history, routes);
}

/**
 * Change locale to preferred one
 */
export function setLanguage() {
	const data = localeStorage.get('settings');
	i18n.changeLanguage(data.context.language);
}
