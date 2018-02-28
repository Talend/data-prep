import { SEARCH } from '../constants';

export function search(event, { term }) {
	return {
		type: SEARCH,
		payload: term,
	};
}
