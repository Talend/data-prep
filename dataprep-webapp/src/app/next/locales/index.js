import DATASET_APP_EN from '@talend/dataset/lib/app/locales/en/dataset-app.json';
import DATASET_APP_FR from '@talend/dataset/lib/app/locales/fr/dataset-app.json';
import DATASET_APP_JA from '@talend/dataset/lib/app/locales/ja/dataset-app.json';

import TDP_APP_EN from './en/tdp-app.json';
import TDP_APP_FR from './fr/tdp-app.json';
import TDP_APP_JA from './ja/tdp-app.json';

import TDP_CMF_EN from './en/tdp-cmf.json';
import TDP_CMF_FR from './fr/tdp-cmf.json';
import TDP_CMF_JA from './ja/tdp-cmf.json';

import TUI_COMPONENTS_EN from './en/tui-components.json';
import TUI_COMPONENTS_FR from './fr/tui-components.json';
import TUI_COMPONENTS_JA from './ja/tui-components.json';

import TUI_CONTAINERS_EN from './en/tui-containers.json';
import TUI_CONTAINERS_FR from './fr/tui-containers.json';
import TUI_CONTAINERS_JA from './ja/tui-containers.json';

import TUI_FORMS_EN from './en/tui-forms.json';
import TUI_FORMS_FR from './fr/tui-forms.json';
import TUI_FORMS_JA from './ja/tui-forms.json';

import constants from '../constants';

const I18N = constants.I18N;

export default {
	[I18N.TDP_APP_NAMESPACE]: {
		[I18N.EN_LOCALE]: TDP_APP_EN,
		[I18N.FR_LOCALE]: TDP_APP_FR,
		[I18N.JA_LOCALE]: TDP_APP_JA,
	},
	[I18N.TDP_CMF_NAMESPACE]: {
		[I18N.EN_LOCALE]: TDP_CMF_EN,
		[I18N.FR_LOCALE]: TDP_CMF_FR,
		[I18N.JA_LOCALE]: TDP_CMF_JA,
	},
	[I18N.DATASET_APP_NAMESPACE]: {
		[I18N.EN_LOCALE]: DATASET_APP_EN,
		[I18N.FR_LOCALE]: DATASET_APP_FR,
		[I18N.JA_LOCALE]: DATASET_APP_JA,
	},
	[I18N.TUI_COMPONENTS_NAMESPACE]: {
		[I18N.EN_LOCALE]: TUI_COMPONENTS_EN,
		[I18N.FR_LOCALE]: TUI_COMPONENTS_FR,
		[I18N.JA_LOCALE]: TUI_COMPONENTS_JA,
	},
	[I18N.TUI_CONTAINERS_NAMESPACE]: {
		[I18N.EN_LOCALE]: TUI_CONTAINERS_EN,
		[I18N.FR_LOCALE]: TUI_CONTAINERS_FR,
		[I18N.JA_LOCALE]: TUI_CONTAINERS_JA,
	},
	[I18N.TUI_FORMS_NAMESPACE]: {
		[I18N.EN_LOCALE]: TUI_FORMS_EN,
		[I18N.FR_LOCALE]: TUI_FORMS_FR,
		[I18N.JA_LOCALE]: TUI_FORMS_JA,
	},
};
