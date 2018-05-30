import http from '@talend/react-cmf/lib/sagas/http';
import SearchProvider from './search.provider';
import { DATAPREP_SEARCH_URL } from '../../constants/search';


export default class DataprepSearchProvider extends SearchProvider {
	static KEY = 'doc';

	static build(term, categories) {
		const query = categories.map(t => `categories=${t}`).join('&');
		return [
			http.get,
			`${DATAPREP_SEARCH_URL}${term}&${query}`,
		];
	}

	static convert(data) {
		const converted = JSON.parse(data.data);
		return Object.keys(converted).map(category => ({
			title: category,
			suggestions: converted[category].map(({ id, name }) => ({
				title: name,
				category,
				id,
			})),
		}));
	}
}
