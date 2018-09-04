import React from 'react';
import PropTypes from 'prop-types';
import { Helmet } from 'react-helmet';
import { I18nextProvider } from 'react-i18next';
import { List, Map } from 'immutable';
import { IconsProvider } from '@talend/react-components';
import { Notification, ShortcutManager } from '@talend/react-containers';
import AppLoader from '@talend/react-containers/lib/AppLoader/index';

import components from './';
import i18n from './../../i18n';
import settingsService from './../services/settings.service';

const initialNotificationsState = new Map({
	notifications: new List([]),
});

export default function App(props) {
	return (
		<I18nextProvider i18n={i18n}>
			<React.Fragment>
				<Helmet htmlAttributes={{ lang: settingsService.getLanguage() }} />
				<AppLoader>
					<div className="tdp">
						<IconsProvider />
						<ShortcutManager />
						<Notification initialState={initialNotificationsState} />
						<components.AboutModal />
						<components.PreparationCreatorModal />
						<components.PreparationCopyMoveModal />
						{props.children}
					</div>
				</AppLoader>
			</React.Fragment>
		</I18nextProvider>
	);
}

App.displayName = 'App';
App.propTypes = {
	children: PropTypes.element.isRequired,
};
