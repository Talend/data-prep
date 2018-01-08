/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../../mocks/Settings.mock';

const { help } = settings;

describe('Playground header component', () => {
	'use strict';

	let scope;
	let createElement;
	let element;

	beforeEach(angular.mock.module('data-prep.playground'));

	beforeEach(inject((HelpService) => {
		HelpService.register(help);
	}));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);

		createElement = () => {
			element = angular.element(`
			<playground-header
                    preview="preview"
                    lookup-visible="lookupVisible"
                    preparation-picker="preparationPicker"
                    parameters-visible="parametersVisible"
                    enable-export="enableExport"
                    on-parameters="onParameters()"
                    on-lookup="onLookup()"
                    on-close="onClose()"
                    on-preparation-picker="showPreparationPicker()"
					preparation-name="preparationName"
					name-edition-mode="nameEditionMode"
					confirm-name-edition="confirmPrepNameEdition"
					is-readonly="isReadonly"></playground-header>`);
			$compile(element)(scope);
			scope.$digest();
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('preview badge', () => {
		it('should NOT be rendered', () => {
			// given
			scope.preview = false;

			// when
			createElement();

			// then
			expect(element.find('.playground-header-preview').length).toBe(0);
		});

		it('should be rendered', () => {
			// given
			scope.preview = true;

			// when
			createElement();

			// then
			expect(element.find('.preview').length).toBe(1);
		});
	});

	describe('left header', () => {
		it('should render editable text on preparation title', () => {
			//given
			scope.isReadOnly = false;

			//when
			createElement();

			//then
			const title = element.find('.playground-sub-header-left').eq(0).find('talend-editable-text');
			expect(title.length).toBe(1);
		});

		it('should render preparation title on readonly mode', () => {
			//given
			scope.isReadOnly = true;
			scope.preparationName = 'prep1';

			//when
			createElement();

			//then
			expect(element.find('.playground-sub-header-left .header-preparation-name').length).toBe(1);
			expect(element.find('.playground-sub-header-left .header-preparation-name').text().trim()).toBe('prep1');
		});
	});

	describe('right header', () => {
		it('dataset parameters toggle button looks inactive by default', () => {
			// given
			createElement();

			// when
			const playgroundGearIcon = element.find('#playground-gear-icon').eq(1);

			// then
			expect(playgroundGearIcon.hasClass('pressed')).toBe(false);
		});

		it('dataset parameters toggle button looks active when its panel is shown', () => {
			// given
			scope.parametersVisible = true;
			createElement();
			const playgroundGearIcon = element.find('#playground-gear-icon').eq(0);

			// when
			playgroundGearIcon.click();

			// then
			expect(playgroundGearIcon.hasClass('pressed')).toBe(true);
		});

		it('should call parameters callback', () => {
			// given
			scope.onParameters = jasmine.createSpy('onParameters');
			createElement();

			// when
			element.find('#playground-gear-icon').eq(0).click();

			// then
			expect(scope.onParameters).toHaveBeenCalled();
		});

		it('lookup toggle button looks inactive by default', () => {
			// given
			createElement();

			// when
			let playgroundLookupIcon = element.find('#playground-lookup-icon').eq(0);

			// then
			expect(playgroundLookupIcon.hasClass('pressed')).toBe(false);
		});

		it('lookup toggle button looks active when its panel is shown', () => {
			// given
			scope.lookupVisible = true;
			createElement();

			// when
			let playgroundLookupIcon = element.find('#playground-lookup-icon').eq(0);
			playgroundLookupIcon.click();

			// then
			expect(playgroundLookupIcon.hasClass('pressed')).toBe(true);
		});

		it('should call lookup callback', () => {
			// given
			scope.onLookup = jasmine.createSpy('onLookup');
			createElement();

			// when
			element.find('#playground-lookup-icon').eq(0).click();

			// then
			expect(scope.onLookup).toHaveBeenCalled();
		});

		it('should render preparation picker icon', () => {
			// when
			scope.preparationPicker = true;
			createElement();

			// then
			expect(element.find('#playground-preparation-picker-icon').length).toBe(1);
		});

		it('should call showPreparationPicker callback', () => {
			// given
			scope.showPreparationPicker = jasmine.createSpy('showPreparationPicker');
			scope.preparationPicker = true;
			createElement();
			const icon = element.find('#playground-preparation-picker-icon').eq(0);

			// when
			icon.click();

			// then
			expect(scope.showPreparationPicker).toHaveBeenCalled();
		});

		it('should call close callback', () => {
			// given
			scope.onClose = jasmine.createSpy('onClose');
			createElement();

			// when
			element.find('#playground-close-icon').eq(0).click();

			// then
			expect(scope.onClose).toHaveBeenCalled();
		});

		it('should render export', () => {
			// given
			scope.enableExport = true;

			// when
			createElement();

			// then
			expect(element.find('export').length).toBe(1);
		});

		it('should NOT render export', () => {
			// given
			scope.enableExport = false;

			// when
			createElement();

			// then
			expect(element.find('export').length).toBe(0);
		});

		it('should render history control', () => {
			// when
			createElement();

			// then
			expect(element.find('history-control').length).toBe(1);
		});

		it('should NOT render preview badge', () => {
			// given
			scope.preview = false;

			// when
			createElement();

			// then
			expect(element.find('#preview').length).toBe(0);
		});
	});
});
