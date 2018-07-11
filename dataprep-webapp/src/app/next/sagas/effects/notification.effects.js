import objectId from 'bson-objectid';
import { put, select } from 'redux-saga/effects';
import { actions } from '@talend/react-cmf';


export function* success(payload) {
	const path = ['Container(Notification)', 'Notification', 'notifications'];
	let notifications = yield select(state => state.cmf.components.getIn(path));
	const newNotif = {
		...payload,
		id: objectId(),
		type: 'info',
	};
	notifications = notifications.push(newNotif);
	yield put(actions.components.mergeState('Container(Notification)', 'Notification', { notifications }));
}

export function* error(payload) {
	const path = ['Container(Notification)', 'Notification', 'notifications'];
	let notifications = yield select(state => state.cmf.components.getIn(path));
	const newNotif = {
		...payload,
		id: objectId(),
		type: 'info',
	};
	notifications = notifications.push(newNotif);
	yield put(actions.components.mergeState('Container(Notification)', 'Notification', { notifications }));
}

export function* warning(payload) {
	const path = ['Container(Notification)', 'Notification', 'notifications'];
	let notifications = yield select(state => state.cmf.components.getIn(path));
	const newNotif = {
		...payload,
		id: objectId(),
		type: 'info',
	};
	notifications = notifications.push(newNotif);
	yield put(actions.components.mergeState('Container(Notification)', 'Notification', { notifications }));
}
