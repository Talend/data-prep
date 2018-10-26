/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import i18n from './../../../i18n/en.json';

describe('AccessError controller', () => {
	'use strict';

	let createController;
	let scope;

	let filter;
	let editable;
	let onEditFn;
	let removable;
	let onRemoveFn;

	beforeEach(angular.mock.module('data-prep.filter-item'));
	beforeEach(angular.mock.module('pascalprecht.translate', $translateProvider => {
		$translateProvider.translations('fr', {
			ERROR_666_TITLE: 'Accès refusé',
			ERROR_666_MESSAGE: 'Vous n\'êtes pas autorisé à accéder à cette page',
		});
		$translateProvider.preferredLanguage('fr');
	}));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();

		createController = () => {
			const ctrl = $componentController('filterItem', {
				$scope: scope,
			}, {
				status: 666,
			});
			ctrl.$onInit();
			return ctrl;
		};
	}));

	it('should return the translated title', () => {
		const ctrl = createController();
		expect(ctrl.title).toEqual(i18n.ERROR_666_TITLE);
	});

	it('should return the translated message', () => {
		const ctrl = createController();
		expect(ctrl.message).toEqual(i18n.ERROR_666_MESSAGE);
	});
});
