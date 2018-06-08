import { call, cancel, fork, take, takeLatest } from 'redux-saga/effects';
import { SEARCH, SEARCH_SELECT, SEARCH_RESET } from '../../constants/actions';
import * as effects from '../effects/search.effects';


function* goto() {
	while (true) {
		const { payload } = yield take(SEARCH_SELECT);
		yield call(effects.goto, payload);
	}
}

function* reset() {
	yield takeLatest(SEARCH_RESET, effects.reset);
}

function* search() {
	let task;
	while (true) {
		const { payload } = yield take(SEARCH);

		if (task) {
			yield cancel(task);
		}

		task = yield fork(effects.search, payload);
	}
}

export default {
	'search:process': search,
	'search:reset': reset,
	'search:goto': goto,
};
