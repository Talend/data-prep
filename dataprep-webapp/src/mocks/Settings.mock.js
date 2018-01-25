/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const settingsMock = {
	views: {
		appheaderbar: {
			logo: {
				name: 'Talend',
				onClick: 'menu:home',
			},
			brand: {
				label: 'Data Preparation',
				onClick: 'menu:home',
			},
			search: {
				onToggle: 'search:toggle',
				onBlur: 'search:toggle',
				onChange: 'search:all',
				onSelect: {
					preparation: 'menu:playground:preparation',
					dataset: 'dataset:open',
					folder: 'menu:folders',
					documentation: 'external:documentation',
				},
				onKeyDown: 'search:focus',
				debounceTimeout: 300,
			},
			userMenu: 'user:menu',
			help: 'headerbar:help',
			products: 'products:menu',
		},
		'appheaderbar:playground': {
			logo: {
				name: 'Talend',
				onClick: 'menu:home',
			},
			brand: {
				label: 'Data Preparation',
				onClick: 'menu:home',
			},
			search: {
				onToggle: 'search:toggle',
				onBlur: 'search:toggle',
				onChange: 'search:all',
				onSelect: {
					preparation: 'menu:playground:preparation',
					dataset: 'dataset:open',
					folder: 'menu:folders',
					documentation: 'external:documentation',
				},
				onKeyDown: 'search:focus',
				debounceTimeout: 300,
			},
			userMenu: 'user:menu',
			help: 'headerbar:help',
			products: 'products:menu',
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
					{ key: 'datasetName', label: 'Dataset' },
					{ key: 'nbSteps', label: 'Nb steps' },
				],
				items: [],
				itemProps: {
					classNameKey: 'className',
				},
				titleProps: {
					displayModeKey: 'displayMode',
					iconKey: 'icon',
					key: 'name',
					onClick: 'menu:playground:preparation',
					onEditCancel: 'inventory:cancel-edit',
					onEditSubmit: 'preparation:submit-edit',
				},
				sort: {
					field: 'name',
					isDescending: false,
					onChange: 'preparation:sort',
				},
			},
			toolbar: {
				sort: {
					options: [
						{ id: 'name', name: 'Name' },
						{ id: 'date', name: 'Creation Date' },
					],
					onChange: 'preparation:sort',
				},
				display: {
					displayModes: [
						'table',
						'large',
					],
					onChange: 'preparation:display-mode',
				},
				actionBar: {
					actions: {
						left: ['preparation:create', 'preparation:folder:create'],
					},
				},
				searchLabel: 'Find a preparation',
			},
		},
		'listview:datasets': {
			didMountActionCreator: 'datasets:fetch',
			list: {
				columns: [
					{ key: 'name', label: 'Name' },
					{ key: 'statusActions', label: 'Actions', hideHeader: true, type: 'actions' },
					{ key: 'author', label: 'Author' },
					{ key: 'creationDate', label: 'Created' },
					{ key: 'nbRecords', label: 'Rows' },
				],
				items: [],
				itemProps: {
					classNameKey: 'className',
				},
				titleProps: {
					displayModeKey: 'displayMode',
					iconKey: 'icon',
					key: 'name',
					onClick: 'dataset:open',
					onEditCancel: 'inventory:cancel-edit',
					onEditSubmit: 'dataset:submit-edit',
				},
				sort: {
					field: 'name',
					isDescending: false,
					onChange: 'dataset:sort',
				},
			},
			toolbar: {
				sort: {
					options: [
						{ id: 'name', name: 'Name' },
						{ id: 'date', name: 'Creation Date' },
					],
					onChange: 'dataset:sort',
				},
				display: {
					displayModes: [
						'table',
						'large',
					],
					onChange: 'preparation:display-mode',
				},
				actionBar: {
					actions: {
						left: ['dataset:create'],
					},
				},
				searchLabel: 'Find a dataset',
			},
		},
	},
	actions: {
		'menu:preparations': {
			id: 'menu:preparations',
			name: 'Preparations',
			icon: 'talend-dataprep',
			type: '@@router/GO_CURRENT_FOLDER',
			payload: {
				method: 'go',
				args: ['home.preparations'],
			},
		},
		'menu:datasets': {
			id: 'menu:datasets',
			name: 'Datasets',
			icon: 'talend-datastore',
			type: '@@router/GO',
			payload: {
				method: 'go',
				args: ['home.datasets'],
			},
		},
		'menu:folders': {
			id: 'menu:folders',
			name: 'Folders',
			icon: 'talend-folder',
			type: '@@router/GO_FOLDER',
			payload: {
				method: 'go',
				args: ['home.preparations'],
			},
		},
		'menu:playground:preparation': {
			id: 'menu:playground:preparation',
			name: 'Open Preparation',
			icon: 'talend-dataprep',
			type: '@@router/GO_PREPARATION',
			payload: {
				method: 'go',
				args: ['playground.preparation'],
			},
		},
		'list:dataset:preparations': {
			id: 'list:dataset:preparations',
			name: 'Open preparation',
			icon: 'talend-dataprep',
			displayMode: 'dropdown',
			items: 'preparations',
			dynamicAction: 'menu:playground:preparation',
			staticActions: ['dataset:open'],
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
					'https://help.talend.com/#/search/all?filters=EnrichPlatform%253D%2522Talend+Data+Preparation%2522%2526EnrichVersion%253D%25222.1%2522&utm_medium=dpdesktop&utm_source=header',
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
		'product:producta': {
			displayMode: 'ActionSettings',
			id: 'product:producta',
			name: 'Product A',
			icon: 'talend-logo-a',
			type: '@@external/OPEN_WINDOW',
			payload: {
				args: ['https://producta.tlnd'],
				method: 'open',
			},
		},
		'product:productb': {
			displayMode: 'ActionSettings',
			id: 'product:productb',
			name: 'Product B',
			icon: 'talend-logo-b',
			type: '@@external/OPEN_WINDOW',
			payload: {
				args: ['https://productb.tlnd'],
				method: 'open',
			},
		},
		'product:productc': {
			displayMode: 'ActionSettings',
			id: 'product:productc',
			name: 'Product C',
			icon: 'talend-logo-c',
			type: '@@external/OPEN_WINDOW',
			payload: {
				args: ['https://productc.tlnd'],
				method: 'open',
			},
		},
		'products:menu': {
			displayMode: 'dropdown',
			id: 'products:menu',
			name: 'Apps',
			icon: 'talend-launcher',
			staticActions: [
				'product:producta',
				'product:productb',
				'product:productc',
			],
		},
		'dataset:open': {
			id: 'dataset:open',
			name: 'Open dataset',
			icon: '',
			type: '@@dataset/OPEN',
		},
		'dataset:display-mode': {
			id: 'dataset:display-mode',
			name: 'Change dataset display mode',
			icon: '',
			type: '@@inventory/DISPLAY_MODE',
			payload: {
				method: 'setDatasetsDisplayMode',
				args: [],
			},
		},
		'dataset:sort': {
			id: 'dataset:sort',
			name: 'Change dataset sort',
			icon: '',
			type: '@@dataset/SORT',
		},
		'dataset:submit-edit': {
			id: 'dataset:submit-edit',
			name: 'Submit name edition',
			icon: 'talend-check',
			type: '@@dataset/SUBMIT_EDIT',
		},
		'dataset:remove': {
			id: 'dataset:remove',
			name: 'Remove dataset',
			icon: 'talend-trash',
			type: '@@dataset/REMOVE',
			payload: {
				method: 'remove',
				args: [],
			},
		},
		'dataset:clone': {
			id: 'dataset:clone',
			name: 'Copy dataset',
			icon: 'talend-files-o',
			type: '@@dataset/CLONE',
			payload: {
				method: 'clone',
				args: [],
			},
		},
		'dataset:favorite': {
			id: 'dataset:favorite',
			name: 'Add dataset in your favorite list',
			icon: 'talend-star',
			type: '@@dataset/FAVORITE',
			payload: {
				method: 'toggleFavorite',
				args: [],
			},
		},
		'dataset:update': {
			id: 'dataset:update',
			name: 'Update dataset ',
			icon: 'talend-file-move',
			type: '@@dataset/UPDATE',
			payload: {
				method: '',
				args: [],
			},
		},
		'dataset:create': {
			id: 'dataset:create',
			name: 'Add Dataset',
			icon: 'talend-plus',
			type: '@@dataset/CREATE',
			bsStyle: 'primary',
			displayMode: 'splitDropdown',
			items: [],
		},
		'datasets:fetch': {
			id: 'datasets:fetch',
			name: 'Fetch all datasets',
			icon: 'talend-dataprep',
			type: '@@dataset/DATASET_FETCH',
			payload: {
				method: 'init',
				args: [],
			},
		},
		'preparation:display-mode': {
			id: 'preparation:display-mode',
			name: 'Change preparation display mode',
			icon: '',
			type: '@@inventory/DISPLAY_MODE',
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
			name: 'Create preparation',
			icon: 'talend-plus',
			type: '@@preparation/CREATE',
			payload: {
				method: 'togglePreparationCreator',
				args: [],
			},
		},
		'preparation:folder:create': {
			id: 'preparation:folder:create',
			name: 'Create folder',
			icon: 'talend-folder',
			type: '@@preparation/CREATE',
			payload: {
				method: 'toggleFolderCreator',
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
		'inventory:edit': {
			id: 'inventory:edit',
			name: 'Edit name',
			icon: 'talend-pencil',
			type: '@@inventory/EDIT',
			payload: {
				method: 'enableInventoryEdit',
				args: [],
			},
		},
		'inventory:cancel-edit': {
			id: 'inventory:cancel-edit',
			name: 'Cancel name edition',
			icon: 'talend-crossbig',
			type: '@@inventory/CANCEL_EDIT',
			payload: {
				method: 'disableInventoryEdit',
				args: [],
			},
		},
		'preparation:submit-edit': {
			id: 'preparation:submit-edit',
			name: 'Submit name edition',
			icon: 'talend-check',
			type: '@@preparation/VALIDATE_EDIT',
			payload: {
				method: '',
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
		'preparation:folder:remove': {
			id: 'preparation:folder:remove',
			name: 'Remove folder',
			icon: 'talend-trash',
			type: '@@preparation/FOLDER_REMOVE',
			payload: {
				method: 'removeFolder',
				args: [],
			},
		},
		'search:toggle': {
			id: 'search:toggle',
			icon: 'talend-search',
			type: '@@search/TOGGLE',
		},
		'search:all': {
			id: 'search:all',
			type: '@@search/ALL',
		},
		'search:focus': {
			id: 'search:focus',
			type: '@@search/FOCUS',
		},
		'external:documentation': {
			id: 'external:documentation',
			type: '@@external/OPEN_WINDOW',
			icon: 'talend-question-circle',
			name: 'Documentation',
			payload: {
				method: 'open',
			},
		},
		'user:menu': {
			id: 'user:menu',
			name: 'anonymousUser',
			icon: 'talend-user-circle',
			displayMode: 'dropdown',
			staticActions: ['user:logout'],
		},
		'version:toggle': {
			displayMode: 'ActionSettings',
			id: 'version:toggle',
			name: 'Toggle version',
			type: '@@version/VERSION_TOGGLE',
			payload: {
				method: 'toggleVersion',
			},
		},
		'version:select': {
			displayMode: 'ActionSettings',
			id: 'version:select',
			name: 'Select version',
			type: '@@router/GO_PREPARATION_READ_ONLY',
			payload: {
				args: 'version',
				method: 'go',
			},
		},
		'headerbar:help': {
			displayMode: 'splitDropdown',
			id: 'headerbar:help',
			name: 'Help',
			icon: 'talend-question-circle',
			type: '@@external/HELP',
			items: [
				'modal:feedback',
				'onboarding:preparation',
				'modal:about',
			],
			action: 'external:help',
		},
		'modal:about': {
			displayMode: 'ActionSettings',
			id: 'modal:about',
			name: 'About Data Preparation',
			icon: 'talend-info-circle',
			type: '@@modal/SHOW',
			payload: {
				method: 'toggleAbout',
			},
		},
	},
	uris: {
		api: '/api',
		apiAggregate: '/api/aggregate',
		apiDatasets: '/api/datasets',
		apiUploadDatasets: '/api/datasets',
		apiExport: '/api/export',
		apiFolders: '/api/folders',
		apiMail: '/api/mail',
		apiPreparations: '/api/preparations',
		apiPreparationsPreview: '/api/preparations/preview',
		apiSearch: '/api/search',
		apiSettings: '/api/settings',
		apiTcomp: '/api/tcomp',
		apiTransform: '/api/transform',
		apiTypes: '/api/types',
		apiUpgradeCheck: '/api/upgrade/check',
		apiVersion: '/api/version',
	},
	help: {
		versionFacet: 'HELP_VERSION_FACET',
		languageFacet: 'HELP_LANGUAGE_FACET',
		searchUrl: 'HELP_SEARCH_URL',
		fuzzyUrl: 'HELP_FUZZY_URL',
		exactUrl: 'HELP_EXACT_URL',
	},
};

export default settingsMock;
