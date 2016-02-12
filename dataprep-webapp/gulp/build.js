'use strict';

var path = require('path');
var gulp = require('gulp');
var conf = require('./conf');
var licences = require('./licences.js');

var $ = require('gulp-load-plugins')({
    pattern: ['gulp-*', 'main-bower-files', 'uglify-save-license', 'del']
});

gulp.task('partials', function () {
    return gulp.src([
            path.join(conf.paths.src, '/app/**/*.html'),
            path.join(conf.paths.tmp, '/serve/app/**/*.html')
        ])
        .pipe($.minifyHtml({
            empty: true,
            spare: true,
            quotes: true
        }))
        .pipe($.angularTemplatecache('templateCacheHtml.js', {
            module: 'data-prep',
            root: 'app'
        }))
        .pipe(gulp.dest(conf.paths.tmp + '/partials/'));
});

gulp.task('html', ['inject', 'partials'], function () {
    var partialsInjectFile = gulp.src(path.join(conf.paths.tmp, '/partials/templateCacheHtml.js'), {read: false});
    var partialsInjectOptions = {
        starttag: '<!-- inject:partials -->',
        ignorePath: path.join(conf.paths.tmp, '/partials'),
        addRootSlash: false
    };

    var htmlFilter = $.filter('*.html', {restore: true});
    var htmlIndexFilter = $.filter('index.html', {restore: true});
    var jsFilter = $.filter('**/*.js', {restore: true});
    var jsAppFilter = $.filter('**/app-*.js', {restore: true});
    var cssFilter = $.filter('**/*.css', {restore: true});
    var cssAppFilter = $.filter('**/app-*.css', {restore: true});
    var assets;

    return gulp.src(path.join(conf.paths.tmp, '/serve/*.html'))
        .pipe($.inject(partialsInjectFile, partialsInjectOptions))
        .pipe(assets = $.useref.assets())
        .pipe($.rev())
        .pipe(jsFilter)
        .pipe($.sourcemaps.init())
        .pipe($.uglify({
            mangle: {
                //workerFn[0-9] are preserved words that won't be mangled during uglification
                //those can be used to pass external functions to the web worker
                except: ['workerFn0', 'workerFn1', 'workerFn2', 'workerFn3', 'workerFn4', 'workerFn5', 'workerFn6', 'workerFn7', 'workerFn8', 'workerFn9']
            },
            preserveComments: function(node, comment) {
                return $.uglifySaveLicense(node, comment) && comment.value.indexOf('Talend Inc') < 0;
            }
        })).on('error', conf.errorHandler('Uglify'))
        .pipe(jsAppFilter)
        .pipe($.header(licences.js, {year: new Date().getFullYear()}))
        .pipe(jsAppFilter.restore)
        .pipe($.sourcemaps.write('maps'))
        .pipe(jsFilter.restore)
        .pipe(cssFilter)
        .pipe($.sourcemaps.init())
        .pipe($.minifyCss({processImport: false}))
        .pipe(cssAppFilter)
        .pipe($.header(licences.css, {year: new Date().getFullYear()}))
        .pipe(cssAppFilter.restore)
        .pipe($.sourcemaps.write('maps'))
        .pipe(cssFilter.restore)
        .pipe(assets.restore())
        .pipe($.useref())
        .pipe($.revReplace())
        .pipe(htmlFilter)
        .pipe($.minifyHtml({
            empty: true,
            spare: true,
            quotes: true,
            conditionals: true
        }))
        .pipe(htmlFilter.restore)
        .pipe(htmlIndexFilter)
        .pipe($.header(licences.html, {year: new Date().getFullYear()}))
        .pipe(htmlIndexFilter.restore)
        .pipe(gulp.dest(path.join(conf.paths.dist, '/')))
        .pipe($.size({title: path.join(conf.paths.dist, '/'), showFiles: true}));
});

// Only applies for fonts from bower dependencies
// Custom fonts are handled by the "other" task
gulp.task('fonts', function () {
    return gulp.src($.mainBowerFiles())
        .pipe($.filter('**/*.{eot,svg,ttf,woff,woff2}'))
        .pipe($.flatten())
        .pipe(gulp.dest(path.join(conf.paths.dist, '/fonts/')));
});

gulp.task('other', function () {
    var fileFilter = $.filter(function (file) {
        return file.stat.isFile();
    });

    return gulp.src([
            path.join(conf.paths.src, '/**/*'),
            path.join('!' + conf.paths.src, '/**/*.{html,css,js,scss}')
        ])
        .pipe(fileFilter)
        .pipe(gulp.dest(path.join(conf.paths.dist, '/')));
});

gulp.task('clean', function () {
    return $.del([path.join(conf.paths.dist, '/'), path.join(conf.paths.tmp, '/')]);
});

function copyWorkerLibs(destination) {
    return gulp.src($.mainBowerFiles())
        .pipe($.filter('**/*.js'))
        .pipe($.flatten())
        .pipe(gulp.dest(destination));
}

gulp.task('worker-libs', function () {
    return copyWorkerLibs(path.join(conf.paths.dist, '/worker/'));
});

gulp.task('worker-libs:dev', function () {
    return copyWorkerLibs(path.join(conf.paths.tmp, '/serve/worker/'));
});

gulp.task('build', ['html', 'fonts', 'other', 'worker-libs']);
