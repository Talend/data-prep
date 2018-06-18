export const CATEGORIES = [
	{
		type: 'dataset',
		label: 'Datasets',
		icon: 'talend-datastore',
	},
	{
		type: 'preparation',
		label: 'Preparations',
		icon: 'talend-dataprep',
	},
];

export const API_RESULTS = {
	data: JSON.stringify({
		preparation: [
			{
				id: '17e0122b-59f3-4fa5-871c-fc9a3fc2320e',
				name: 'test prep',
			},
		],
		dataset: [
			{
				id: 'ec43a3e8-5fe7-47e6-986f-e27fcafd3a85',
				name: 'test dataset',
			},
		],
	}),
};

export const FORMATTED_RESULTS = [
	{
		icon: {
			name: 'talend-dataprep',
			title: 'Preparations',
		},
		suggestions: [
			{
				id: '17e0122b-59f3-4fa5-871c-fc9a3fc2320e',
				title: 'test prep',
				type: 'preparation',
			},
		],
		title: 'Preparations',
	},
	{
		icon: {
			name: 'talend-datastore',
			title: 'Datasets',
		},
		suggestions: [
			{
				id: 'ec43a3e8-5fe7-47e6-986f-e27fcafd3a85',
				title: 'test dataset',
				type: 'dataset',
			},
		],
		title: 'Datasets',
	},
];
