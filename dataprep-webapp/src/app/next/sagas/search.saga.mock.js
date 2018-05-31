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
						},
						{
							type: 'folder',
							title: 'R2',
						},
						{
							type: 'documentation',
							title: 'R3',
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
