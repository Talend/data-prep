import en from './en';
import fr from './fr';
import ja from './ja';
import zh from './zh';

import d3LocaleEn from '../lib/d3/locale.en';
import d3LocaleFr from '../lib/d3/locale.fr';
import d3LocaleJa from '../lib/d3/locale.ja';
import d3LocaleZh from '../lib/d3/locale.zh';

const d3CustomLocales = {
	en: d3LocaleEn,
	fr: d3LocaleFr,
	ja: d3LocaleJa,
	zh: d3LocaleZh,
};

export function getD3Locale(locale) {
	return d3CustomLocales[locale];
}

export default { en, fr, ja, zh };
