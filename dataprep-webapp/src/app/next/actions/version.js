import { FETCH_VERSION } from '../constants/actions';

function fetch() {
	return { type: FETCH_VERSION };
}

export default {
	fetch,
};
