import objectId from 'bson-objectid';
import { put, select } from 'redux-saga/effects';
import { actions } from '@talend/react-cmf';

export const COMPONENT_NAME = 'Container(Notification)';
export const COMPONENT_KEY = 'Notification';

export function* push(notification) {
	const path = [COMPONENT_NAME, COMPONENT_KEY, 'notifications'];
	let notifications = yield select(state => state.cmf.components.getIn(path));
	console.log(notifications);
	notifications = notifications.push(notification);
	yield put(actions.components.mergeState(COMPONENT_NAME, COMPONENT_KEY, { notifications }));
}

export function* success({ payload }) {
	const newNotification = {
		...payload,
		id: objectId(),
		type: 'info',
	};
	yield* push(newNotification);
}

export function* error({ payload }) {
	const newNotification = {
		...payload,
		id: objectId(),
		type: 'error',
	};
	yield* push(newNotification);
}

export function* warning({ payload }) {
	const newNotification = {
		...payload,
		id: objectId(),
		type: 'warning',
	};
	yield* push(newNotification);
}
