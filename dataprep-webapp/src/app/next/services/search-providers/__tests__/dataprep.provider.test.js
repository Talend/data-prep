import http from '@talend/react-cmf/lib/sagas/http';
import DataprepProvider from '../dataprep.provider';
import { CATEGORIES, API_RESULTS, FORMATTED_RESULTS } from './dataprep.provider.mock';

jest.mock('../../../constants/search', () => ({
	DATAPREP_SEARCH_URL: 'mock.lol?term=',
}));

describe('DataprepProvider', () => {
	it('should build correct arguments', () => {
		const provider = new DataprepProvider(CATEGORIES);
		expect(provider.build('yeah')).toEqual([
			http.get,
			'mock.lol?term=yeah&categories=dataset&categories=preparation',
		]);
	});

	it('should transform results', () => {
		const provider = new DataprepProvider(CATEGORIES);
		expect(provider.transform(API_RESULTS)).toEqual(FORMATTED_RESULTS);
	});
});
