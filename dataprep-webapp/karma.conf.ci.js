var baseConfig = require('./karma.conf.js');

module.exports = function (config) {
	// Load base config
	baseConfig(config);

	config.coverageReporter.type = 'cobertura';
	config.coverageReporter.dir = 'coverage/';
	config.coverageReporter.file = 'coverage.xml';

	// Override base config
	config.set({
		browserNoActivityTimeout: 120000,
		reporters: ['progress', 'coverage', 'junit'],
	});
};
