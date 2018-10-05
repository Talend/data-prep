/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:UrlService
 * @description Provides helpers to manage URLs
 */
export default class UrlService {
	constructor() {
		'ngInject';
	}

	/**
	 * @ngdoc method
	 * @name build
	 * @methodOf data-prep.services.utils.service:UrlService
	 * @param {string} url The base url
	 * @param {object} parameters The parameters
	 * @description Returns a GET url with the given parameters
	 */
	build(url, parameters) {
		if (!parameters || !Object.keys(parameters).length) {
			return url;
		}

		const str = Object.keys(parameters)
			.map(k => `${sanitize(k)}=${sanitize(parameters[k])}`)
			.join('&');
		const separator = url.includes('?') ? '&' : '?';
		return `${url}${separator}${str}`;
	}

	/**
	 * @ngdoc method
	 * @name exctract
	 * @methodOf data-prep.services.utils.service:UrlService
	 * @param {string} url The url with parameters
	 * @description Returns an object that contains the extracted GET parameters
	 */
	extract(url = '') {
		const str = url.split('?')[1];
		const result = {};

		if (str) {
			str.split('&').forEach((p) => {
				const parts = p.split('=');
				result[parts[0]] = parts[1];
			});
		}

		return result;
	}
}


function sanitize(str = '') {
	return encodeURIComponent(str);
}
