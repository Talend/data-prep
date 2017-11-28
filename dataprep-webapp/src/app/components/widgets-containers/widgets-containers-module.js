/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

import AppHeaderBar from '@talend/react-components/lib/HeaderBar';
import Breadcrumbs from '@talend/react-components/lib/Breadcrumbs';
import CircularProgress from '@talend/react-components/lib/CircularProgress';
import CollapsiblePanel from '@talend/react-components/lib/CollapsiblePanel';
import HttpError from '@talend/react-components/lib/HttpError';
import Icon from '@talend/react-components/lib/Icon';
import IconsProvider from '@talend/react-components/lib/IconsProvider';
import SidePanel from '@talend/react-components/lib/SidePanel';
import List from '@talend/react-components/lib/List';
import Progress from '@talend/react-components/lib/Progress';
import Form from '@talend/react-forms';
import getTranslated from '@talend/react-components/lib/TranslateWrapper';

import { i18n } from './../../index-module';

import AppHeaderBarContainer from './app-header-bar/app-header-bar-container';
import BreadcrumbContainer from './breadcrumb/breadcrumb-container';
import CollapsiblePanelContainer from './collapsible-panel/collapsible-panel-container';
import LayoutContainer from './layout/layout-container';
import InventoryListContainer from './inventory-list/inventory-list-container';
import SidePanelContainer from './side-panel/side-panel-container';

import SETTINGS_MODULE from '../../settings/settings-module';
import STATE_MODULE from '../../services/state/state-module';
import DATASET_UPLOAD_STATUS_MODULE from '../dataset/upload-status/dataset-upload-status-module';
import STEP_PROGRESS_MODULE from '../step-progress/step-progress-module';

const MODULE_NAME = '@talend/react-components.containers';

angular.module(MODULE_NAME,
	[
		'react',
		'pascalprecht.translate',
		SETTINGS_MODULE,
		STATE_MODULE,
		DATASET_UPLOAD_STATUS_MODULE,
		STEP_PROGRESS_MODULE,
	])
	.directive('pureAppHeaderBar', ['reactDirective', reactDirective => reactDirective(
		getTranslated(AppHeaderBar, { i18n })
	)])
	.directive('pureBreadcrumb', ['reactDirective', reactDirective => reactDirective(Breadcrumbs)])
	.directive('pureCircularProgress', ['reactDirective', reactDirective => reactDirective(CircularProgress)])
	.directive('pureCollapsiblePanel', ['reactDirective', reactDirective => reactDirective(CollapsiblePanel)])
	.directive('pureList', ['reactDirective', reactDirective => reactDirective(
		getTranslated(List, { i18n })
	)])
	.directive('pureSidePanel', ['reactDirective', reactDirective => reactDirective(
		getTranslated(SidePanel, { i18n })
	)])
	.directive('pureProgress', ['reactDirective', reactDirective => reactDirective(Progress)])
	.directive('iconsProvider', ['reactDirective', reactDirective => reactDirective(IconsProvider)])
	.directive('icon', ['reactDirective', reactDirective => reactDirective(Icon)])
	.directive('httpError', ['reactDirective', reactDirective => reactDirective(HttpError)])
	.directive('talendForm', ['reactDirective', reactDirective => reactDirective(getTranslated(Form, { i18n }), [
		// We need to declare each used props in order to pass them to React component in prod mode
		// @see https://github.com/ngReact/ngReact/issues/193
		'autocomplete',
		'data',
		'actions',
		'onTrigger',
		'onSubmit',
		'showErrorList',
	])])
	.component('appHeaderBar', AppHeaderBarContainer)
	.component('breadcrumbs', BreadcrumbContainer)
	.component('collapsiblePanel', CollapsiblePanelContainer)
	.component('inventoryList', InventoryListContainer)
	.component('sidePanel', SidePanelContainer)
	.component('layout', LayoutContainer);

export default MODULE_NAME;
