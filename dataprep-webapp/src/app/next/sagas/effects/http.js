import http from '@talend/react-cmf/lib/sagas/http';

export default http.create({
	headers: {
		Accept: 'application/json, text/plain, */*',
		'Content-Type': 'application/json',
	},
});
