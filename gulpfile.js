var gulp        = require('gulp');
var less        = require('gulp-less');
var path        = require('path');
var browserSync = require('browser-sync').create();
//var scp         = require('gulp-scp');
var scp         = require('scp');
var sitemap     = require('gulp-sitemap');
var nunjucks    = require('gulp-nunjucks-render');
var htmlLint    = require('gulp-html-lint');


var scpconfig = {
  file: 'public/*',
  user: 'peter',
  host: 'ziggy.vagnerr.com',
  port: 22,
  path: '/home/peter/www/dev.vagnerr.com/docroot'
}

gulp.task('deploy', function () {

  // using scp directly rather than gulp-scp as gulp-scp
  // doesnt properly handle the recursive file structure
  // NOTE: this does NOT remove files from the remote server
  //       will neeed to switch to using rsync in future.
  scp.send( scpconfig, function (err) {
    if (err) console.log(err);
    else console.log('Files transferred.');
  })


});

// define the default task and add the watch task to it
gulp.task('default', ['watch']);

gulp.task('less', function () {
  return gulp.src('src/less/**/*.less')
    .pipe(less({
      paths: [ path.join(__dirname, 'less', 'includes') ]
    }))
    .pipe(gulp.dest('./public/css'));
});


gulp.task('nunjucks', function() {
  // Gets .html and .nunjucks files in pages
  return gulp.src('src/pages/**/*.+(html|nunjucks)')
  // Renders template with nunjucks
  .pipe(nunjucks({
      path: ['src/templates']
    }))
  // output files in app folder
  .pipe(gulp.dest('public'))
});

gulp.task('browserSync', function() {
  browserSync.init({
    server: {
      baseDir: 'public/'
    },
  })
})

gulp.task('sitemap', function () {
    gulp.src('public/**/*.html', {
            read: false
        })
        .pipe(sitemap({
            siteUrl: 'https://www.vagnerr.com'
        }))
        .pipe(gulp.dest('./public'));
});

gulp.task('static', function () {
   gulp.src('src/**/*.{html,jpg,png}')
    .pipe(gulp.dest('./public'));
});



gulp.task('htmllint', function() {
    return gulp.src('public/**/*.html')
        .pipe(htmlLint())
        .pipe(htmlLint.format())
        .pipe(htmlLint.failOnError());
});


gulp.task('watch',
          ['browserSync', 'less', 'static','sitemap'],   // Init the public dir on first run
          function(){
  gulp.watch('src/less/**/*.less', ['less', browserSync.reload]);
  // Other watchers
  // Reloads the browser whenever HTML or JS files change
  gulp.watch('src/*.{html,jpg,png}', ['static', 'sitemap',browserSync.reload]);
  gulp.watch('public/js/**/*.js', browserSync.reload);
})

