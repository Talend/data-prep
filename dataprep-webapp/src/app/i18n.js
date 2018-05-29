import i18next from 'i18next'; // eslint-disable-line import/no-named-as-default-member

import constants from './next/constants';

import { default as locales } from './next/locales';

const I18N = constants.I18N;
console.log(I18N);

const NAMESPACES = [
	I18N.DATASET_APP_NAME_SPACE,
	I18N.TUI_COMPONENTS_NAME_SPACE,
];

function setNameSpace(locale, namespace) {
	// overwrite all existing resources if exists
	i18next.addResources(
		locale,
		namespace,
		locales[namespace][locale],
	);
}

// eslint-disable-next-line import/no-named-as-default-member
i18next.init({
	fallbackLng: 'en',
	debug: false,
	wait: true, // globally set to wait for loaded translations in translate hoc
	lng: I18N.EN_LOCALE,
});

I18N.LOCALES.forEach(locale => NAMESPACES.forEach(namespace => setNameSpace(locale, namespace)));

export default i18next;
