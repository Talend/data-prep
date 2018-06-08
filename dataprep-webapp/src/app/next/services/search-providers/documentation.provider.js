import http from '@talend/react-cmf/lib/sagas/http';
import SearchProvider from './search.provider';
import { DOCUMENTATION_SEARCH_URL } from '../../constants/search';


export default class DocumentationSearchProvider extends SearchProvider {
	constructor(categories) {
		super();
		this.category = categories[0];
		this.DEFAULT_PAYLOAD = {
			contentLocale: 'en',
			filters: [
				{ key: 'version', values: ['2.1'] },
				{ key: 'EnrichPlatform', values: ['Talend Data Preparation'] },
			],
			paging: { page: 1, perPage: 5 },
		};
	}

	build(query) {
		return [
			http.post,
			DOCUMENTATION_SEARCH_URL,
			{
				...this.DEFAULT_PAYLOAD,
				query,
			},
		];
	}

	transform(data) {
		return {
			title: this.category.label,
			icon: {
				name: this.category.icon,
				title: this.category.type,
			},
			suggestions: data.data.results.map(topic => ({
				type: this.category.type,
				description: normalize(topic.htmlExcerpt),
				title: normalize(topic.htmlTitle),
				url: topic.occurrences[0].readerUrl,
			})),
		};
	}
}

function normalize(str) {
	const dom = document.createElement('p');
	dom.innerHTML = str.replace(/(<[^>]*>)/g, '');
	return dom.innerText;
}
