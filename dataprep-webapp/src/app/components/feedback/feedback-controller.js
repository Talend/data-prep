/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default function FeedbackCtrl(state, $translate, HelpService, FeedbackRestService, MessageService, StateService, StorageService) {
	'ngInject';

	const vm = this;
	vm.isSendingFeedback = false;
	vm.state = state;
	vm.HelpService = HelpService;

	$translate(
		[
			'FEEDBACK_TYPE_BUG',
			'FEEDBACK_TYPE_IMPROVEMENT',
			'FEEDBACK_SEVERITY_CRITICAL',
			'FEEDBACK_SEVERITY_MAJOR',
			'FEEDBACK_SEVERITY_MINOR',
			'FEEDBACK_SEVERITY_TRIVIAL',
		])
		.then((translations) => {
			vm.feedbackTypes = [
				{ name: translations.FEEDBACK_TYPE_BUG, value: 'BUG' },
				{ name: translations.FEEDBACK_TYPE_IMPROVEMENT, value: 'IMPROVEMENT' },
			];
			vm.feedbackSeverities = [
				{ name: translations.FEEDBACK_SEVERITY_CRITICAL, value: 'CRITICAL' },
				{ name: translations.FEEDBACK_SEVERITY_MAJOR, value: 'MAJOR' },
				{ name: translations.FEEDBACK_SEVERITY_MINOR, value: 'MINOR' },
				{ name: translations.FEEDBACK_SEVERITY_TRIVIAL, value: 'TRIVIAL' },
			];
		});

	resetForm();

	function resetForm() {
		vm.feedback = {
			title: '',
			mail: StorageService.getFeedbackUserMail(),
			severity: 'MINOR',
			type: 'BUG',
			description: '',
		};
	}

	vm.sendFeedback = function sendFeedback() {
		vm.feedbackForm.$commitViewValue();
		vm.isSendingFeedback = true;
		FeedbackRestService.sendFeedback(vm.feedback)
			.then(() => {
				StorageService.saveFeedbackUserMail(vm.feedback.mail);
				resetForm();
				StateService.hideFeedback();
				MessageService.success('FEEDBACK_SENT_TITLE', 'FEEDBACK_SENT_CONTENT');
			})
			.finally(() => {
				vm.isSendingFeedback = false;
			});
	};
}
