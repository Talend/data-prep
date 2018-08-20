import { actions } from '@talend/react-cmf';
import { put, select, call } from 'redux-saga/effects';

import http from './http';


export function* open() {
	const versions = yield select(state => state.cmf.collections.getIn(['versions']));
	if (!versions) {
		const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
		const { data } = yield call(http.get, uris.get('apiVersion'));
		yield put(actions.collections.addOrReplace('versions', data));
	}

	yield put(actions.components.mergeState('AboutModal', 'default', { show: true }));
}
