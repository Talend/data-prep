import http from '@talend/react-cmf/lib/sagas/http';
import SearchProvider from './search.provider';
import { DOCUMENTATION_SEARCH_URL } from '../../constants/search';

export default class DocumentationSearchProvider extends SearchProvider {
	static KEY = 'doc';
	static CATEGORY = 'documentation';
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
			http.post,
			DOCUMENTATION_SEARCH_URL,
			{
				...DocumentationSearchProvider.DEFAULT_PAYLOAD,
				query,
			},
		];
	}

	static _normalize(str) {
		const dom = document.createElement('p');
		dom.innerHTML = str.replace(/(<[^>]*>)/g, '');
		return dom.innerText;
	}

	static convert(data) {
		return {
			title: DocumentationSearchProvider.CATEGORY,
			suggestions: data.data.results.map(topic => ({
				category: DocumentationSearchProvider.CATEGORY,
				description: DocumentationSearchProvider._normalize(topic.htmlExcerpt),
				title: DocumentationSearchProvider._normalize(topic.htmlTitle),
				url: topic.occurrences[0].readerUrl,
			})),
		};
	}
}
