import i18n from '../../i18n';
import constants from './i18n';

export const DOCUMENTATION_SEARCH_URL = 'https://www.talendforge.org/find/api/THC.php';
export const DATAPREP_SEARCH_URL = '/api/search?path=/&name=';
export const DEBOUNCE_TIMEOUT = 500;
export const DEFAULT_PAYLOAD = {
	contentLocale: constants.DEFAULT_LOCALE,
	filters: [
		{ key: 'version', values: ['2.1'] },
		{ key: 'EnrichPlatform', values: ['Talend Data Preparation'] },
	],
	paging: { page: 1, perPage: 5 },
};
export const SEARCH_CATEGORIES_BY_PROVIDER = {
	tdp: [
		{
			type: 'dataset',
			label: () => i18n.t('tdp-app:DATASETS', { defaultValue: 'datasets' }),
			icon: 'talend-datastore',
		},
		{
			type: 'preparation',
			label: () => i18n.t('tdp-app:PREPARATIONS', { defaultValue: 'preparations' }),
			icon: 'talend-dataprep',
		},
		{
			type: 'folder',
			label: () => i18n.t('tdp-app:FOLDERS', { defaultValue: 'folders' }),
			icon: 'talend-folder',
		},
	],
	doc: [
		{
			type: 'documentation',
			label: () => i18n.t('tdp-app:DOCUMENTATION', { defaultValue: 'documentation' }),
			icon: 'talend-question-circle',
		},
	],
};
