import i18next from 'i18next'; // eslint-disable-line import/no-named-as-default-member

import constants from './next/constants';

export const fallbackLng = constants.I18N.EN_LOCALE;
export const defaultNS = constants.I18N.TDP_APP_NAMESPACE;
export const fallbackNS = constants.I18N.TDP_APP_NAMESPACE;

// eslint-disable-next-line import/no-named-as-default-member
const i18n = i18next.init({
	fallbackLng,
	debug: false,
	wait: true, // globally set to wait for loaded translations in translate hoc
	interpolation: {
		escapeValue: false,
		format: (value, format) => {
			if (format === 'lowercase') return value.toLowerCase();
			if (format === 'uppercase') return value.toUpperCase();
			return value;
		},
	},
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

export function getDefaultTranslate(key) {
	return key;
}

window.i18n = i18n;

export default i18n;
