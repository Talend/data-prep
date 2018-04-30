import { FETCH_VERSION } from '../constants';

export function fetch() {
	return { type: FETCH_VERSION };
}

export default {
	fetch,
};
