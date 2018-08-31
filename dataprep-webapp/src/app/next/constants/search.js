import i18n from '../../i18n';

export const DATAPREP_SEARCH_URL = '/api/search?path=/&name=';

export const DEBOUNCE_TIMEOUT = 500;

export const SEARCH_CATEGORIES_BY_PROVIDER = {
	tdp: [
		{
			type: 'dataset',
			labelFn: () => i18n.t('tdp-app:DATASETS', { defaultValue: 'datasets' }),
			icon: 'talend-datastore',
		},
		{
			type: 'preparation',
			labelFn: () => i18n.t('tdp-app:PREPARATIONS', { defaultValue: 'preparations' }),
			icon: 'talend-dataprep',
		},
		{
			type: 'folder',
			labelFn: () => i18n.t('tdp-app:FOLDERS', { defaultValue: 'folders' }),
			icon: 'talend-folder',
		},
	],
	doc: [
		{
			type: 'documentation',
			labelFn: () => i18n.t('tdp-app:DOCUMENTATION', { defaultValue: 'documentation' }),
			icon: 'talend-question-circle',
		},
	],
};
