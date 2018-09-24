import { put, select } from 'redux-saga/effects';
import Constants from '@talend/react-containers/lib/AboutDialog/AboutDialog.constant';


export function* open() {
	const uris = yield select(state => state.cmf.collections.getIn(['settings', 'uris']));
	yield put({
		type: Constants.ABOUT_DIALOG_SHOW,
		url: uris.get('apiVersion'),
	});
}
