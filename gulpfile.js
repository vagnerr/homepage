var gulp = require('gulp');
var less = require('gulp-less');
var path = require('path');
var browserSync = require('browser-sync').create();
var scp = require('gulp-scp');

var config = {
  host: 'ziggy.vagnerr.com',
  port: 22,
  username: 'peter',
  //privateKey: fs.readFileSync('/Users/zensh/.ssh/id_rsa')
}

gulp.task('deploy', function () {
    gulp.src('public/*')
        .pipe(scp({
            host: 'ziggy.vagnerr.com',
            user: 'peter',
            port: 22,
            path: '/home/peter/www/dev.vagnerr.com/docroot'
        }));
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

gulp.task('browserSync', function() {
  browserSync.init({
    server: {
      baseDir: 'public/'
    },
  })
})

gulp.task('watch', ['browserSync', 'less'], function(){
  gulp.watch('src/less/**/*.less', ['less', browserSync.reload]);
  // Other watchers
  // Reloads the browser whenever HTML or JS files change
  gulp.watch('public/*.html', browserSync.reload);
  gulp.watch('public/js/**/*.js', browserSync.reload);
})