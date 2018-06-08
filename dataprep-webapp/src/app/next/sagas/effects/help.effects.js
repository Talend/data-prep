import { actions } from '@talend/react-cmf';
import { put } from 'redux-saga/effects';

export function* open() {
	yield put(actions.components.mergeState('AboutModal', 'default', { show: true }));
}
