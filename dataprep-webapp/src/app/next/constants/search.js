import i18n from '../../i18n';

export const DATAPREP_SEARCH_URL = '/api/search?path=/&name=';

export const DEBOUNCE_TIMEOUT = 500;

export const SEARCH_CATEGORIES_BY_PROVIDER = {
	tdp: [
		{
			type: 'dataset',
			labelFn: () => i18n.t('tdp-app:DATASETS'),
			icon: 'talend-datastore',
		},
		{
			type: 'preparation',
			labelFn: () => i18n.t('tdp-app:PREPARATIONS'),
			icon: 'talend-dataprep',
		},
		{
			type: 'folder',
			labelFn: () => i18n.t('tdp-app:FOLDERS'),
			icon: 'talend-folder',
		},
	],
	doc: [
		{
			type: 'documentation',
			labelFn: () => i18n.t('tdp-app:DOCUMENTATION'),
			icon: 'talend-question-circle',
		},
	],
};
