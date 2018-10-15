/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import i18n from '../../../../i18n/en';

describe('Dataset upload status controller', () => {
	'use strict';

	let createController;
	let scope;

	beforeEach(angular.mock.module('data-prep.dataset-upload-status'));

	beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(function ($rootScope, $controller) {
		scope = $rootScope.$new();

		createController = function () {
			return $controller('DatasetUploadStatusCtrl', {
				$scope: scope,
			});
		};
	}));

	describe('getProgressionLabel', () => {

		it('should processing', inject(($rootScope) => {
			//given
			const ctrl = createController();
			ctrl.dataset = {
				progress: 100,
			};

			//when
			$rootScope.$digest();
			const label = ctrl.getProgressionLabel();

			//then
			expect(label).toBe(i18n.UPLOAD_PROCESSING);
		}));

		it('should finalizing', inject(($rootScope) => {
			//given
			const ctrl = createController();
			ctrl.dataset = {
				progress: 42,
			};

			//when
			$rootScope.$digest();
			const label = ctrl.getProgressionLabel();

			//then
			expect(label).toBe('42 %');
		}));
	});
});
