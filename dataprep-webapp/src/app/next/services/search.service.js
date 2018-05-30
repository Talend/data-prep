import http from '@talend/react-cmf/lib/sagas/http';
import {
	DOCUMENTATION_SEARCH_URL,
	DATAPREP_SEARCH_URL,
} from '../constants/search';


class SearchProvider {
	build() {
		throw new Error('build(): Not implemented');
	}

	convert() {
		throw new Error('convert(): Not implemented');
	}
}

class DataprepSearchProvider extends SearchProvider {
	static build(term, categories) {
		const query = categories.map(t => `categories=${t}`).join('&');
		return [
			[
				http.get,
				`${DATAPREP_SEARCH_URL}${term}&${query}`,
			],
		];
	}

	static convert(data) {
		console.log('[NC] DataprepSearchProvider::convert(): ', data);
	}
}


class DocumentationSearchProvider extends SearchProvider {
	static DEFAULT_PAYLOAD = {
		contentLocale: 'en',
		filters: [
			{ key: 'version', values: ['2.1'] },
			{ key: 'EnrichPlatform', values: ['Talend Data Preparation'] },
		],
		paging: { page: 1, perPage: 5 },
	};

	static build(query) {
		return [
			[
				http.post,
				DOCUMENTATION_SEARCH_URL,
				{
					...DocumentationSearchProvider.DEFAULT_PAYLOAD,
					query,
				},
			],
		];
	}

	static convert(data) {
		console.log('[NC] DocumentationSearchProvider::convert(): ', data);
	}
}

export default class SearchService {
	static PROVIDERS = {
		tdp: DataprepSearchProvider,
		doc: DocumentationSearchProvider,
	};

	static build(provider, categories, term) {
		if (!SearchService.PROVIDERS[provider]) {
			throw new Error('Unknown provider');
		}

		return SearchService.PROVIDERS[provider].build(term, categories);
	}
}
