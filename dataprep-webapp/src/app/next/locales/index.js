import DATASET_APP_EN from '@talend/dataset/lib/app/locales/en/dataset-app.json';
import DATASET_APP_FR from '@talend/dataset/lib/app/locales/fr/dataset-app.json';
// import RATING_APP_EN from '@talend/rating/lib/app/locales/en/rating-app.json';
// import RATING_APP_FR from '@talend/rating/lib/app/locales/fr/rating-app.json';
// import SHARING_APP_EN from '@talend/sharing/lib/app/locales/en/sharing-app.json';
// import SHARING_APP_FR from '@talend/sharing/lib/app/locales/fr/sharing-app.json';
import TUI_COMPONENTS_EN from './en/tui-components.json';
// import TUI_COMPONENTS_FR from './fr/tui-components.json';
// import TUI_FORMS_EN from './en/tui-forms.json';
// import TUI_FORMS_FR from './fr/tui-forms.json';
// import TUI_DATAGRID_EN from './en/tui-datagrid.json';
// import TUI_DATAGRID_FR from './fr/tui-datagrid.json';
// import TUI_CONTAINERS_EN from './en/tui-containers.json';
// import TUI_CONTAINERS_FR from './fr/tui-containers.json';
import constants from '../constants';

// import TDC_APP_EN from './en/tdc-app.json';
// import TDC_APP_FR from './fr/tdc-app.json';

const I18N = constants.I18N;

export default {
	// [I18N.TDC_NAME_SPACE]: {
	// 	[I18N.EN_LOCALE]: TDC_APP_EN,
	// 	[I18N.FR_LOCALE]: TDC_APP_FR,
	// },
	[I18N.DATASET_APP_NAME_SPACE]: {
		[I18N.EN_LOCALE]: DATASET_APP_EN,
		[I18N.FR_LOCALE]: DATASET_APP_FR,
	},
	// [I18N.RATING_APP_NAME_SPACE]: {
	// 	[I18N.EN_LOCALE]: RATING_APP_EN,
	// 	[I18N.FR_LOCALE]: RATING_APP_FR,
	// },
	// [I18N.SHARING_APP_NAME_SPACE]: {
	// 	[I18N.EN_LOCALE]: SHARING_APP_EN,
	// 	[I18N.FR_LOCALE]: SHARING_APP_FR,
	// },
	// [I18N.TUI_FORMS_NAME_SPACE]: {
	// 	[I18N.EN_LOCALE]: TUI_FORMS_EN,
	// 	[I18N.FR_LOCALE]: TUI_FORMS_FR,
	// },
	[I18N.TUI_COMPONENTS_NAME_SPACE]: {
		[I18N.EN_LOCALE]: TUI_COMPONENTS_EN,
		// [I18N.FR_LOCALE]: TUI_COMPONENTS_FR,
	},
	// [I18N.TUI_DATAGRID_NAME_SPACE]: {
	// 	[I18N.EN_LOCALE]: TUI_DATAGRID_EN,
	// 	[I18N.FR_LOCALE]: TUI_DATAGRID_FR,
	// },
	// [I18N.TUI_CONTAINERS_NAME_SPACE]: {
	// 	[I18N.EN_LOCALE]: TUI_CONTAINERS_EN,
	// 	[I18N.FR_LOCALE]: TUI_CONTAINERS_FR,
	// },
};
