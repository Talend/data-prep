const webpack = require('webpack');
const ReactCMFWebpackPlugin = require('@talend/react-cmf-webpack-plugin');

const config = require('./webpack.config');

config.plugins = config.plugins.concat([
	new ReactCMFWebpackPlugin({
		quiet: true,
	}),
	new webpack.DefinePlugin({
		'process.env.NODE_ENV': JSON.stringify('production'),
	}),
	new webpack.optimize.UglifyJsPlugin({
		cache: true,
		compress: {
			warnings: false,
		},
		sourceMap: true,
		comments: false,
		mangle: true,
		minimize: true,
	}),
]);

module.exports = config;
