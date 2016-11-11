/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

import { AppHeaderBar, Breadcrumbs,	IconsProvider, SidePanel, List } from 'react-talend-components';
import AppHeaderBarContainer from './app-header-bar/app-header-bar-container';
import BreadcrumbContainer from './breadcrumb/breadcrumb-container';
import LayoutContainer from './layout/layout-container';
import PreparationListContainer from './preparation-list/preparation-list-container';
import SidePanelContainer from './side-panel/side-panel-container';

import FOLDER_MODULE from '../../services/folder/folder-module';
import SETTINGS_MODULE from '../../settings/settings-module';

const MODULE_NAME = 'react-talend-components.containers';

angular.module(MODULE_NAME,
	[
		'react',
		'pascalprecht.translate',
		FOLDER_MODULE,
		SETTINGS_MODULE,
	])
	.directive('pureAppHeaderBar', ['reactDirective', reactDirective => reactDirective(AppHeaderBar)])
	.directive('pureBreadcrumb', ['reactDirective', reactDirective => reactDirective(Breadcrumbs)])
	.directive('pureList', ['reactDirective', reactDirective => reactDirective(List)])
	.directive('pureAppSidePanel', ['reactDirective', reactDirective => reactDirective(SidePanel)])
	.directive('iconsProvider', ['reactDirective', reactDirective => reactDirective(IconsProvider)])
	.component('appHeaderBar', AppHeaderBarContainer)
	.component('breadcrumbs', BreadcrumbContainer)
	.component('reactPreparationList', PreparationListContainer) // TODO rename this when preparation-list is removed
	.component('sidePanel', SidePanelContainer)
	.component('layout', LayoutContainer);

export default MODULE_NAME;
