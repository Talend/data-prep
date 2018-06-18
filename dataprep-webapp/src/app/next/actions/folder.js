import { OPEN_FOLDER } from '../constants/actions';

function open(event, { id }) {
	return {
		type: OPEN_FOLDER,
		id,
		cmf: {
			routerPush: `/preparations/${id}`,
		},
	};
}

export default {
	open,
};
