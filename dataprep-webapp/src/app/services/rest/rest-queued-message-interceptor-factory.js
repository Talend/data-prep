/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const ACCEPTED_CODE = 202;
const LOOP_DELAY = 1000;
const FOLLOWED_STATUS = ['NEW', 'RUNNING'];
const FAILED_STATUS = 'FAILED';
const CANCELLED_STATUS = 'CANCELLED';

const METHODS = {
	POST: 'POST',
	GET: 'GET',
	HEAD: 'HEAD',
};
const ALLOWED_METHODS = [METHODS.POST, METHODS.GET, METHODS.HEAD];
const NOOP = () => {};

/**
 * @ngdoc service
 * @name data-prep.services.rest.service:RestQueuedMessageHandler
 * @description Queued message interceptor
 */
export default function RestQueuedMessageHandler($q, $injector, $timeout, RestURLs, MessageService) {
	'ngInject';

	function checkStatus(url) {
		const $http = $injector.get('$http');

		return new Promise((resolve, reject) => {
			$http.get(url)
				.then(({ data }) => (FOLLOWED_STATUS.includes(data.status) ? reject : resolve)(data));
		});
	}

	function loop(url, callback) {
		function checker(url) {
			return checkStatus(url)
				.then((data) => {
					(callback || NOOP)(data);
					return data;
				})
				.catch((data) => {
					(callback || NOOP)(data);
					return $timeout(LOOP_DELAY).then(() => checker(url));
				});
		}
		return checker(url);
	}

	return {
		/**
		 * @ngdoc method
		 * @name response
		 * @methodOf data-prep.services.rest.service:RestQueuedMessageHandler
		 * @param {object} response - the intercepted response
		 * @description If a 202 occurs, loop until the status change from NEW/RUNNING to anything else
		 */
		response(response) {
			const { headers, config, status } = response;
			if (status === ACCEPTED_CODE && ALLOWED_METHODS.includes(config.method) && !config.async) {
				return loop(`${RestURLs.context}${headers('Location')}`, config.statusCallback)
					.then((data) => {
						const $http = $injector.get('$http');

						switch (data.status) {
						case FAILED_STATUS:
							MessageService.error('SERVER_ERROR_TITLE', 'GENERIC_ERROR');
						case CANCELLED_STATUS:
							return $q.reject({});
						default:
							return data.result.downloadUrl ? $http({
								method: config.method === METHODS.HEAD ? METHODS.HEAD : METHODS.GET,
								url: `${RestURLs.context}${data.result.downloadUrl}`,
							}) : $q.resolve(data);
						}
					});
			}

			return $q.resolve(response);
		},
	};
}
