import { SEARCH, SEARCH_SELECT, SEARCH_RESET } from '../constants/actions';

function start({ target }) {
	return { type: SEARCH, payload: target.value };
}

function select(event, { itemIndex, sectionIndex }) {
	return { type: SEARCH_SELECT, payload: { sectionIndex, itemIndex } };
}

function reset() {
	return { type: SEARCH_RESET };
}

export default {
	start,
	select,
	reset,
};
