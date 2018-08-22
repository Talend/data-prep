import { actions } from '@talend/react-cmf';
import { put } from 'redux-saga/effects';
import localeStorage from 'store';
import i18n from './../../../i18n';

/**
 * Fetch app settings
 * @returns {IterableIterator<*>}
 */
export function* fetch() {
	const data = yield localeStorage.get('settings');
	yield put(actions.collections.addOrReplace('settings', data));
}

/**
 * Change locale to preferred one
 */
export function setLanguage() {
	const data = localeStorage.get('settings');
	i18n.changeLanguage(data.context.language);
}
