const path = require('path');
const webpack = require('webpack');
const autoprefixer = require('autoprefixer');

const TalendHTML = require('@talend/html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const extractCSS = new ExtractTextPlugin({ filename: '[name]-[hash].css' });

const APP_CONF = require('./app.conf');
const LICENSE_BANNER = require('./license');
const SASS_DATA = require('./sass.conf');
const CHUNKS_ORDER = ['vendor', 'style', 'app'];

const INDEX_TEMPLATE_PATH = path.resolve(__dirname, '../src/index.html');
const STYLE_PATH = path.resolve(__dirname, '../src/app/index.scss');
const VENDOR_PATH = path.resolve(__dirname, '../src/vendor.js');

const AppLoader = require('@talend/react-components/lib/AppLoader/constant').default;

const isTestMode = process.env.NODE_ENV === 'test';

function getCommonStyleLoaders(enableModules) {
	let cssOptions = {};
	if (enableModules) {
		cssOptions = {
			sourceMap: true,
			modules: true,
			importLoaders: 1,
			localIdentName: '[name]__[local]___[hash:base64:5]',
		};
	}
	return [
		{ loader: 'css-loader', options: cssOptions },
		{
			loader: 'postcss-loader',
			options: { sourceMap: true, plugins: () => [autoprefixer({ browsers: ['last 2 versions'] })] },
		},
		{ loader: 'resolve-url-loader' },
	];
}

function getSassLoaders(enableModules) {
	return getCommonStyleLoaders(enableModules)
		.concat({
			loader: 'sass-loader',
			options: { sourceMap: true, data: SASS_DATA },
		});
}

const config = {
	entry: {
		vendor: [
			'babel-polyfill',
			VENDOR_PATH,
		],
		style: STYLE_PATH,
		app: ['babel-polyfill', 'whatwg-fetch', './src/app/index-module.js'],
	},
	output: {
		path: `${__dirname}/../build`,
		publicPath: '/',
		filename: '[name]-[hash].js',
	},
	module: {
		loaders: [
			{
				test: /\.js$/,
				exclude: /node_modules/,
				use: [
					{ loader: 'ng-annotate-loader' },
					{ loader: 'babel-loader' },
				],
			},
			{
				test: /\.css$/,
				use: isTestMode ? { loader: 'null-loader' } : extractCSS.extract(getCommonStyleLoaders()),
				exclude: /@talend/,
			},
			{
				test: /\.scss$/,
				use: isTestMode ? { loader: 'null-loader' } : extractCSS.extract(getSassLoaders()),
				exclude: /@talend/,
			},
			// css modules local scope
			{
				test: /\.scss$/,
				use: isTestMode ? { loader: 'null-loader' } : extractCSS.extract(getSassLoaders(true)),
				include: /@talend/,
			},
			{
				test: /\.html$/,
				use: [
					{ loader: 'ngtemplate-loader' },
					{ loader: 'html-loader' },
				],
				exclude: INDEX_TEMPLATE_PATH,
			},
			{
				test: /\.(png|jpg|jpeg|gif)$/,
				loader: isTestMode ? 'null-loader' : 'url-loader',
				options: { mimetype: 'image/png' },
			},
			{
				test: /\.woff(2)?(\?v=\d+\.\d+\.\d+)?$/,
				loader: isTestMode ? 'null-loader' : 'file-loader',
				options: {
					name: '[name].[ext]',
					limit: 10000,
					mimetype: 'application/font-woff',
					publicPath: '/',
					outputPath: 'assets/fonts/',
				},
			},
			{
				test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
				loader: isTestMode ? 'null-loader' : 'url-loader',
				options: { name: '/assets/fonts/[name].[ext]', limit: 10000, mimetype: 'image/svg+xml' },
			},
		],
	},
	resolve: {
		symlinks: false,
	},
	plugins: [
		extractCSS,
		new webpack.ProvidePlugin({
			$: 'jquery',
			jQuery: 'jquery',
			jquery: 'jquery',
			'window.jQuery': 'jquery',
			moment: 'moment',
		}),
		new webpack.LoaderOptionsPlugin({
			minimize: true,
		}),
	],
	node: {
		fs: 'empty',
	},
};

if (!isTestMode) {
	config.plugins = config.plugins.concat([
		CopyWebpackPlugin([
			{ from: 'src/assets/images', to: 'assets/images' },
			{ from: 'src/assets/config/config.json', to: 'assets/config' },
		]),
		new HtmlWebpackPlugin({
			filename: './index.html',
			template: INDEX_TEMPLATE_PATH,
			title: APP_CONF.title,
			rootElement: APP_CONF.rootElement,
			rootModule: APP_CONF.rootModule,
			inject: 'body',
			loader: AppLoader.APP_LOADER,
			// ensure loding order vendor/style/app
			chunksSortMode: (a, b) => {
				const aOrder = CHUNKS_ORDER.indexOf(a.names[0]);
				const bOrder = CHUNKS_ORDER.indexOf(b.names[0]);
				if (aOrder > bOrder) {
					return 1;
				}
				if (aOrder < bOrder) {
					return -1;
				}
				return 0;
			},
		}),
		new TalendHTML({
			loadCSSAsync: true,
			appLoaderIcon: APP_CONF.icon,
		}),
		new webpack.BannerPlugin({
			banner: LICENSE_BANNER,
		}),
		new webpack.optimize.CommonsChunkPlugin({
			name: 'vendor',
			minChunks: Infinity,
		}),
	]);
}

module.exports = config;
