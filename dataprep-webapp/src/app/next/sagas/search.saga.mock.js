import Immutable from 'immutable';

export const INITIAL_STATE = {
	cmf: {
		collections: new Immutable.Map({
			search: Immutable.fromJS([
				{
					title: 'A',
					suggestions: [
						{
							type: 'preparation',
							title: 'R1',
							id: 666,
						},
						{
							type: 'folder',
							title: 'R2',
							id: 42,
						},
						{
							type: 'documentation',
							title: 'R3',
							url: 'www.doc.org/test',
						},
						{
							type: 'unknown',
							title: 'FAKE',
						},
					],
				},
			]),
		}),
	},
};

export default {
	INITIAL_STATE,
};
