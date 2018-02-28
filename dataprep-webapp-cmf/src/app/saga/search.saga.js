import { actions } from '@talend/react-cmf';
import http from '@talend/react-cmf/lib/sagas/http';
import { put, all, call, take } from 'redux-saga/effects';
import { SEARCH } from '../constants';

const tdpUrl = 'http://localhost:8888/api/search?path=/&name=test';
const docUrl = 'https://www.talendforge.org/find/api/THC.php'
const docPl = {
	contentLocale: 'en',
	filters: [
		{
			key: 'version',
			values: [2.1],
		},
		{
			key: 'EnrichPlatform',
			values: ['Talend Data Preparation'],
		},
	],
	paging: {
		page: 1,
		perPage: 5,
	},
	query: 'test',
};

export function* search() {
	while (true) {
		yield take(SEARCH);
		const [tdp, doc] = yield all([
			call(http.get, tdpUrl),
			call(http.post, docUrl, docPl),
		]);
		console.log('[NC] results: ', { tdp, doc });
	}
}
