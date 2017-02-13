const appConf = require('./app.conf.js');
const getLicense = require('./license');
const SASS_DATA = require('./sass.conf');

const path = require('path');
const webpack = require('webpack');
const DashboardPlugin = require('webpack-dashboard/plugin');
const autoprefixer = require('autoprefixer');

const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const SassLintPlugin = require('sasslint-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

const extractCSS = new ExtractTextPlugin({ filename: 'styles/[name]-[hash].css' });

const INDEX_TEMPLATE_PATH = path.resolve(__dirname, '../src/index.html');
const STYLE_PATH = path.resolve(__dirname, '../src/app/index.scss');
const INDEX_PATH = path.resolve(__dirname, '../src/app/index-module.js');
const VENDOR_PATH = path.resolve(__dirname, '../src/vendor.js');
const BUILD_PATH = path.resolve(__dirname, '../build');

const CHUNKS_ORDER = ['vendor', 'style', 'app'];

function getDefaultConfig(options) {
	return {
		module: {
			rules: [
				{
					test: /\.js$/,
					use: [
						{ loader: 'ng-annotate-loader' },
						{ loader: 'babel-loader', options: { cacheDirectory: true } },
					],
					exclude: /node_modules/,
				},
				{
					test: /\.css$/,
					use: extractCSS.extract(getCommonStyleLoaders()),
					exclude: /react-talend-/,
				},
				{
					test: /\.scss$/,
					use: extractCSS.extract(getSassLoaders()),
					exclude: /react-talend-/,
				},
				// css moodules local scope
				{
					test: /\.scss$/,
					use: extractCSS.extract(getSassLoaders(true)),
					include: /react-talend-/,
				},
				{ test: /\.(png|jpg|jpeg|gif)$/, loader: 'url-loader', options: { mimetype: 'image/png' } },
				{
					test: /\.html$/,
					use: [
						{ loader: 'ngtemplate-loader' },
						{ loader: 'html-loader' },
					],
					exclude: INDEX_TEMPLATE_PATH,
				},
				{ test: /\.woff(2)?(\?v=\d+\.\d+\.\d+)?$/, loader: 'url-loader', options: { name: '/assets/fonts/[name].[ext]', limit: 10000, mimetype: 'application/font-woff' } },
				{ test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: 'url-loader', options: { name: '/assets/fonts/[name].[ext]', limit: 10000, mimetype: 'image/svg+xml' } },
			]
		},
		plugins: [
			extractCSS,
			new webpack.ProvidePlugin({
				$: 'jquery',
				jQuery: 'jquery',
				'window.jQuery': 'jquery',
			}),
			// for compatibility, needed for some loaders
			new webpack.LoaderOptionsPlugin({
				options: {
					context: path.join(__dirname, '../src'),
					output: {
						path: BUILD_PATH,
					},
				}
			})
		],
		cache: true,
		devtool: options.devtool,
	};
}

function getCommonStyleLoaders(enableModules) {
	const cssOptions = enableModules ?
		{ sourceMap: true, modules: true, importLoaders: 1, localIdentName: '[name]__[local]___[hash:base64:5]' } :
		{};
	return [
		{ loader: 'css-loader', options: cssOptions },
		{ loader: 'postcss-loader', options: { plugins: () => [autoprefixer({ browsers: ['last 2 versions'] })] } },
		{ loader: 'resolve-url-loader' }
	];
}

function getSassLoaders(enableModules) {
	return getCommonStyleLoaders(enableModules).concat({ loader: 'sass-loader', options: { sourceMap: true, data: SASS_DATA } });
}

function addDashboardPlugin(config) {
	config.plugins.push(new DashboardPlugin());
}

function addProdEnvPlugin(config) {
	config.plugins.push(
		new webpack.DefinePlugin({
			'process.env': {
				NODE_ENV: JSON.stringify('production'),
			},
		})
	);
}

function addCoverageConfig(config) {
	config.module.rules.push({
		test: /\.js$/,
		enforce: 'pre',
		loader: 'isparta-loader',
		exclude: [/node_modules/, /\.spec\.js$/],
		options: {
			embedSource: true,
			noAutoWrap: true,
			babel: {
				presets: ['es2015']
			}
		},
	});
}

function addDevServerConfig(config) {
	config.devServer = {
		port: appConf.port,
		host: appConf.host,
		watchOptions: {
			aggregateTimeout: 300,
			poll: 1000,
		},
		compress: true,
		inline: true,
		contentBase: BUILD_PATH,
	};
}

function addFilesConfig(config) {
	config.entry = {
		vendor: VENDOR_PATH,
		style: STYLE_PATH,
		app: INDEX_PATH,
	};
	config.output = {
		path: BUILD_PATH,
		filename: '[name]-[hash].js',
	};
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
				force: (options.env === 'dev'),
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
			}
		}),

		/*
		 * Plugin: BannerPlugin
		 * Description: Inject a banner on top of the output file
		 * This is used to inject the licence.
		 *
		 * See: https://webpack.github.io/docs/list-of-plugins.html#bannerplugin
		 */
		new webpack.BannerPlugin({ banner: getLicense() }),

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

function addMinifyConfig(config) {
	config.plugins.push(
		new webpack.optimize.UglifyJsPlugin(),
		new webpack.LoaderOptionsPlugin({ minimize: true })
	);
}

function addStripCommentsConfig(config) {
	config.module.rules.push({
		test: /\.js$/,
		enforce: 'pre',
		use: 'stripcomment-loader',
		exclude: [/node_modules/, /\.spec\.js$/],
	});
}

function addLinterConfig(config) {
	config.module.rules.push({
		test: /src\/.*\.js$/,
		enforce: 'pre',
		loader: 'eslint-loader',
		exclude: /node_modules/,
		options: { configFile: path.resolve(__dirname, '../.eslintrc') },
	});

	// config.plugins.push(new SassLintPlugin({
	//     glob: 'src/app/**/*.s?(a|c)ss',
	// }));
}

/*
 {
 coverage: (true | false)            // configure coverage instrumenter
 dashboard: (true | false)           // enable webpack dashboard plugin
 devtool: 'inline-source-map',       // source map type
 devServer: (true | false),          // configure webpack-dev-server
 entryOutput: (true | false),        // configure entry and output files and plugins to generate full app. For example, test with karma doesn't need that, as the files are managed by karma.
 env: ('dev' | 'prod' | 'test'),     // the environment
 linter: (true | false),             // enable eslint and sass-lint
 minify: (true | false),             // enable minification/uglification
 stripComments: (true | false),      // remove comments
 }
 */
module.exports = (options) => {
	const config = getDefaultConfig(options);


	if (options.coverage) {
		addCoverageConfig(config);
	}

	if (options.dashboard) {
		addDashboardPlugin(config);
	}

	if (options.devServer) {
		addDevServerConfig(config);
	}

	if (options.entryOutput) {
		addFilesConfig(config);
		addPlugins(config, options);
	}

	if (options.env === 'prod') {
		addProdEnvPlugin(config);
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
