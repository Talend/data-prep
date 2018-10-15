/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('URL service', () => {
	beforeEach(angular.mock.module('data-prep.services.utils'));

	describe('build', () => {
		it('should return the original url if there are no parameters', inject((UrlService) => {
			const url = 'http://www.test.net';

			expect(UrlService.build(url)).toBe(url);
		}));

		it('should return the url with GET parameters', inject((UrlService) => {
			const url = 'http://www.test.net';
			const parameters = {
				a: 1,
				b: 2,
				c: 3,
			};

			expect(UrlService.build(url, parameters)).toBe(`${url}?a=1&b=2&c=3`);
		}));

		it('should also work if GET parameters are already present', inject((UrlService) => {
			const url = 'http://www.test.net?oh=yeah';
			const parameters = {
				a: 1,
				b: 2,
				c: 3,
			};

			expect(UrlService.build(url, parameters)).toBe(`${url}&a=1&b=2&c=3`);
		}));

		it('should encode special chars', inject((UrlService) => {
			const url = 'http://www.test.net?oh=yeah';
			const parameters = {
				tést: 'àçéèìõù',
				àçéèìõù: 'lol',
			};

			expect(UrlService.build(url, parameters)).toBe(
				`${url}&t%C3%A9st=%C3%A0%C3%A7%C3%A9%C3%A8%C3%AC%C3%B5%C3%B9&%C3%A0%C3%A7%C3%A9%C3%A8%C3%AC%C3%B5%C3%B9=lol`
			);
		}));
	});

	describe('extract', () => {
		it('should return an empty object if the url does not contain parameters', inject((UrlService) => {
			const url = 'http://www.test.net';

			expect(UrlService.extract(url)).toEqual({});
		}));

		it('should return an object that contains the GET parameters', inject((UrlService) => {
			const url = 'http://www.test.net?a=test_a&b=test_b&c=test_c';

			expect(UrlService.extract(url)).toEqual({
				a: 'test_a',
				b: 'test_b',
				c: 'test_c',
			});
		}));
	});
});
