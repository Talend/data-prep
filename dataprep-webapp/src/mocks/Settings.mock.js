/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const settingsMock = {
	views: {
		appheaderbar: {
			app: 'Data Preparation',
			brandLink: {
				title: 'Talend Data Preparation',
				onClick: 'menu:home',
			},
			actions: ['onboarding:preparation', 'modal:feedback', 'external:help'],
			userMenuActions: {
				id: 'user-menu',
				name: 'Mike Tuchen',
				icon: 'icon-profile',
				menu: ['user:logout'],
			},
		},
		"breadcrumb": {
			"maxItems": 5,
			"onItemClick": "menu:folders"
		},
		"sidepanel": {
			"onToggleDock": "sidepanel:toggle",
			"actions": [
				{
					"label": "Preparations",
					"icon": "icon-lab-mix",
					"onClick": "menu:preparations"
				},
				{
					"label": "Datasets",
					"icon": "icon-dataset",
					"onClick": "menu:datasets"
				}
			]
		},
	},
	actions: {
		'menu:preparations': {
			id: 'menu:preparations',
			name: 'Preparations',
			icon: 'icon-lab-mix',
			type: '@@router/GO',
			payload: {
				method: 'go',
				args: [
					'nav.index.preparations',
				],
			},
		},
		'menu:datasets': {
			id: 'menu:datasets',
			name: 'Datasets',
			icon: 'icon-copy-duplicate-files',
			type: '@@router/GO',
			payload: {
				method: 'go',
				args: [
					'nav.index.datasets',
				],
			},
		},
		"menu:folders": {
			"id": "menu:folders",
			"name": "folders",
			"icon": "icon-folder",
			"type": "@@router/GO_FOLDER",
			"payload": {
				"method": "go",
				"args": [
					"nav.index.preparations"
				]
			}
		},
		"sidepanel:toggle": {
			"id": "sidepanel:toggle",
			"name": "Click here to toggle the side panel",
			"icon": "",
			"type": "@@sidepanel/TOGGLE",
			"payload": {
				"method": "toggleHomeSidepanel",
				"args": []
			}
		},
		'user:logout': {
			id: 'user:logout',
			name: 'Logout',
			icon: 'icon-logout',
			type: '@@user/logout',
			payload: {
				method: 'logout',
			},
		},
		'onboarding:preparation': {
			id: 'onboarding:preparation',
			name: 'Click here to discover the application',
			icon: 'icon-student-user-2',
			type: '@@onboarding/START_TOUR',
			payload: {
				method: 'startTour',
				args: [
					'preparation',
				],
			},
		},
		'modal:feedback': {
			id: 'modal:feedback',
			name: 'Send feedback to Talend',
			icon: 'icon-bubbles-talk-1',
			type: '@@modal/SHOW',
			payload: {
				method: 'showFeedback',
			},
		},
		'external:help': {
			id: 'external:help',
			name: 'Open Online Help',
			icon: 'icon-help',
			type: '@@external/OPEN_WINDOW',
			payload: {
				method: 'open',
				args: [
					'https://help.talend.com/pages/viewpage.action?pageId=266307043&utm_medium=dpdesktop&utm_source=header',
				],
			},
		},
	},
};

export default settingsMock;
