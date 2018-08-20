export const RAW_FOLDERS = [
	{
		id: 'L3Rlc3QgZm9sZGVyIDE=',
		name: 'test folder 1',
		ownerId: 'ncomont',
		parentId: 'Lw==',
		creationDate: 1528465267000,
		lastModificationDate: 1528465267000,
		nbPreparations: 0,
		owner: { id: 'ncomont', firstName: 'ncomont', lastName: '', displayName: 'ncomont' },
		sharedFolder: false,
		sharedByMe: false,
		roles: [],
		path: '/test folder 1',
	},
	{
		id: 'L3Rlc3QgZm9sZGVyIDI=',
		name: 'test folder 2',
		ownerId: 'ncomont',
		parentId: 'Lw==',
		creationDate: 1528465283000,
		lastModificationDate: 1528465283000,
		nbPreparations: 0,
		owner: { id: 'ncomont', firstName: 'ncomont', lastName: '', displayName: 'ncomont' },
		sharedFolder: false,
		sharedByMe: false,
		roles: [],
		path: '/test folder 2',
	},
];

export const RAW_PREPARATIONS = [
	{
		id: '17e0122b-59f3-4fa5-871c-fc9a3fc2320e',
		dataSetId: 'ec43a3e8-5fe7-47e6-986f-e27fcafd3a85',
		author: 'ncomont',
		name: 'test prep 1',
		creationDate: 1528377459479,
		lastModificationDate: 1528465288655,
		headId: '097506e0-828c-47ae-90bb-64e4bae613ea',
		steps: ['f6e172c33bdacbc69bca9d32b2bd78174712a171', '097506e0-828c-47ae-90bb-64e4bae613ea'],
		owner: { id: 'ncomont', firstName: 'ncomont', lastName: '', displayName: 'ncomont' },
		allowFullRun: false,
		diff: [{ createdColumns: [] }],
		dataset: {
			dataSetId: 'ec43a3e8-5fe7-47e6-986f-e27fcafd3a85',
			dataSetName: 'test dataset',
			dataSetNbRow: 410,
		},
	},
	{
		id: '28b39366-c87b-423c-b9ea-ec4963dbfb69',
		dataSetId: 'ec43a3e8-5fe7-47e6-986f-e27fcafd3a85',
		author: 'ncomont',
		name: 'test prep 2',
		creationDate: 1528465296854,
		lastModificationDate: 1528465296854,
		headId: 'a91d5c9e-074a-4bea-9f12-16fe238cf8a7',
		steps: ['f6e172c33bdacbc69bca9d32b2bd78174712a171', 'a91d5c9e-074a-4bea-9f12-16fe238cf8a7'],
		owner: { id: 'ncomont', firstName: 'ncomont', lastName: '', displayName: 'ncomont' },
		allowFullRun: false,
		diff: [{ createdColumns: [] }],
		dataset: {
			dataSetId: 'ec43a3e8-5fe7-47e6-986f-e27fcafd3a85',
			dataSetName: 'test dataset',
			dataSetNbRow: 410,
		},
	},
];

export const FORMATTED_FOLDERS = [
	{
		author: 'ncomont',
		className: 'list-item-folder',
		icon: 'talend-folder',
		id: 'L3Rlc3QgZm9sZGVyIDE=',
		name: 'test folder 1',
		type: 'folder',
	},
	{
		author: 'ncomont',
		className: 'list-item-folder',
		icon: 'talend-folder',
		id: 'L3Rlc3QgZm9sZGVyIDI=',
		name: 'test folder 2',
		type: 'folder',
	},
];

export const FORMATTED_PREPARATIONS = [
	{
		author: 'ncomont',
		className: 'list-item-preparation',
		datasetName: 'test dataset',
		icon: 'talend-dataprep',
		id: '17e0122b-59f3-4fa5-871c-fc9a3fc2320e',
		name: 'test prep 1',
		nbSteps: 1,
		type: 'preparation',
	},
	{
		author: 'ncomont',
		className: 'list-item-preparation',
		datasetName: 'test dataset',
		icon: 'talend-dataprep',
		id: '28b39366-c87b-423c-b9ea-ec4963dbfb69',
		name: 'test prep 2',
		nbSteps: 1,
		type: 'preparation',
	},
];


export const RAW_FOLDERS_HIERARCHY = {
	folder: {
		id: 'L3Rlc3QgZm9sZGVyIDE=',
		name: 'test folder 1',
		ownerId: 'ncomont',
		parentId: 'Lw==',
		creationDate: 1528465267000,
		lastModificationDate: 1528465267000,
		nbPreparations: 0,
		owner: { id: 'ncomont', firstName: 'ncomont', lastName: '', displayName: 'ncomont' },
		sharedFolder: false,
		sharedByMe: false,
		roles: [],
		path: '/test folder 1',
	},
	hierarchy: [
		{
			id: 'L3Rlc3QgZm9sZGVyIDI=',
			name: 'test folder 2',
			ownerId: 'ncomont',
			parentId: 'Lw==',
			creationDate: 1528465283000,
			lastModificationDate: 1528465283000,
			nbPreparations: 0,
			owner: { id: 'ncomont', firstName: 'ncomont', lastName: '', displayName: 'ncomont' },
			sharedFolder: false,
			sharedByMe: false,
			roles: [],
			path: '/test folder 2',
		},
	],
};


export const FORMATTED_FOLDERS_HIERARCHY = [
	{
		actionCreator: 'folder:open',
		id: 'L3Rlc3QgZm9sZGVyIDI=',
		text: 'test folder 2',
		title: 'test folder 2',
	},
	{
		actionCreator: 'folder:open',
		id: 'L3Rlc3QgZm9sZGVyIDE=',
		text: 'test folder 1',
		title: 'test folder 1',
	},
];
