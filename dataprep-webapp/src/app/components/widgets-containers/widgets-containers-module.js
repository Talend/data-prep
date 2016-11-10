/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

import { AppHeaderBar, SidePanel } from 'react-talend-components';
import AppHeaderBarContainer from './app-header-bar/app-header-bar-container';
import SidePanelContainer from './side-panel/side-panel-container';
import LayoutContainer from './layout/layout-container';


import SETTINGS_MODULE from '../../settings/settings-module';

const MODULE_NAME = 'react-talend-components.containers';

angular.module(MODULE_NAME,
	[
		'react',
		'pascalprecht.translate',
		SETTINGS_MODULE,
	])
	.directive('pureAppHeaderBar', ['reactDirective', reactDirective => reactDirective(AppHeaderBar)])
	.directive('pureAppSidePanel', ['reactDirective', reactDirective => reactDirective(SidePanel)])
	.component('appHeaderBar', AppHeaderBarContainer)
	.component('sidePanel', SidePanelContainer)
	.component('layout', LayoutContainer);

export default MODULE_NAME;
