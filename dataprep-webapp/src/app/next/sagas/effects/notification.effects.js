import objectId from 'bson-objectid';
import { put, select } from 'redux-saga/effects';
import { actions } from '@talend/react-cmf';

const COMPONENT_NAME = 'Container(Notification)';
const COMPONENT_KEY = 'Notification';

export function* pushNotification(notification) {
	const path = [COMPONENT_NAME, COMPONENT_KEY, 'notifications'];
	let notifications = yield select(state => state.cmf.components.getIn(path));
	notifications = notifications.push(notification);
	yield put(actions.components.mergeState(COMPONENT_NAME, COMPONENT_KEY, { notifications }));
}

export function* success(payload) {
	const newNotification = {
		...payload,
		id: objectId(),
		type: 'info',
	};
	yield* pushNotification(newNotification);
}

export function* error(payload) {
	const newNotification = {
		...payload,
		id: objectId(),
		type: 'error',
	};
	yield* pushNotification(newNotification);
}

export function* warning(payload) {
	const newNotification = {
		...payload,
		id: objectId(),
		type: 'warning',
	};
	yield* pushNotification(newNotification);
}
