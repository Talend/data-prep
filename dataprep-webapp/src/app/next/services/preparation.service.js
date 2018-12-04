function _adapt(value) {
	return (value || '').toString();
}

function sort(by, asc) {
	return (a, b) => {
		const at = a.get('type');
		const bt = b.get('type');
		const an = a.get(by);
		const bn = b.get(by);

		return (asc ? 1 : -1) * _adapt(at).localeCompare(bt) || _adapt(an).localeCompare(bn);
	};
}

export default {
	sort,
};
