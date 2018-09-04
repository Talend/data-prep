import http from '@talend/react-cmf/lib/sagas/http';
import DocumentationProvider from '../documentation.provider';
import { CATEGORIES, API_RESULTS, FORMATTED_RESULTS } from './documentation.provider.mock';

jest.mock('../../settings.service', () => ({
	help: {
		languageFacet: 'fr',
		searchUrl: 'https://www.talendforge.org/find/api/THC.php',
		versionFacet: 'cloud',
	},
}));

describe('DocumentationProvider', () => {
	it('should build correct arguments', () => {
		const query = 'regex';
		const provider = new DocumentationProvider(CATEGORIES);
		expect(provider.build(query))
			.toEqual([
				http.post,
				'https://www.talendforge.org/find/api/THC.php',
				{
					contentLocale: 'fr',
					filters: [
						{ key: 'version', values: ['cloud'] },
						{ key: 'EnrichPlatform', values: ['Talend Data Preparation'] },
					],
					paging: { page: 1, perPage: 5 },
					query,
				},
			]);
	});

	it('should transform results', () => {
		const provider = new DocumentationProvider(CATEGORIES);
		expect(provider.transform(API_RESULTS))
			.toEqual(FORMATTED_RESULTS);
	});
});
