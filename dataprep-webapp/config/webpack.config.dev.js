module.exports = require('./webpack.config')({
	env: 'dev',
	debug: true,
	devServer: true,
	devtool: 'eval-source-map',
	linter: true,
	stripComments: true,
});
