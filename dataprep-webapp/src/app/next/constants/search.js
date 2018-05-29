import http from '@talend/react-cmf/lib/actions/http';


export const DOCUMENTATION_SEARCH_URL = 'https://www.talendforge.org/find/api/THC.php';
export const TDP_SEARCH_URL = 'http://localhost:3000/api/search?path=/&name=';
export const DEBOUNCE_TIMEOUT = 500;
export const DEFAULT_SEARCH_PAYLOAD = {
	contentLocale: 'en',
	filters: [
		{ key: 'version', values: ['2.1'] },
		{ key: 'EnrichPlatform', values: ['Talend Data Preparation'] },
	],
	paging: { page: 1, perPage: 5 },
};
export const SEARCH_CATEGORIES = [
	{
		id: 'dataset',
		provider: 'tdp',
	},
	{
		id: 'preparation',
		provider: 'tdp',
	},
	{
		id: 'folder',
		provider: 'tdp',
	},
	{
		id: 'dataset',
		provider: 'tdp',
	},
	{
		id: 'documentation',
		provider: 'external',
	},
];


export const QUERY_BUILDER = {
	tdp: () => {
		return http.get('http://tld.com');
	},
	external: () => {
		return http.get('http://tld.fr');
	},
};
