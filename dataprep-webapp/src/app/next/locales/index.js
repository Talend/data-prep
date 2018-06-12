import DATASET_APP_EN from '@talend/dataset/lib/app/locales/en/dataset-app.json';
import DATASET_APP_FR from '@talend/dataset/lib/app/locales/fr/dataset-app.json';
import TUI_COMPONENTS_EN from './en/tui-components.json';
import constants from '../constants';

const I18N = constants.I18N;

export default {
	[I18N.DATASET_APP_NAME_SPACE]: {
		[I18N.EN_LOCALE]: DATASET_APP_EN,
		[I18N.FR_LOCALE]: DATASET_APP_FR,
	},
	[I18N.TUI_COMPONENTS_NAME_SPACE]: {
		[I18N.EN_LOCALE]: TUI_COMPONENTS_EN,
	},
};
