module.exports = function (grunt) {

  require('jit-grunt')(grunt);

//  var appModules = grunt.file.expand ({filter: "isFile", cwd: "src/main/webapp/_/app/js"}, ["*/*.js"]).map (function (s) {return 'app/' + s.replace ('.js', '')})
//  appModules.unshift ('../app/handler')

  grunt.initConfig ({
  
    svg_sprite      : {
        options     : {
            // Task-specific options go here.
        },
        icons: {
            expand      : true,
            cwd         : 'src/main/webapp/_/libs/mosgis/svg',
            src         : ['**/*.svg'],
            dest        : 'src/main/webapp/_/libs/mosgis',
            sprite      : 'sprite.svg',
            options     : {

                shape               : {
                    dimension       : {         // Set maximum dimensions
                        maxWidth    : 32,
                        maxHeight   : 32,
                        precision: 0
                    },
                    spacing         : {         // Add padding
                        padding     : 1
                    }
                },

                mode : {
                    css : {
                        sprite  : 'sprite.svg',
                        layout  : "vertical",
                        bust    : false,
                        render  : {
                           less : true
                        }
//                        dimensions: true
                    }
                }
            }
        }
    },
  
    less: {
      development: {
        options: {
          compress: true,
          yuicompress: true,
          relativeUrls: true,
          optimization: 2
        },
        files: {
          "src/main/webapp/_/libs/mosgis/mosgis.css": "src/main/webapp/_/libs/mosgis/mosgis.less"
        }
      }
    },
    
    bump: {
      options: {
        files: ['package.json'],
        updateConfigs: [],
        commit: false,
        createTag: false,
        push: false
      }
    },
/*        
    replace: {
      versionNumber: {
        src: ['src/main/webapp/index.html'],
        overwrite: true,
        replacements: [{
          from: /var ver.* /,
          to: "var ver = '<%= grunt.file.readJSON ('package.json') ['version'] %>';"
        }]
      }
    },

    requirejs: {
      compile: {
        options: {
            generateSourceMaps: false,
            baseUrl: 'src/main/webapp/_/libs',
            paths: {app: '../app/js'},
//            optimize: "none",
            out: "src/main/webapp/_/app/js/app.js",
            findNestedDependencies: true,
            include: appModules
        }
      }
    },
*/    
    concat: {
        options: {
            stripBanners: true
        },
        js: {
            src: [
                'src/main/webapp/_/libs/jquery/jquery-3.1.1.min.js', 
                'src/main/webapp/_/libs/requirejs/require.js',
                'src/main/webapp/_/libs/w2ui/w2ui-1.5.rc1.min.js',
                'src/main/webapp/_/app/handler.js'                
            ],
            dest: 'src/main/webapp/_/app/js/_.js'
        }
    }, 

/*
    compress: {
      xslt: {
        options: {mode: 'gzip'},
        expand: true,
        cwd: 'src/main/webapp/_/app/xslt',
        ext: '.xsl.gz',
        src: ['*.xsl'],
        dest: 'src/main/webapp/_/app/xslt'
      },
      js: {
        options: {mode: 'gzip'},
        expand: true,
        cwd: 'src/main/webapp/_/app/js',
        ext: '.js.gz',
        src: ['*.js'],
        dest: 'src/main/webapp/_/app/js'
      }
    },
*/
//    clean: {
//      gz: ['src/main/webapp/_/app/**/*.gz']
//    },

    watch: {

      general: {
        files: ['src/main/webapp/_/app/**/*.*'],
        tasks: ['bump'],
        options: {nospawn: true}
      },

      styles: {
        files: ['src/main/webapp/_/libs/mosgis/*.less'],
        tasks: ['less'],
        options: {nospawn: true}
      },
/*
      svg: {
        files: ['src/main/webapp/_/libs/mosgis/svg/*.svg'],
        tasks: ['svg_sprite', 'less'],
        options: {nospawn: true}
      },
*/
      js: {
        files: ['src/main/webapp/_/app/handler.js'],
        tasks: ['concat:js'],
        options: {nospawn: true}
      }
      
    }
    
  });
  
  grunt.loadNpmTasks('grunt-text-replace');
  grunt.loadNpmTasks('grunt-contrib-requirejs');
  grunt.loadNpmTasks('grunt-svg-sprite');
  grunt.loadNpmTasks('grunt-contrib-compress');
  
  grunt.registerTask('default', ['watch']);
  grunt.registerTask('build', ['bump', 'replace', 'svg_sprite', 'less', 'requirejs', 'concat'/*, 'compress'*/]);
  
};