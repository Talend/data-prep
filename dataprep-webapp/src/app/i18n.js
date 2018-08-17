import i18next from 'i18next'; // eslint-disable-line import/no-named-as-default-member

import constants from './next/constants';

import { default as locales } from './next/locales';

const I18N = constants.I18N;

function setNameSpace(locale, namespace) {
	// overwrite all existing resources if exists
	i18next.addResources(
		locale,
		namespace,
		locales[namespace][locale],
	);
}

export const fallbackLng = I18N.EN_LOCALE;
export const defaultNS = I18N.TUI_COMPONENTS_NAMESPACE;
export const fallbackNS = I18N.TUI_COMPONENTS_NAMESPACE;

// eslint-disable-next-line import/no-named-as-default-member
const i18n = i18next.init({
	fallbackLng,
	debug: false,
	wait: true, // globally set to wait for loaded translations in translate hoc
	interpolation: { escapeValue: false },
	ns: [
		I18N.DATASET_APP_NAMESPACE,
		I18N.TUI_COMPONENTS_NAMESPACE,
		I18N.TUI_FORMS_NAMESPACE,
	],
	defaultNS,
	fallbackNS,
});

I18N.LOCALES.forEach(locale => [
	I18N.TDP_APP_NAMESPACE,
	I18N.DATASET_APP_NAMESPACE,
	I18N.TUI_COMPONENTS_NAMESPACE,
	I18N.TUI_CONTAINERS_NAMESPACE,
	I18N.TUI_FORMS_NAMESPACE,
].forEach(namespace => setNameSpace(locale, namespace)));

window.i18n = i18n;

export default i18n;
