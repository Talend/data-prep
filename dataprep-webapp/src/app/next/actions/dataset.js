import { REDIRECT_WINDOW } from '../constants/actions';

function open(event, { id }) {
	return {
		type: REDIRECT_WINDOW,
		payload: {
			url: `${window.location.origin}/#/playground/dataset?datasetid=${id}`,
		},
	};
}

export default {
	open,
};
