export function open(action) {
	if (action.payload) {
		window.open(action.payload.url, '_blank');
	}
}

export function redirect(action) {
	if (action.payload) {
		window.location.assign(action.payload.url);
	}
}
