module.exports = require('./webpack.config')({
	env: 'dev',
	debug: true,
	devServer: true,
	devtool: 'eval',
	linter: true,
	stripComments: true,
});
