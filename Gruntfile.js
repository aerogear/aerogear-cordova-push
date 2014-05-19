module.exports = function (grunt) {
    'use strict';


    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        'test-cordova-plugin': {
            default: {
                deleteTemp: false
            }
        }
    });

    grunt.loadNpmTasks("grunt-cordova-plugin-jasmine");

    grunt.registerTask('test', 'test-cordova-plugin');
};