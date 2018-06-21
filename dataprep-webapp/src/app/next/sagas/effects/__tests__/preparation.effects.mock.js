import Immutable from 'immutable';

export const STATE = [
	{
		author: 'author0',
		className: 'list-item-preparation0',
		datasetName: 'dataSetName0',
		icon: 'talend-dataprep',
		id: 'id0',
		name: 'name0',
		nbSteps: 1,
		type: 'preparation',
	},
	{
		author: 'author1',
		className: 'list-item-preparation1',
		datasetName: 'dataSetName1',
		icon: 'talend-dataprep',
		id: 'id1',
		name: 'name1',
		nbSteps: 2,
		type: 'preparation',
	},
	{
		author: 'author2',
		className: 'list-item-preparation2',
		datasetName: 'dataSetName2',
		icon: 'talend-dataprep',
		id: 'id2',
		name: 'name2',
		nbSteps: 1,
		type: 'preparation',
	},
];

export const IMMUTABLE_STATE = Immutable.fromJS(STATE);
