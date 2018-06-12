import { takeLatest } from 'redux-saga/effects';
import * as effects from '../effects/http.effects';


function* handler() {
	yield takeLatest('@@HTTP/ERRORS', effects.handle);
}

export default {
	'http:errors:handler': handler,
};
