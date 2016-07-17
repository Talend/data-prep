'use strict';

var path = require('path');
var gulp = require('gulp');
var conf = require('./conf');

var browserSync = require('browser-sync');
var webpack = require('webpack-stream');

var $ = require('gulp-load-plugins')();

function webpackWrapper(watch, callback) {
    var webpackOptions = {
        watch: watch,
        babel: {
            presets: ['es2015'],
            plugins: ['transform-object-rest-spread']
        },
        module: {
            preLoaders: [{test: /\.js$/, exclude: /node_modules/, loader: 'eslint-loader'}],
            loaders: [{test: /\.js$/, exclude: /node_modules/, loaders: ['ng-annotate', 'babel-loader']}]
        },
        output: {filename: 'index.module.js'},
        devtool : watch ? 'eval' : 'source-map'
    };

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

    var sources = [path.join(conf.paths.src, '/app/**/*module.js')];

    return gulp.src(sources)
        .pipe(webpack(webpackOptions, null, webpackChangeHandler))
        .pipe(gulp.dest(path.join(conf.paths.tmp, '/serve/app')));
}

gulp.task('scripts', function () {
    return webpackWrapper(false);
});

gulp.task('scripts:watch', function (callback) {
    return webpackWrapper(true, callback);
});
