export const DOCUMENTATION_SEARCH_URL = 'https://www.talendforge.org/find/api/THC.php';
export const DATAPREP_SEARCH_URL = '/api/search?path=/&name=';
export const DEBOUNCE_TIMEOUT = 500;
export const SEARCH_CATEGORIES_BY_PROVIDER = {
	tdp: [
		{
			type: 'dataset',
			label: 'Datasets',
			icon: 'talend-datastore',
		},
		{
			type: 'preparation',
			label: 'Preparations',
			icon: 'talend-dataprep',
		},
		{
			type: 'folder',
			label: 'Folders',
			icon: 'talend-folder',
		},
	],
	doc: [
		{
			type: 'documentation',
			label: 'Documentation',
			icon: 'talend-question-circle',
		},
	],
};
