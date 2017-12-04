var baseConfig = require('./karma.conf.js');

process.env.CHROME_BIN = require('puppeteer').executablePath();

module.exports = function (config) {
	// Load base config
	baseConfig(config);

	config.coverageReporter.type = 'cobertura';
	config.coverageReporter.dir = 'coverage/';
	config.coverageReporter.file = 'coverage.xml';

	// Override base config
	config.set({
		browsers: ['ChromeHeadless'],
		browserNoActivityTimeout: 120000,
		reporters: ['progress', 'coverage', 'junit'],
	});
};
