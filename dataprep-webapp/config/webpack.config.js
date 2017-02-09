const appConf = require('./app.conf.js');
const getLicense = require('./license');
const SASS_DATA = require('./sass.conf');

const path = require('path');
const webpack = require('webpack');
const autoprefixer = require('autoprefixer');

const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const SassLintPlugin = require('sasslint-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

const extractCSS = new ExtractTextPlugin('styles/[name]-[hash].css');

const INDEX_TEMPLATE_PATH = path.resolve(__dirname, '../src/index.html');
const STYLE_PATH = path.resolve(__dirname, '../src/app/index.scss');
const INDEX_PATH = path.resolve(__dirname, '../src/app/index-module.js');
const VENDOR_PATH = path.resolve(__dirname, '../src/vendor.js');
const BUILD_PATH = path.resolve(__dirname, '../build');

function getDefaultConfig(options) {
	return {
		entry: {
			app: INDEX_PATH,
			vendor: VENDOR_PATH,
			style: STYLE_PATH,
		},
		output: {
			path: BUILD_PATH,
			filename: '[name]-[hash].js',
		},
		module: {
			preLoaders: [],
			loaders: [
				{ test: /\.js$/, loaders: ['ng-annotate', 'babel?cacheDirectory'], exclude: /node_modules/ },
				{ test: /\.(css|scss)$/, loader: extractCSS.extract(['css', 'postcss', 'resolve-url', 'sass?sourceMap']), exclude: /react-talend-/ },
				{ test: /\.(css|scss)$/, loader: extractCSS.extract(['css?sourceMap&modules&importLoaders=1&localIdentName=[name]__[local]___[hash:base64:5]', 'postcss', 'resolve-url', 'sass?sourceMap']),  include: /react-talend-/ }, // css moodules  local scope
				{ test: /\.(png|jpg|jpeg|gif)$/, loader: 'url-loader', query: { mimetype: 'image/png' } },
				{ test: /\.html$/, loaders: ['ngtemplate', 'html'], exclude: INDEX_TEMPLATE_PATH },
				{ test: /\.woff(2)?(\?v=\d+\.\d+\.\d+)?$/, loader: "url?name=/assets/fonts/[name].[ext]&limit=10000&mimetype=application/font-woff" },
				{ test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: "url?name=/assets/fonts/[name].[ext]&limit=10000&mimetype=application/octet-stream" },
				{ test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: "file?name=/assets/fonts/[name].[ext]" },
				{ test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: "url?name=/assets/fonts/[name].[ext]&limit=10000&mimetype=image/svg+xml" },
			]
		},
		plugins: [
			extractCSS,
			new webpack.ProvidePlugin({
				$: 'jquery',
				jQuery: 'jquery',
				'window.jQuery': 'jquery',
			})
		],
		sassLoader: {
			data: SASS_DATA,
		},
		postcss() {
			return [autoprefixer({ browsers: ['last 2 versions'] })];
		},
		cache: true,
		devtool: options.devtool,
		debug: options.debug,
	};
}

function addProdEnvPlugin(config) {
	config.plugins.push(
		new webpack.DefinePlugin({
			'process.env': {
				NODE_ENV: JSON.stringify("production")
			}
		}),
		new webpack.optimize.DedupePlugin(),
		new webpack.optimize.OccurrenceOrderPlugin()
	);
}

function addDevServerConfig(config) {
	config.devServer = {
		port: appConf.port,
		host: appConf.host,
		watchOptions: {
			aggregateTimeout: 300,
			poll: 1000
		},
		inline: true,
		progress: true,
		contentBase: BUILD_PATH,
		outputPath: BUILD_PATH,
	};
}

function addMinifyConfig(config) {
	config.plugins.push(new webpack.optimize.UglifyJsPlugin({
		compress: { warnings: false },
	}));
}

function addStripCommentsConfig(config) {
	config.module.preLoaders.push({ test: /\.js$/, loader: 'stripcomment', exclude: [/node_modules/, /\.spec\.js$/] });
}

function addPlugins(config, options) {
	config.plugins.push(
		/*
		 * Plugin: CopyWebpackPlugin
		 * Description: Copy files and directories in webpack.
		 * Copies project static assets.
		 *
		 * See: https://www.npmjs.com/package/copy-webpack-plugin
		 */
		new CopyWebpackPlugin([
			{ from: 'src/assets', to: 'assets' },
			{
				from: 'src/assets/config/config.mine.json',
				to: 'assets/config/config.json',
				force: (options.env === 'dev')
			},
			{ from: 'src/i18n', to: 'i18n' },
		]),

		/*
		 * Plugin: HtmlWebpackPlugin
		 * Description: Simplifies creation of HTML files to serve your webpack bundles.
		 * This is especially useful for webpack bundles that include a hash in the filename
		 * which changes every compilation.
		 *
		 * See: https://github.com/ampedandwired/html-webpack-plugin
		 */
		new HtmlWebpackPlugin({
			title: appConf.title,
			rootElement: appConf.rootElement,
			rootModule: appConf.rootModule,
			env: options.env,
			template: INDEX_TEMPLATE_PATH,
			inject: 'head',
		}),

		/*
		 * Plugin: BannerPlugin
		 * Description: Inject a banner on top of the output file
		 * This is used to inject the licence.
		 *
		 * See: https://webpack.github.io/docs/list-of-plugins.html#bannerplugin
		 */
		new webpack.BannerPlugin(getLicense()),

		/*
		 * Plugin: webpack.optimize.CommonsChunkPlugin
		 * Description: Identifies common modules and put them into a commons chunk
		 *
		 * See: https://github.com/webpack/docs/wiki/optimization
		 */
		new webpack.optimize.CommonsChunkPlugin({
			name: 'vendor',
			minChunks: Infinity,
		})
	);
}

function addLinterConfig(config) {
	config.eslint = { configFile: path.resolve(__dirname, '../.eslintrc') };
	config.module.preLoaders.push({
		test: /src\/.*\.js$/,
		exclude: /node_modules/,
		loader: 'eslint-loader',
	});

	// config.plugins.push(new SassLintPlugin({
	//     glob: 'src/app/**/*.s?(a|c)ss',
	// }));
}

function addCoverageConfig(config) {
	config.module.preLoaders.push(
		{ test: /\.js$/, loader: 'isparta', exclude: [/node_modules/, /data-prep\//, /\.spec\.js$/] }
	);
	config.isparta = {
		embedSource: true,
		noAutoWrap: true,
		babel: {
			presets: ['es2015'],
		},
	};
}

function removeFilesConfig(config) {
	config.entry = undefined;
	config.output = undefined;
}

/*
 {
 env: ('dev' | 'prod' | 'test'),     // the environment
 debug: (true | false),              // enable debug
 devtool: 'inline-source-map',       // source map type
 devServer: (true | false),          // configure webpack-dev-server and plugins to generate app
 linter: (true | false),             // enable eslint and sass-lint
 minify: (true | false),             // enable minification/uglification
 stripComments: (true | false),      // remove comments
 }
 */
module.exports = (options) => {
	const config = getDefaultConfig(options);

	if (options.env === 'prod') {
		addProdEnvPlugin(config);
	}

	if (options.env === 'test') {
		addCoverageConfig(config);
		removeFilesConfig(config);
	}

	if (options.devServer) {
		addDevServerConfig(config);
		addPlugins(config, options);
	}

	if (options.minify) {
		addMinifyConfig(config);
	}

	if (options.stripComments) {
		addStripCommentsConfig(config);
	}

	if (options.linter) {
		addLinterConfig(config);
	}

	return config;
};
