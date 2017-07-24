var gulp = require('gulp');
var less = require('gulp-less');
var path = require('path');
var browserSync = require('browser-sync').create();



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