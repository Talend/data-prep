/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_EASTER_EGG_MODULE from '../../services/easter-eggs/easter-eggs-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';
import SERVICES_UTILS_MODULE from '../../services/utils/utils-module';

import EasterEggsCtrl from './easter-eggs-controller';
import EasterEggs from './easter-eggs-directive';
import StarWars from './star-wars/star-wars-directive';
import BozoTheClown from './bozo-the-clown/bozo-the-clown-directive';

const MODULE_NAME = 'data-prep.easter-eggs';

/**
 * @ngdoc object
 * @name data-prep.easter-eggs
 * @description This module contains data prep easter eggs
 * @requires 'data-prep.services.easter-eggs'
 * @requires 'data-prep.services.state'
 * @requires 'data-prep.services.utils'
 */
angular.module(MODULE_NAME,
	[
		SERVICES_EASTER_EGG_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .controller('EasterEggsCtrl', EasterEggsCtrl)
    .directive('starWars', StarWars)
    .directive('bozoTheClown', BozoTheClown)
    .directive('easterEggs', EasterEggs);

export default MODULE_NAME;
