import http from '@talend/react-cmf/lib/sagas/http';
import SearchProvider from './search.provider';
import { DATAPREP_SEARCH_URL } from '../../constants/search';


export default class DataprepSearchProvider extends SearchProvider {
	constructor(categories) {
		super();
		this.categories = categories;
	}

	build(term) {
		const query = this.categories.map(t => `categories=${t.type}`).join('&');
		return [
			http.get,
			`${DATAPREP_SEARCH_URL}${term}&${query}`,
		];
	}

	transform(data) {
		const converted = JSON.parse(data.data);
		return Object.keys(converted).map((type) => {
			const category = this.categories.find(cat => cat.type === type);

			return {
				title: category.label,
				icon: {
					name: category.icon,
					title: category.label,
				},
				suggestions: converted[type]
					.filter(suggestion => suggestion.name.length)
					.map(({ id, name }) => ({
						title: name,
						type,
						id,
					})),
			};
		});
	}
}
