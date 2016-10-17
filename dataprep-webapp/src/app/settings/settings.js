/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const actions = {
	MENU_HOME: 'menu:home',
	APPBAR_ONBOARDING: 'bar:preparation-onboarding',
	APPBAR_FEEDBACK: 'bar:feedback',
	APPBAR_HELP: 'bar:help',
};

const settings = {
	views: {
		appheaderbar: {
			app: 'Data Preparation',
			brandLink: {
				title: 'Talend Data Preparation',
				onClick: actions.MENU_HOME,
			},
			content: [{
				navs: [{
					nav: { pullRight: true },
					navItems: [
						{
							type: 'navItem',
							item: { id: 'onboarding-icon', name: 'ONBOARDING', onClick: actions.APPBAR_ONBOARDING, 'data-icon': 'y' },
						},
						{
							type: 'navItem',
							item: { id: 'message-icon', name: 'FEEDBACK_TOOLTIP', onClick: actions.APPBAR_FEEDBACK, 'data-icon': 'H' },
						},
						{
							type: 'navItem',
							item: { id: 'online-help-icon', name: 'ONLINE_HELP_TOOLTIP', onClick: actions.APPBAR_HELP, 'data-icon': 'l' },
						},
					],
				}],
			}],
		},
	},
};

export default settings;
