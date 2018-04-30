const path = require('path');
const webpack = require('webpack');
const ReactCMFWebpackPlugin = require('@talend/react-cmf-webpack-plugin');

const appConfig = require('./../src/assets/config/config.json');
const config = require('./webpack.config');

config.devtool = 'eval-source-map';

config.module.loaders.push({
	test:	/src\/.*\.js$/,
	enforce: 'pre',
	loader: 'eslint-loader',
	exclude: /node_modules/,
	options: { configFile: path.resolve(__dirname, '../.eslintrc') },
});

config.plugins.push(
	new webpack.DefinePlugin({
		'process.env.NODE_ENV': JSON.stringify('development'),
	}),
	new ReactCMFWebpackPlugin({
		watch: true,
	}),
);

config.watchOptions = {
	aggregateTimeout: 300,
	poll: 1000,
};

const SERVER_URL = 'http://localhost:8888';

config.devServer = {
	port: 3000,
	contentBase: path.resolve(__dirname, '../build'),
	setup(app) {
		app.get('/assets/config/config.json', function (req, res) {
			appConfig.serverUrl = SERVER_URL;
			res.json(appConfig);
		});
	},
	proxy: {
		'/api/v1/stream-websocket': {
			target: process.env.API_URL || 'http://localhost',
			ws: true,
		},
		'/api': {
			target: process.env.API_URL || 'http://localhost',
			changeOrigin: true,
			secure: false,
		},
	},
	stats: 'errors-only',
	historyApiFallback: true,
};

module.exports = config;
