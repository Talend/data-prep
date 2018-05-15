import { actions } from '@talend/react-cmf';
import { put, take } from 'redux-saga/effects';
import { OPEN_ABOUT } from '../constants';

function* openAbout() {
	while (true) {
		yield take(OPEN_ABOUT);
		yield put(actions.components.mergeState('AboutModal', 'default', { show: true }));
	}
}

export default {
	openAbout,
};
