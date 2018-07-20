import { takeEvery } from 'redux-saga/effects';
import { ERROR_NOTIFICATION, SUCCESS_NOTIFICATION, WARNING_NOTIFICATION } from '../../constants/actions';
import * as effects from '../effects/notification.effects';

function* success() {
	yield takeEvery(SUCCESS_NOTIFICATION, effects.success);
}

function* error() {
	yield takeEvery(ERROR_NOTIFICATION, effects.error);
}

function* warning() {
	yield takeEvery(WARNING_NOTIFICATION, effects.warning);
}

export default {
	'notification:error': error,
	'notification:warning': warning,
	'notification:success': success,
};
