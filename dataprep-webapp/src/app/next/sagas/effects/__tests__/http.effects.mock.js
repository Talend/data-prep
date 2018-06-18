import Immutable from 'immutable';


export const getPayload = status => ({
	error: {
		stack: {
			status,
		},
	},
});

export const STATE = {
	uris: {
		login: 'rofl',
	},
};

export const IMMUTABLE_STATE = Immutable.fromJS(STATE);
