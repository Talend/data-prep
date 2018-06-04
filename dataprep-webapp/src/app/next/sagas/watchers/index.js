import { default as preparation } from './preparation.saga';
import { default as bootstrap } from './bootstrap.saga';
import { default as redirect } from './redirect.saga';
import { default as help } from './help.saga';
import { default as http } from './http.saga';


export default {
	redirect: Object.keys(redirect).map(k => redirect[k]),
	help: Object.keys(help).map(k => help[k]),
	preparation,
	bootstrap,
	http,
};
