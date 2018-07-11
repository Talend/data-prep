import { take, call } from 'redux-saga/effects';
import { ERROR_NOTIFICATION, SUCCESS_NOTIFICATION, WARNING_NOTIFICATION } from '../../constants/actions';
import * as effects from '../effects/notification.effects';


function* success() {
	while (true) {
		const { payload } = yield take(SUCCESS_NOTIFICATION);
		yield call(effects.success, payload);
	}
}

function* error() {
	while (true) {
		const { payload } = yield take(ERROR_NOTIFICATION);
		yield call(effects.error, payload);
	}
}

function* warning() {
	while (true) {
		const { payload } = yield take(WARNING_NOTIFICATION);
		yield call(effects.warning, payload);
	}
}

export default {
	'notification:error': error,
	'notification:warning': warning,
	'notification:success': success,
};
