/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import EASTER_EGG_MODULE from '../easter-eggs/easter-eggs-module';
import FEEDBACK_MODULE from '../feedback/feedback-module';
import HOME_MODULE from '../home/home-module';
import NAVBAR_MODULE from '../navbar/navbar-module';
import PLAYGROUND_MODULE from '../playground/playground-module';
import UPGRADE_VERSION_MODULE from '../upgrade-version/upgrade-version-module';

import DataPrepApp from './app-directive';

const MODULE_NAME = 'data-prep.app';

angular.module(MODULE_NAME,
	[
		EASTER_EGG_MODULE,
		FEEDBACK_MODULE,
		HOME_MODULE,
		NAVBAR_MODULE,
		PLAYGROUND_MODULE,
		UPGRADE_VERSION_MODULE,
	])
    .directive('dataprepApp', DataPrepApp);

export default MODULE_NAME;
