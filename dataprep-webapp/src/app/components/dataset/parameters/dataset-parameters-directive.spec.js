/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Dataset parameters directive', () => {
	let scope;
	let createElement;
	let element;
	const translations = {
		DATASET_PARAMETERS: 'Dataset parameters',
		DATASET_PARAMETERS_ENCODING: 'Encoding',
		DATASET_PARAMETERS_SEPARATOR: 'Separator',
		FILE_DETAILS_LINES: '{{records}} lines',
		FILE_DETAILS_LIMIT: 'cut at {{records}} lines',
		NAME: 'Name',
		SIZE: 'Size',
		OTHER: 'Other',
	};

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', translations);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(angular.mock.module('data-prep.dataset-parameters'));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new();
		scope.validate = jasmine.createSpy('validation');
		scope.configuration = {
			separators: [
				{ label: ';', value: ';' },
				{ label: ',', value: ',' },
				{ label: '<space>', value: ' ' },
				{ label: '<tab>', value: '\t' },
			],
			encodings: ['UTF-8', 'UTF-16', 'ISO-8859-1'],
		};
		scope.parameters = { separator: ';', encoding: 'UTF-8' };

		createElement = () => {
			const html = '<dataset-parameters ' +
				'processing="processing" ' +
				'dataset="dataset" ' +
				'on-parameters-change="validate(dataset, parameters)" ' +
				'configuration="configuration" ' +
				'parameters="parameters"' +
				'display-nb-lines="displayNbLines"></dataset-parameters>';

			element = angular.element(html);
			$compile(element)(scope);
			scope.$digest();

			return element;
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('render', () => {
		it('should render title', () => {
			// when
			createElement();

			// then
			expect(element.find('.dataset-parameters-title').eq(0).text()).toBe(translations.DATASET_PARAMETERS);
		});

		it('should render dataset name', () => {
			// given
			scope.dataset = {
				id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
				name: 'US States',
				records: '3',
			};
			scope.displayNbLines = true;

			// when
			createElement();

			// then
			expect(element.find('.dataset-name-group').eq(0).text().trim().replace(/[\s]+/g, ' '))
				.toBe(`${translations.NAME} ${scope.dataset.name}`);
		});

		it('should render nb lines', () => {
			// given
			scope.dataset = {
				id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
				name: 'US States',
				records: '3',
			};
			scope.displayNbLines = true;

			// when
			createElement();

			// then
			expect(element.find('.line-number-group').eq(0).text().trim().replace(/[\s]+/g, ' '))
				.toBe(`${translations.SIZE} ${translations.FILE_DETAILS_LINES.replace('{{records}}', scope.dataset.records)}`);
		});

		it('should render cut lines number when dataset is truncated', () => {
			// given
			scope.dataset = {
				id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
				name: 'US States',
				records: '3',
				limit: 50,
			};
			scope.displayNbLines = true;

			// when
			createElement();

			// then
			expect(element.find('.line-number-group').eq(0).text().trim().replace(/[\s]+/g, ' '))
				.toBe(`${translations.SIZE} ${translations.FILE_DETAILS_LIMIT.replace('{{records}}', scope.dataset.limit)}`);
		});

		it('should not render dataset nb lines', () => {
			// given
			scope.dataset = {
				id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
				name: 'US States',
				records: '3',
				limit: 50,
			};
			scope.displayNbLines = false;

			// when
			createElement();

			// then
			expect(element.find('#line-number').length).toBe(0);
		});

		it('should render encodings', () => {
			// when
			createElement();

			// then
			const encodingContainer = element.find('.encodings-group').eq(0);
			expect(encodingContainer.find('label').eq(0).text().trim()).toBe(translations.DATASET_PARAMETERS_ENCODING);

			const encodingOptions = encodingContainer.find('select > option');
			expect(encodingOptions.length).toBe(3);
			expect(encodingOptions.eq(0).attr('value')).toBe('string:UTF-8');
			expect(encodingOptions.eq(0).text()).toBe('UTF-8');
			expect(encodingOptions.eq(1).attr('value')).toBe('string:UTF-16');
			expect(encodingOptions.eq(1).text()).toBe('UTF-16');
			expect(encodingOptions.eq(2).attr('value')).toBe('string:ISO-8859-1');
			expect(encodingOptions.eq(2).text()).toBe('ISO-8859-1');
		});

		describe('separator', () => {
			it('should NOT render separators on NON csv', () => {
				// given
				scope.dataset = { type: 'other' };

				// when
				createElement();

				// then
				expect(element.find('.separator-group').length).toBe(0);
			});

			it('should NOT render text enclosure and escape character on NON csv', () => {
				// given
				scope.dataset = { type: 'other' };

				// when
				createElement();

				// then
				expect(element.find('.text-enclosure-group').length).toBe(0);
				expect(element.find('.escape-character-group').length).toBe(0);
			});

			it('should render separators on csv dataset', () => {
				// given
				scope.dataset = { type: 'text/csv' };

				// when
				createElement();

				// then
				const separatorContainer = element.find('.separator-group').eq(0);
				expect(separatorContainer.find('label').eq(0).text().trim()).toBe(translations.DATASET_PARAMETERS_SEPARATOR);

				const separatorOptions = separatorContainer.find('select > option');
				expect(separatorOptions.length).toBe(5);
				expect(separatorOptions.eq(0).attr('value')).toBe('');
				expect(separatorOptions.eq(0).text()).toBe(translations.OTHER);
				expect(separatorOptions.eq(1).attr('value')).toBe('string:;');
				expect(separatorOptions.eq(1).text()).toBe(';');
				expect(separatorOptions.eq(2).attr('value')).toBe('string:,');
				expect(separatorOptions.eq(2).text()).toBe(',');
				expect(separatorOptions.eq(3).attr('value')).toBe('string: ');
				expect(separatorOptions.eq(3).text()).toBe('<space>');
				expect(separatorOptions.eq(4).attr('value')).toBe('string:\t');
				expect(separatorOptions.eq(4).text()).toBe('<tab>');
			});

			it('should render custom separator input only when separator is not in the configuration list', () => {
				// given
				scope.dataset = { type: 'text/csv' };
				createElement();

				const separatorContainer = element.find('.separator-group').eq(0);
				expect(separatorContainer.find('select').length).toBe(1);

				// when
				scope.parameters.separator = '|';
				scope.$digest();

				// then
				expect(separatorContainer.find('input').length).toBe(1);
			});

			it('should render separators on csv dataset', () => {
				// given
				scope.dataset = { type: 'text/csv' };

				// when
				createElement();

				// then
				expect(element.find('.separator-group').length).toBe(1);
			});

			it('should render text enclosure and escape character on csv dataset', () => {
				// given
				scope.dataset = { type: 'text/csv' };

				// when
				createElement();

				// then
				expect(element.find('.text-enclosure-group').length).toBe(1);
				expect(element.find('.escape-character-group').length).toBe(1);
			});
		});

		describe('button', () => {
			it('should enable button when processing is falsy', () => {
				// given
				scope.processing = false;

				// when
				createElement();

				// then
				expect(element.find('button').attr('disabled')).toBeFalsy();
			});

			it('should disable button when processing is truthy', () => {
				// given
				scope.processing = true;

				// when
				createElement();

				// then
				expect(element.find('button').attr('disabled')).toBeTruthy();
			});
		});
	});

	describe('validation', () => {
		it('should call validation callback on form submit', () => {
			// given
			scope.dataset = { id: '54a146cf854b54', type: 'text/csv' };
			scope.parameters.separator = '|';
			scope.parameters.encoding = 'UTF-16';
			createElement();

			expect(scope.validate).not.toHaveBeenCalled();

			// when
			element.find('button').click();

			// then
			expect(scope.validate).toHaveBeenCalledWith(
				{ id: '54a146cf854b54', type: 'text/csv' },
				{ separator: '|', encoding: 'UTF-16' }
			);
		});
	});
});
