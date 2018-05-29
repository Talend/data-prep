import { takeEvery } from 'redux-saga/effects';

import { OPEN_WINDOW, REDIRECT_WINDOW } from '../constants/actions';

function openWindow(action) {
	if (action.payload) {
		window.open(action.payload.url, '_blank');
	}
}

function redirectWindow(action) {
	if (action.payload) {
		window.location.assign(action.payload.url);
	}
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
