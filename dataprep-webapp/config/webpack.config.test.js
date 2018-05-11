const config = require('./webpack.config');

config.module.loaders.push({
	test: /\.js$/,
	enforce: 'pre',
	loader: 'isparta-loader',
	exclude: [/node_modules/, /\.spec\.js$/],
	options: {
		embedSource: true,
		noAutoWrap: true,
	},
});

delete config.entry;
delete config.output;

module.exports = config;
