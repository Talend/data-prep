import { call, take } from 'redux-saga/effects';
import http from '@talend/react-cmf/lib/sagas/http';
import { FETCH_VERSION } from '../constants';

function* fetch() {
	while (true) {
		yield take(FETCH_VERSION);
		yield call(
			http.get('http://localhost:8888/api/version',
				{
					cmf: {
						collectionId: 'versions',
					},
				},
			),
		);
	}
}

export default {
	fetch,
};
