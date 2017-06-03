const configure = require('./webpack.config');

module.exports = function() {
	return configure({
		env: 'dev',
		entryOutput: true,
		devServer: true,
		devtool: 'eval-source-map',
		linter: true,
		stripComments: true,
	});
};
