import { takeEvery } from 'redux-saga/effects';
import { OPEN_WINDOW, REDIRECT_WINDOW } from '../../constants/actions';
import * as effects from '../effects/redirect.effects';


/**
 * Open new tab
 * @returns {IterableIterator<*|ForkEffect>}
 */
function* open() {
	yield takeEvery(OPEN_WINDOW, effects.open);
}

/**
 * Redirect window to
 * @returns {IterableIterator<*|ForkEffect>}
 */
function* redirect() {
	yield takeEvery(REDIRECT_WINDOW, effects.redirect);
}

export default {
	'window:open': open,
	'window:redirect': redirect,
};
