import { default as bootstrap } from './bootstrap.saga';
import { default as help } from './help.saga';
import { default as http } from './http.saga';
import { default as preparation } from './preparation.saga';
import { default as redirect } from './redirect.saga';
import { default as search } from './search.saga';
import { default as notification } from './notification.saga';

export default {
	bootstrap,
	help,
	http,
	preparation,
	redirect,
	search,
	notification,
};
