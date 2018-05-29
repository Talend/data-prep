import { takeEvery } from 'redux-saga/effects';

import { OPEN_WINDOW, REDIRECT_WINDOW } from '../constants/actions';

function openWindow(action) {
	console.log('action', action);
	if (!action || !action.payload) {
		return;
	}
	window.open(action.payload.url, '_blank');
}

function redirectWindow(action) {
	console.log('action', action);
	if (!action || !action.payload) {
		return;
	}
	window.location.assign(action.payload.url);
}

/**
 * Open new tab
 * @returns {IterableIterator<*|ForkEffect>}
 */
function* open() {
	yield takeEvery(OPEN_WINDOW, openWindow);
}

/**
 * Redirect window to
 * @returns {IterableIterator<*|ForkEffect>}
 */
function* redirect() {
	yield takeEvery(REDIRECT_WINDOW, redirectWindow);
}

export default {
	open,
	openWindow,
	redirect,
	redirectWindow,
};
