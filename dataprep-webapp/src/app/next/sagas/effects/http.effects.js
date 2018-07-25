import { put, select } from 'redux-saga/effects';
import { HTTP_STATUS } from '@talend/react-cmf/lib/middlewares/http/constants';
import { REDIRECT_WINDOW } from '../../constants/actions';
import { default as creators } from '../../actions';

export function* handle(event) {
	if (!event.error || !event.error.stack) {
		return;
	}

	switch (event.error.stack.status) {
	case HTTP_STATUS.UNAUTHORIZED: {
		const settings = yield select(state => state.cmf.collections.get('settings'));
		yield put({
			type: REDIRECT_WINDOW,
			payload: {
				url: settings.get('uris').get('login'),
			},
		});
		break;
	}
	case HTTP_STATUS.FORBIDDEN:
	case HTTP_STATUS.NOT_FOUND: {
		yield put({
			type: '@@router/CALL_HISTORY_METHOD',
			payload: {
				method: 'replace',
				args: [`/${event.error.stack.status}`],
			},
		});
		break;
	}
	case HTTP_STATUS.GATEWAY_TIMEOUT: {
		yield put(creators.notification.error(null, {
			title: 'An error has occurred',
			message: 'Service unavailable',
		}));
		break;
	}
	default: {
		yield put(creators.notification.error(null, {
			title: 'An error has occurred',
			message: 'Sorry an unexpected error occurred and we could not complete your last operation, but you can keep using Data Preparation.',
		}));
	}
	}
}
