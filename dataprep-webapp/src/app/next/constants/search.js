export const DOCUMENTATION_SEARCH_URL = 'https://www.talendforge.org/find/api/THC.php';
export const DATAPREP_SEARCH_URL = 'http://localhost:3000/api/search?path=/&name=';
export const DEBOUNCE_TIMEOUT = 500;
export const DEFAULT_SEARCH_PAYLOAD = {
	contentLocale: 'en',
	filters: [
		{ key: 'version', values: ['2.1'] },
		{ key: 'EnrichPlatform', values: ['Talend Data Preparation'] },
	],
	paging: { page: 1, perPage: 5 },
};
export const SEARCH_CATEGORIES_BY_PROVIDER = {
	tdp: ['dataset', 'preparation', 'folder'],
	doc: ['documentation'],
};
