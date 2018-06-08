import Immutable from 'immutable';

export const PROVIDERS = {
	test: [
		{
			type: 'test',
			label: 'Test',
			icon: 'talend-test',
		},
	],
	mock: [
		{
			type: 'mock',
			label: 'Mock',
			icon: 'talend-mock',
		},
	],
};

export const STATE = [
	{
		title: 'Preparations',
		icon: {
			name: 'talend-dataprep',
			title: 'Preparations',
		},
		suggestions: [
			{
				title: 'test preparation',
				type: 'preparation',
				id: 'id preparation 1',
			},
		],
	},
	{
		title: 'Datasets',
		icon: {
			name: 'talend-datastore',
			title: 'Datasets',
		},
		suggestions: [
			{
				title: 'test dataset',
				type: 'dataset',
				id: 'id dataset 1',
			},
		],
	},
	{
		title: 'Folders',
		icon: {
			name: 'talend-folder',
			title: 'Folders',
		},
		suggestions: [
			{
				title: 'test folder',
				type: 'folder',
				id: 'id folder 1',
			},
		],
	},
	{
		title: 'Documentation',
		icon: {
			name: 'talend-question-circle',
			title: 'documentation',
		},
		suggestions: [
			{
				type: 'documentation',
				description: 'description 1',
				title: 'title 1',
				url: 'url 1',
			},
		],
	},
	{
		title: 'Nop',
		icon: {
			name: 'talend-nop',
			title: 'nop',
		},
		suggestions: [
			{
				type: 'nop',
				description: 'description 1',
				title: 'title 1',
				url: 'url 1',
			},
		],
	},
];

export const IMMUTABLE_STATE = Immutable.fromJS(STATE);
