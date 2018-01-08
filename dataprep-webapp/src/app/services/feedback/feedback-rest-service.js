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
 * @name data-prep.services.feedback.service:FeedbackRestService
 * @description Feedback service. This service provide the entry point to feedback
 */
export default class FeedbackRestService {
	constructor($http, RestURLs) {
		'ngInject';
		this.$http = $http;
		this.url = RestURLs.mailUrl;
	}

    /**
     * @ngdoc method
     * @name sendFeedback
     * @methodOf data-prep.services.feedback.service:FeedbackRestService
     * @param {string} feedback The feedback information
     * @description Send a feedback
     */
	sendFeedback(feedback) {
		const request = {
			method: 'PUT',
			url: this.url,
			data: feedback,
		};

		return this.$http(request);
	}
}
