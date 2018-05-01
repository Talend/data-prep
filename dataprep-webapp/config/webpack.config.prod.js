const configure = require('./webpack.config');

module.exports = function() {
	return configure({
		env: 'prod',
		entryOutput: true,
		minify: true,
		stripComments: true,
		devServer: true,
	});
};
