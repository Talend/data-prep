import { OPEN_DATASET } from '../constants/actions';


function open(event, { type, id }) {
	return {
		type: OPEN_DATASET,
		payload: {
			type, id,
		},
	};
}
export default {
	open,
};
