// Karma configuration
// Generated on Wed May 18 2016 17:29:01 GMT+0200 (CEST)

module.exports = function(config) {
  config.set({


    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '',

      plugins: [
          require("karma-jasmine"),
          require("karma-chrome-launcher"),
          require("karma-sourcemap-loader"),
          require("karma-phantomjs-launcher"),
          require("karma-webpack")
      ],

    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['jasmine'],


    // list of files / patterns to load in the browser
    files: [ { pattern: 'test-context.js', watched: false },
             'src/main/resources/static/js/jquery/jquery-3.3.1.js',
             'src/main/resources/static/js/jquery-ui/jquery-ui.js',
             'src/main/resources/static/js/alfrescoMock.js',
             'src/main/resources/static/js/recognition.js',
             'src/main/resources/static/js/util.js',
             'src/main/resources/static/js/verteilung.js',
             'src/main/resources/static/js/Base64.js',
             'src/main/resources/static/js/superfish.js',
             'src/main/resources/static/js/alfrescoClient.js',
             'src/main/resources/static/js/dataTables/dataTables.js',
             'src/main/resources/static/js/dataTables/dataTables.scroller.js',
             'src/main/resources/static/js/dataTables/dataTables.editable.js',
             'src/main/resources/static/js/dataTables/datetime-moment.js',
             'src/main/resources/static/js/jstree/jstree.js',
             'src/main/resources/static/js/alertify/alertify.js',
             'src/test/js/specs/**.js'
    ],


    // list of files to exclude
    exclude: [
    ],


    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
      preprocessors: {
          // source files, that you wanna generate coverage for
          // do not include tests or libraries
          // (these files will be instrumented by Istanbul)
          //'src/main/resources/static/js/*.js': ['coverage']
          'test-context.js': ['webpack', 'sourcemap']
      },
      webpack: {
          devtool: 'source-map',
          mode: 'development',
          module: {
              rules: [{
                  loader: 'babel-loader',
                    query :{
                        presets:['@babel/preset-env']
        // ,'es2017'
                },
                test: /\.js?$/,
                exclude: /node_modules/
              }
              ]
          },
          watch: true
      },
      webpackServer: {
          noInfo: true
      },

    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
      reporters: ['progress'],


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: false,


    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: ['ChromeHeadless'],


    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: true,

    // Concurrency level
    // how many browser should be started simultaneous
    concurrency: Infinity
  })
};
