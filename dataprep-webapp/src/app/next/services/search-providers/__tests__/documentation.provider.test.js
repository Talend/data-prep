import http from '@talend/react-cmf/lib/sagas/http';
import DocumentationProvider from '../documentation.provider';
import { CATEGORIES, API_RESULTS, FORMATTED_RESULTS } from './documentation.provider.mock';


jest.mock('../../../constants/search', () => ({
	DOCUMENTATION_SEARCH_URL: 'mock.lol',
	DEFAULT_PAYLOAD: { hou: 'yeah' },
}));

describe('DocumentationProvider', () => {
	it('should build correct arguments', () => {
		const provider = new DocumentationProvider(CATEGORIES);
		expect(provider.build('coin')).toEqual([
			http.post,
			'mock.lol',
			{
				hou: 'yeah',
				query: 'coin',
			},
		]);
	});

	it('should transform results', () => {
		const provider = new DocumentationProvider(CATEGORIES);
		expect(provider.transform(API_RESULTS)).toEqual(FORMATTED_RESULTS);
	});
});
