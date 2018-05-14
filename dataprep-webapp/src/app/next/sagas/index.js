import { default as help } from './help.saga';
import { default as preparation } from './preparation.saga';
// import { default as version } from './version.saga';

export default {
	help: Object.keys(help).map(k => help[k]),
	preparation: Object.keys(preparation).map(k => preparation[k]),
	// version: Object.keys(version).map(k => version[k]),
};
