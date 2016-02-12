'use strict';

var path = require('path');
var gulp = require('gulp');
var conf = require('./conf');

var browserSync = require('browser-sync');

function isOnlyChange(event) {
    return event.type === 'changed';
}

gulp.task('watch', ['scripts:watch', 'inject'], function () {

    gulp.watch([path.join(conf.paths.src, '/*.html'), 'bower.json'], ['inject-reload']);

    gulp.watch([
        path.join(conf.paths.src, '/app/**/*.css'),
        path.join(conf.paths.src, '/app/**/*.scss')
    ], function (event) {
        if (isOnlyChange(event)) {
            gulp.start('styles-reload');
        } else {
            gulp.start('inject-reload');
        }
    });


    gulp.watch(path.join(conf.paths.src, '/app/**/*.html'), function (event) {
        browserSync.reload(event.path);
    });

    gulp.watch(path.join(conf.paths.src, '/assets/config/config.mine.json'), function (event) {
        gulp.start('replace-mine');
        browserSync.reload(event.path);
    });
});
