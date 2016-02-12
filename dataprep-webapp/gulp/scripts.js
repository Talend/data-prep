'use strict';

var path = require('path');
var gulp = require('gulp');
var conf = require('./conf');
var argv = require('yargs').argv;

var browserSync = require('browser-sync');
var webpack = require('webpack-stream');

var $ = require('gulp-load-plugins')();


function webpackWrapper(watch, test, callback) {
    var webpackOptions = {
        watch: watch,
        module: {
            preLoaders: [{test: /\.js$/, exclude: /node_modules/, loader: 'eslint-loader'}],
            loaders: [{test: /\.js$/, exclude: /node_modules/, loaders: ['ng-annotate', 'babel-loader']}]
        },
        output: {filename: test ? 'index.module.spec.js' : 'index.module.js'}
    };

    if (watch) {
        webpackOptions.devtool = 'inline-source-map';
    }

    var webpackChangeHandler = function (err, stats) {
        if (err) {
            conf.errorHandler('Webpack')(err);
        }
        $.util.log(stats.toString({
            colors: $.util.colors.supportsColor,
            chunks: false,
            hash: false,
            version: false
        }));
        browserSync.reload();
        if (watch) {
            watch = false;
            callback();
        }
    };

    var sources = [];
    if (test) {
        var specFiles = argv.folder ? '/app/**/' + argv.folder + '/*.spec.js' : '/app/**/*.spec.js';
        sources.push(path.join(conf.paths.src, specFiles));
    }
    else {
        sources.push(path.join(conf.paths.src, '/app/**/*module.js'));
    }

    return gulp.src(sources)
        .pipe(webpack(webpackOptions, null, webpackChangeHandler))
        .pipe(gulp.dest(path.join(conf.paths.tmp, '/serve/app')));
}

gulp.task('scripts', function () {
    return webpackWrapper(false, false);
});

gulp.task('scripts:watch', function (callback) {
    return webpackWrapper(true, false, callback);
});

gulp.task('scripts:test', ['scripts'], function () {
    return webpackWrapper(false, true);
});

gulp.task('scripts:test-watch', ['scripts:watch'], function (callback) {
    return webpackWrapper(true, true, callback);
});
