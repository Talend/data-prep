const configure = require('./webpack.config');

module.exports = function(env) {
	return configure({
		dashboard: env && env.dashboard,
		env: 'dev',
		entryOutput: true,
		devServer: true,
		devtool: 'eval-source-map',
		linter: true,
		stripComments: true,
	});
};
