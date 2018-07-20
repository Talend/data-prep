import objectId from 'bson-objectid';
import { put, select } from 'redux-saga/effects';
import { actions } from '@talend/react-cmf';

export const COMPONENT_NAME = 'Container(Notification)';
export const COMPONENT_KEY = 'Notification';

export function* push(notification) {
	const path = [COMPONENT_NAME, COMPONENT_KEY, 'notifications'];
	const notifications = yield select(state => state.cmf.components.getIn(path));
	yield put(actions.components.mergeState(COMPONENT_NAME, COMPONENT_KEY,
		{ notifications: notifications.push(notification) }
	));
}

export function* success({ payload }) {
	yield* push({
		...payload,
		id: objectId(),
		type: 'info',
	});
}

export function* error({ payload }) {
	yield* push({
		...payload,
		id: objectId(),
		type: 'error',
	});
}

export function* warning({ payload }) {
	yield* push({
		...payload,
		id: objectId(),
		type: 'warning',
	});
}
