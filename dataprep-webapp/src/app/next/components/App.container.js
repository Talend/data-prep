import React from 'react';
import PropTypes from 'prop-types';
import { Helmet } from 'react-helmet';
import { I18nextProvider } from 'react-i18next';
import { List, Map } from 'immutable';
import { IconsProvider } from '@talend/react-components';
import { ACKDispatcher } from '@talend/react-cmf-cqrs';
import {
	Notification,
	ShortcutManager,
	AppLoader,
	AboutDialog,
	ConfirmDialog,
} from '@talend/react-containers';

import components from './';
import i18n from './../../i18n';
import settingsService from './../services/settings.service';
import './App.scss';

const initialNotificationsState = new Map({
	notifications: new List([]),
});

const initialAddFolderState = new Map({
	show: false,
});

export default function App(props) {
	return (
		<I18nextProvider i18n={i18n}>
			<React.Fragment>
				<Helmet htmlAttributes={{ lang: settingsService.getLanguage() }} />
				<AppLoader>
					<div className="tdp">
						<IconsProvider />
						<ACKDispatcher />
						<ShortcutManager />
						<Notification initialState={initialNotificationsState} />
						<AboutDialog />
						<ConfirmDialog />
						<components.PreparationCreatorModal />
						<components.PreparationCopyMoveModal />
						<components.FolderCreatorModal initialState={initialAddFolderState} />
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
