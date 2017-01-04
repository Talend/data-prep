/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

import AppHeaderBar from 'react-talend-components/lib/AppHeaderBar';
import Breadcrumbs from 'react-talend-components/lib/Breadcrumbs';
import CollapsiblePanel from 'react-talend-components/lib/CollapsiblePanel';
import Icon from 'react-talend-components/lib/Icon';
import IconsProvider from 'react-talend-components/lib/IconsProvider';
import SidePanel from 'react-talend-components/lib/SidePanel';
import List from 'react-talend-components/lib/List';
import Progress from 'react-talend-components/lib/Progress';
import Form from 'react-talend-forms';

import AppHeaderBarContainer from './app-header-bar/app-header-bar-container';
import BreadcrumbContainer from './breadcrumb/breadcrumb-container';
import CollapsiblePanelContainer from './collapsible-panel/collapsible-panel-container';
import LayoutContainer from './layout/layout-container';
import InventoryListContainer from './inventory-list/inventory-list-container';
import SidePanelContainer from './side-panel/side-panel-container';

import SETTINGS_MODULE from '../../settings/settings-module';
import STATE_MODULE from '../../services/state/state-module';

const MODULE_NAME = 'react-talend-components.containers';

angular.module(MODULE_NAME,
	[
		'react',
		'pascalprecht.translate',
		SETTINGS_MODULE,
		STATE_MODULE,
	])
	.directive('pureAppHeaderBar', ['reactDirective', reactDirective => reactDirective(AppHeaderBar)])
	.directive('pureBreadcrumb', ['reactDirective', reactDirective => reactDirective(Breadcrumbs)])
	.directive('pureCollapsiblePanel', ['reactDirective', reactDirective => reactDirective(CollapsiblePanel)])
	.directive('pureList', ['reactDirective', reactDirective => reactDirective(List)])
	.directive('pureSidePanel', ['reactDirective', reactDirective => reactDirective(SidePanel)])
	.directive('pureProgress', ['reactDirective', reactDirective => reactDirective(Progress)])
	.directive('iconsProvider', ['reactDirective', reactDirective => reactDirective(IconsProvider)])
	.directive('icon', ['reactDirective', reactDirective => reactDirective(Icon)])
	.directive('talendForm', ['reactDirective', reactDirective => reactDirective(Form)])
	.component('appHeaderBar', AppHeaderBarContainer)
	.component('breadcrumbs', BreadcrumbContainer)
	.component('collapsiblePanel', CollapsiblePanelContainer)
	.component('inventoryList', InventoryListContainer)
	.component('sidePanel', SidePanelContainer)
	.component('layout', LayoutContainer);

export default MODULE_NAME;
