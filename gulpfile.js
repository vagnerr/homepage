var gulp            = require('gulp');
var less            = require('gulp-less');
var path            = require('path');
var browserSync     = require('browser-sync').create();
var scp             = require('scp');
var sitemap         = require('gulp-sitemap');
var nunjucks        = require('gulp-nunjucks-render');
var htmlLint        = require('gulp-html-lint');
var csslint         = require('gulp-csslint');
var csslintStylish  = require('csslint-stylish');
var bootlint        = require('gulp-bootlint');
var gutil           = require('gulp-util');
var Crawler         = require('simplecrawler');
var sh              = require('sync-exec');
var data            = require('gulp-data');

const fs            = require('fs');

var scpconfig = {
  file: 'public/*',
  user: 'peter',
  host: 'ziggy.vagnerr.com',
  port: 22,
  path: '/home/peter/www/dev.vagnerr.com/docroot'
}

gulp.task('deploy',['sitemap'], function () {  // always rebuild sitemap, just before deploying

  // using scp directly rather than gulp-scp as gulp-scp
  // doesnt properly handle the recursive file structure
  // NOTE: this does NOT remove files from the remote server
  //       will neeed to switch to using rsync in future.
  scp.send( scpconfig, function (err) {
    if (err) console.log(err);
    else console.log('Files transferred.');
  })


});


gulp.task('checklinks', function(cb) {

  var last_referrer="";
  var link_failure=false;
  var offsite_links = {};
  var redirect_links = {};

  var crawler = Crawler("http://localhost:3000/")     //<---------------TODO: PARAMETERISE?
    .on("fetchcomplete", function (queueItem, response) {
        if ( queueItem.host != crawler.host ) {
          //gutil.log("Offsite>>: " + queueItem.url);
          offsite_links[queueItem.url] = queueItem.referrer;
        }
    })
    .on('fetchredirect', function(oldQueueItem, redirectQueueItem, response) {
      //gutil.log("REDIRECT: " + oldQueueItem.url + " > " + redirectQueueItem.url );
      redirect_links[oldQueueItem.url] = redirectQueueItem.url + " ("+oldQueueItem.referrer+") ["+response.statusCode+"]";
    })
    .on('fetch404', function(queueItem, response) {
      if( !link_failure ){
        link_failure=true;
        gutil.log("Broken Links...");
      }
      if( queueItem.referrer != last_referrer ) {
        last_referrer = queueItem.referrer;
        gutil.log("  " + queueItem.referrer + ": ");
      }
      gutil.log('                         ' + queueItem.url + " ["+response.statusCode+"]");

    })
    .on('complete', function(queueItem) {
      gutil.log("Crawl complete...");
      if ( Object.keys(offsite_links).length ) {
        gutil.log( "");
        gutil.log( "Offsite links....");
        for ( var link in offsite_links ) {
          gutil.log( "  " + offsite_links[link] + " > " + link );
        }
      }
      if ( Object.keys(redirect_links).length ) {
        gutil.log( "");
        gutil.log( "Redirects....");
        for ( var link in redirect_links ) {
          gutil.log( "   " + link + ": " + redirect_links[link]);
        }
      }

      cb();
    })
  ;

  crawler.filterByDomain = false;
  crawler.addFetchCondition(function(queueItem, referrerQueueItem, callback) {
      callback(
                null,
                (
                  !queueItem.path.match(/^\/browser-sync/i) &&   // Skip browser-sync links
                  referrerQueueItem.host === crawler.host        // check offsite links (1 hop only)
                )
              );
  });

  crawler.start();


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


function getDataForFile(file) {
  return {
    last_modified: get_last_modified(file)
  };
}

gulp.task('nunjucks', function() {
  // Gets .html and .nunjucks files in pages
  return gulp.src('src/pages/**/*.+(html|nunjucks)')
  // Renders template with nunjucks
  .pipe(data(getDataForFile))
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


function get_last_modified(file) {
      // Calculate the source .nunjucks file and
      // query git for the last commit.
      let cwd = file.cwd;
      let filedir  = path.dirname(file.relative);
      let filebase = path.basename(file.relative, path.extname(file.relative));
      let sourcefile = path.join( cwd, 'src','pages', filedir, filebase + '.nunjucks' )

      if (fs.existsSync(sourcefile)){
        var cmd = 'git log -1 --format=%cI "' + sourcefile + '"';
        return sh(cmd).stdout.toString().trim();
      }
      else {
        console.log( "source file: [" + sourcefile + "] not found for file.relative");
        return null;
      }

};


gulp.task('sitemap', function () {
  gulp.src('public/**/*.html', {
    read: false
  })
  .pipe(sitemap({
    siteUrl: 'https://www.vagnerr.com',
    lastmod: function(file) {
      return get_last_modified(file)
  }}))
  .pipe(gulp.dest('./public'));
});

gulp.task('static', function () {
  gulp.src('src/static/**/*')
    .pipe(gulp.dest('./public'));
});

gulp.task('bootlint', ['nunjucks','static'], function() {
    return gulp.src('public/**/*.html')
        .pipe(bootlint({
          loglevel:     'warning',
          stoponerror:  'true'
        }));
});

gulp.task('htmllint', ['nunjucks','static'], function() {
  return gulp.src('public/**/*.html')
    .pipe(htmlLint())
    .pipe(htmlLint.format())
    .pipe(htmlLint.failOnError());
});

gulp.task('csslint',    ['less'], function()  {
  return gulp.src('public/css/*.css')
    .pipe(csslint())
    .pipe(csslint.formatter(csslintStylish))
    .pipe(csslint.formatter('fail'));
});

gulp.task('test', ['htmllint','bootlint','csslint'], function () {
  console.log("=============");
  console.log("test complete");
  console.log("=============");
})


gulp.task('watch',
          ['browserSync', 'less', 'nunjucks', 'static','sitemap'],   // Init the public dir on first run
          function(){
  gulp.watch('src/less/**/*.less', ['less', browserSync.reload]);
  // Other watchers
  // Reloads the browser whenever HTML or JS files change
  gulp.watch('src/static/**/*', ['static',browserSync.reload]);
  gulp.watch('src/**/*.nunjucks', ['nunjucks', browserSync.reload]);
  gulp.watch('public/js/**/*.js', browserSync.reload);
})

