import { SUCCESS_NOTIFICATION, ERROR_NOTIFICATION, WARNING_NOTIFICATION } from '../constants/actions';

function success(event, { title, message }) {
	return {
		type: SUCCESS_NOTIFICATION,
		payload: {
			title,
			message,
		},
	};
}
function error(event, { title, message }) {
	return {
		type: ERROR_NOTIFICATION,
		payload: {
			title,
			message,
		},
	};
}
function warning(event, { title, message }) {
	return {
		type: WARNING_NOTIFICATION,
		payload: {
			title,
			message,
		},
	};
}

export default {
	success,
	error,
	warning,
};
