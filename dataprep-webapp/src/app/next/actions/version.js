import { FETCH_VERSION } from '../constants/actions';

export function fetch() {
	return { type: FETCH_VERSION };
}

export default {
	fetch,
};
