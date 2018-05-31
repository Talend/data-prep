import { default as help } from './help.saga';
import { default as preparation } from './preparation.saga';
import { default as search } from './search.saga';
import { default as bootstrap } from './bootstrap.saga';
import { default as httpHandler } from './http.saga';
import { default as redirect } from './window.saga';

export default {
	help: Object.keys(help).map(k => help[k]),
	search: Object.keys(search).map(k => search[k]),
	redirect: Object.keys(redirect).map(k => redirect[k]),
	preparation: Object.keys(preparation).map(k => preparation[k]),
	bootstrap,
	httpHandler,
};
