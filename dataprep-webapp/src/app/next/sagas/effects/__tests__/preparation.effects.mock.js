import Immutable from 'immutable';

export const SETTINGS = {
	apiExport: '/api/export',
	apiAggregate: '/api/aggregate',
	apiSettings: '/api/settings',
	apiTcomp: '/api/tcomp',
	apiTypes: '/api/types',
	apiUploadDatasets: '/api/datasets',
	apiDatasets: '/api/datasets',
	apiUpgradeCheck: '/api/upgrade/check',
	apiVersion: '/api/version',
	apiFolders: '/api/folders',
	apiMail: '/api/mail',
	apiPreparationsPreview: '/api/preparations/preview',
	apiPreparations: '/api/preparations',
	context: '',
	apiSearch: '/api/search',
	apiTransform: '/api/transform',
};

export const IMMUTABLE_SETTINGS = Immutable.fromJS(SETTINGS);

export const API_PAYLOAD = { test: 'lol' };

export const API_RESPONSE = {
	data: API_PAYLOAD,
	response: { ok: true },
};
