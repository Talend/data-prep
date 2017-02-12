module.exports = require('./webpack.config')({
	env: 'dev',
	devServer: true,
	devtool: 'eval-source-map',
	linter: true,
	stripComments: true,
});
