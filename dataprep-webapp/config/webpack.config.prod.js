const configure = require('./webpack.config');

module.exports = function(env) {
	return configure({
		dashboard: env && env.dashboard,
		devServer: true,
		env: 'prod',
		entryOutput: true,
		minify: true,
		stripComments: true,
	});
};
