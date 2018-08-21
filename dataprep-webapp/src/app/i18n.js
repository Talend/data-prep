import i18next from 'i18next'; // eslint-disable-line import/no-named-as-default-member

import constants from './next/constants';

export const fallbackLng = constants.I18N.EN_LOCALE;
export const defaultNS = constants.I18N.TUI_COMPONENTS_NAMESPACE;
export const fallbackNS = constants.I18N.TUI_COMPONENTS_NAMESPACE;

// eslint-disable-next-line import/no-named-as-default-member
const i18n = i18next.init({
	fallbackLng,
	debug: false,
	wait: true, // globally set to wait for loaded translations in translate hoc
	interpolation: { escapeValue: false },
	defaultNS,
	fallbackNS,
});

export function registerLocales(locales) {
	constants.I18N.LOCALES
		.forEach((locale) => {
			Object.keys(locales)
				.forEach((namespace) => {
					i18next.addResources(
						locale,
						namespace,
						locales[namespace][locale],
					);
				});
		});
}

window.i18n = i18n;

export default i18n;
