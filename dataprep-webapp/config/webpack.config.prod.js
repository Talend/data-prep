module.exports = require('./webpack.config')({
	env: 'prod',
	devServer: true,
	minify: true,
	stripComments: true,
});
