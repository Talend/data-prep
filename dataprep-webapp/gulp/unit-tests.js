'use strict';

var gulp = require('gulp');
var $ = require('gulp-load-plugins')();
var wiredep = require('wiredep');
var runSequence = require('run-sequence');

var fullTestsFiles = [
    'src/*.js',
    'src/{services,components}/**/*-module.js',
    'src/{services,components,mocks}/**/*.js',
    'src/components/**/*.html'
];

var filesToExclude = {
    services: [
        'src/components/**/*.spec.js'
    ],
    components: [
        'src/services/**/*.spec.js',
        'src/components/widgets/**/*.spec.js'
    ],
    widgets: [
        'src/services/**/*.spec.js',
        'src/components/!(widgets)/**/*.spec.js'
    ]
};


var filesToCover = {
    services: 'src/services/**/!(*spec|*mock).js',
    components: 'src/components/!(widgets)/**/!(*spec|*mock).js',
    widgets: 'src/components/widgets/**/!(*spec|*mock).js'
};

function runTests(singleRun, done, karmaConfPath, type) {
    var bowerDeps = wiredep({
        directory: 'bower_components',
        exclude: ['bootstrap-sass-official'],
        dependencies: true,
        devDependencies: true
    });

    var testFiles = bowerDeps.js.concat(fullTestsFiles);

    var preprocessors = {
        'src/**/*.html':['ng-html2js']
    };
    var toCover = type ? filesToCover [type] : 'src/**/!(*spec|*mock).js';
    preprocessors[toCover] = ['coverage'];

    return gulp.src([])
        .pipe($.karma({
            configFile: karmaConfPath,
            action: (singleRun) ? 'run' : 'watch',
            files: testFiles,
            exclude: filesToExclude[type],
            coverageReporter: {
                dir: 'coverage/' + (type || '')
            },
            preprocessors: preprocessors
        }))
        .on('error', function (err) {
            // Make sure failed tests cause gulp to exit non-zero
            throw err;
        });
}

gulp.task('test:components', function (done) {
    return runTests(true /* singleRun */, done, 'karma.conf.js', 'components')
});

gulp.task('test:services', function (done) {
    return runTests(true /* singleRun */, done, 'karma.conf.js', 'services')
});

gulp.task('test:widgets', function (done) {
    return runTests(true /* singleRun */, done, 'karma.conf.js', 'widgets')
});

gulp.task('test:parts', function(done) {
    return runSequence('test:services', 'test:components', 'test:widgets', done);
});

gulp.task('test', function (done) {
    runTests(true /* singleRun */, done, 'karma.conf.js')
});

gulp.task('test:auto', function (done) {
    runTests(false /* singleRun */, done, 'karma.conf.js')
});

gulp.task('test:ci', function (done) {
    runTests(true /* singleRun */, done, 'karma.conf.ci.js')
});
