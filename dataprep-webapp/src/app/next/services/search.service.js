import * as providers from './search-providers';

export default class SearchService {
	static PROVIDERS = {
		tdp: providers.DataprepSearchProvider,
		doc: providers.DocumentationSearchProvider,
	};

	static getProvider(key) {
		if (!SearchService.PROVIDERS[key]) {
			throw new Error('Unknown provider: ' + key);
		}
		return SearchService.PROVIDERS[key];
	}

	static build(provider, categories, term) {
		return SearchService.getProvider(provider).build(term, categories);
	}

	static convert(provider, results) {
		return SearchService.getProvider(provider).convert(results);
	}
}
