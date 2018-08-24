import providers from './search-providers';

export default class SearchService {
	constructor(categories) {
		this.providers = {};
		Object
			.keys(categories)
			.forEach((categoryKey) => {
				this.providers[categoryKey] = new providers[categoryKey](categories[categoryKey]);
			});
	}

	build(provider, term) {
		return this.providers[provider].build(term);
	}

	transform(provider, results) {
		return this.providers[provider].transform(results);
	}
}
