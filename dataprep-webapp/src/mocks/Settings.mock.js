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
		breadcrumb: {
			maxItems: 5,
			onItemClick: 'menu:folders',
		},
		sidepanel: {
			onToggleDock: 'sidepanel:toggle',
			actions: ['menu:preparations', 'menu:datasets'],
		},
		'listview:folders': {
			list: {
				titleProps: {
					onClick: 'menu:folders',
				},
			},
		},
		'listview:preparations': {
			didMountActionCreator: 'preparations:fetch',
			list: {
				columns: [
					{ key: 'name', label: 'Name' },
					{ key: 'author', label: 'Author' },
					{ key: 'creationDate', label: 'Created' },
					{ key: 'lastModificationDate', label: 'Last change' },
					{ key: 'dataset', label: 'Dataset' },
					{ key: 'nbLines', label: 'Nb lines' },
					{ key: 'nbSteps', label: 'Nb steps' },
				],
				items: [],
				titleProps: {
					iconKey: 'icon',
					key: 'name',
					onClick: 'menu:playground:preparation',
				},
			},
			toolbar: {
				sortBy: [
					{ id: 'name', name: 'Name' },
					{ id: 'date', name: 'Creation Date' },
				],
				actions: ['preparation:create:folder'],
				onClickAdd: 'preparation:create',
				onSelectDisplayMode: 'preparation:display-mode',
				onSelectSortBy: 'preparation:sort',
				searchLabel: 'Find a preparation',
			},
		},
	},
	actions: {
		'menu:preparations': {
			id: 'menu:preparations',
			name: 'Preparations',
			icon: 'talend-dataprep',
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
			icon: 'talend-cluster',
			type: '@@router/GO',
			payload: {
				method: 'go',
				args: ['nav.index.datasets'],
			},
		},
		'menu:folders': {
			id: 'menu:folders',
			name: 'folders',
			icon: 'talend-folder',
			type: '@@router/GO_FOLDER',
			payload: {
				method: 'go',
				args: ['nav.index.preparations'],
			},
		},
		'menu:playground:preparation': {
			id: 'menu:playground:preparation',
			name: 'Preparation Playground',
			icon: 'talend-dataprep',
			type: '@@router/GO_PREPARATION',
			payload: {
				method: 'go',
				args: ['playground.preparation'],
			},
		},
		'sidepanel:toggle': {
			id: 'sidepanel:toggle',
			name: 'Click here to toggle the side panel',
			icon: '',
			type: '@@sidepanel/TOGGLE',
			payload: {
				method: 'toggleHomeSidepanel',
				args: [],
			},
		},
		'onboarding:preparation': {
			id: 'onboarding:preparation',
			name: 'Click here to discover the application',
			icon: 'talend-board',
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
			icon: 'talend-bubbles',
			type: '@@modal/SHOW',
			payload: {
				method: 'showFeedback',
			},
		},
		'external:help': {
			id: 'external:help',
			name: 'Open Online Help',
			icon: 'talend-question-circle',
			type: '@@external/OPEN_WINDOW',
			payload: {
				method: 'open',
				args: [
					'https://help.talend.com/pages/viewpage.action?pageId=266307043&utm_medium=dpdesktop&utm_source=header',
				],
			},
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
		'preparation:display-mode': {
			id: 'preparation:display-mode',
			name: 'Change preparation display mode',
			icon: '',
			type: '@@preparation/DISPLAY_MODE',
			payload: {
				method: 'setPreparationsDisplayMode',
				args: [],
			},
		},
		'preparation:sort': {
			id: 'preparation:sort',
			name: 'Change preparation sort',
			icon: '',
			type: '@@preparation/SORT',
			payload: {
				method: 'setPreparationsSortFromIds',
				args: [],
			},
		},
		'preparation:create': {
			id: 'preparation:create',
			name: 'Create a preparation',
			icon: 'talend-dataprep',
			type: '@@preparation/CREATE',
			payload: {
				method: 'togglePreparationCreator',
				args: [],
			},
		},
		'preparation:create:folder': {
			id: 'preparation:create:folder',
			name: 'Create a folder',
			icon: 'talend-folder',
			type: '@@preparation/CREATE_FOLDER',
			payload: {
				method: 'createFolder',
				args: [],
			},
		},
		'preparations:fetch': {
			id: 'preparations:fetch',
			name: 'Fetch preparations from current folder',
			icon: 'talend-dataprep',
			type: '@@preparation/FETCH',
			payload: {
				method: 'init',
				args: [],
			},
		},
		'preparation:copy-move': {
			id: 'preparation:copy-move',
			name: 'Copy/Move preparation',
			icon: 'talend-copy_dataset',
			type: '@@preparation/COPY_MOVE',
			payload: {
				method: 'copyMove',
				args: [],
			},
		},
		'preparation:remove': {
			id: 'preparation:remove',
			name: 'Remove preparation',
			icon: 'talend-delete',
			type: '@@preparation/REMOVE',
			payload: {
				method: 'remove',
				args: [],
			},
		},
		'preparation:remove:folder': {
			id: 'preparation:remove:folder',
			name: 'Remove folder',
			icon: 'talend-trash',
			type: '@@preparation/REMOVE_FOLDER',
			payload: {
				method: 'removeFolder',
				args: [],
			},
		},
	},
};

export default settingsMock;
