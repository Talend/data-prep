import localStorage from 'store';

import { fallbackLng } from '../../i18n';

const settings = localStorage.get('settings') || {
	context: {},
	help: {},
};

function getLanguage() {
	return settings.context.language || fallbackLng;
}

window.settings = settings;

export default {
	...settings,
	getLanguage,
};
