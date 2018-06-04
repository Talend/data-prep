import { default as p } from './search-providers';

export default class SearchService {
	constructor(categories) {
		this.providers = {};
		Object.keys(categories).forEach((key) => {
			this.providers[key] = new p[key](categories[key]);
		});
	}

	build(provider, term) {
		return this.providers[provider].build(term);
	}

	transform(provider, results) {
		return this.providers[provider].transform(results);
	}
}
