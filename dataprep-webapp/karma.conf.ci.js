var baseConfig = require('./karma.conf.js');

module.exports = function(config){
    // Load base config
    baseConfig(config);

    // Override base config
    config.set({
        browserNoActivityTimeout: 60000,
        reporters: ['progress', 'coverage', 'junit'],
        coverageReporter : {
 			type : 'cobertura',
			dir : 'coverage/',
  			file : 'coverage.xml'
		}
    });
};