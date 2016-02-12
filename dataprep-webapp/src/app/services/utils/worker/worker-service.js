/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:WorkerService
 * @description This service convert a function into a web worker
 */
export default function WorkerService($q, $window) {
    'ngInject';

    $window.URL = $window.URL || $window.webkitURL || $window.mozURL || $window.msURL || $window.oURL;
    $window.BlobBuilder = $window.BlobBuilder || $window.WebKitBlobBuilder || $window.MozBlobBuilder || $window.MSBlobBuilder || $window.OBlobBuilder;
    var url = $window.location.protocol + '//' + $window.location.host;

    return {
        create: create
    };

    /**
     * @ngdoc method
     * @name create
     * @methodOf data-prep.services.utils.service:WorkerService
     * @param {Array} importLibs The libs to import into the worker
     * @param {Array} helperFns The helper function that the main function use.
     * Functions are directly serialized. They MUST be named functions
     * Objects are used here to set a name to the functions {name1: fn1, name2: fn2} will be serialized as
     * var name1 = fn1;
     * var name2 = fn2
     * This is used to pass external function to the web worker and avoid minification problems
     * The names should be preserved from minification name mangling in the build configuration
     * @param {function} workerFn The main function that is executed in the worker
     * @description Create a wrapper on a worker that execute the provided function
     * @returns {object} The wrapper on a created web worker
     */
    function create(importLibs, helperFns, workerFn) {
        importLibs = importLibs || [];
        helperFns = helperFns || [];

        var strWorker = 'self.window = self;\n';
        strWorker += importScriptsStr(importLibs);  // worker libs imports
        strWorker += helperFnsStr(helperFns);       // worker helper function used in the main function
        strWorker += onmessageFnStr(workerFn);      // worker main function

        var blobURL = createBlobURL(strWorker, {type: 'text/javascript'});
        var worker = new $window.Worker(blobURL);

        return {
            postMessage: function (args) {
                var defer = $q.defer();

                worker.onmessage = function (e) {
                    defer.resolve(e.data);
                };

                worker.postMessage(args);

                return defer.promise;
            },
            clean: function () {
                $window.URL.revokeObjectURL(blobURL);
            },
            terminate: function () {
                worker.terminate();
                this.clean();
            }
        };
    }

    /**
     * @ngdoc method
     * @name importScriptsStr
     * @methodOf data-prep.services.utils.service:WorkerService
     * @param {Array} importLibs The libs to import into the worker
     * @description Generate the import statement
     * @returns {string} A string representing the import code
     */
    function importScriptsStr(importLibs) {
        return 'importScripts(' +
            importLibs
                .map(function (lib) {
                    return '\'' + url + lib + '\'';
                })
                .join(', ') +
            ');\n';
    }

    /**
     * @ngdoc method
     * @name helperFnsStr
     * @methodOf data-prep.services.utils.service:WorkerService
     * @param {Array} helperFns The helper functions.
     * Functions are directly serialized. They MUST be named functions
     * Objects are used here to set a name to the functions {name1: fn1, name2: fn2} will be serialized as
     * var name1 = fn1;
     * var name2 = fn2
     * This is used to pass external function to the web worker and avoid minification problems
     * The names should be preserved from minification name mangling in the build configuration
     * @description Stringify the helper functions. The array items can be functions or objects.
     * @returns {string} A string representing the helper code
     */
    function helperFnsStr(helperFns) {
        return _.reduce(helperFns, function (accu, helper) {
            var helperStr;
            if (angular.isFunction(helper)) {
                helperStr = helper.toString();
            }
            else if (angular.isObject(helper)) {
                helperStr = _.chain(helper)
                    .keys()
                    .map(function (key) {
                        var fn = helper[key];
                        return 'var ' + key + ' = ' + fn.toString() + ';\n';
                    })
                    .reduce(function (accu, fnStr) {
                        return accu + fnStr;
                    }, '')
                    .value();
            }

            return accu + helperStr + '\n';
        }, '');
    }

    /**
     * @ngdoc method
     * @name onmessageFnStr
     * @methodOf data-prep.services.utils.service:WorkerService
     * @param {function} workerFn The worker main function
     * @description Generate the onmessage function
     * @returns {string} A string representing the onmessage code
     */
    function onmessageFnStr(workerFn) {
        // inject a name to the function if necessary
        var aFuncParts = /function\s*(\w*)(.*)/.exec(workerFn.toString());
        aFuncParts[1] = aFuncParts[1] || 'a'; // give unnamed functions a name.
        var mainFn = 'function ' + aFuncParts[1] + aFuncParts[2];
        mainFn += workerFn.toString().substring(aFuncParts[0].length);

        // worker onmessage
        var onmessageFn = '\n;onmessage = function(e) {' +
            'var result = ' + aFuncParts[1] + '.apply(null,e.data);' +
            'postMessage(result);' +
            '};';

        return mainFn + onmessageFn;
    }

    /**
     * @ngdoc method
     * @name createBlobURL
     * @methodOf data-prep.services.utils.service:WorkerService
     * @param {string} strWorker The worker code as string
     * @param {object} config The blob configuration
     * @description Create a blob and request an url to access it
     * @returns {string} The blob url
     */
    function createBlobURL(strWorker, config) {
        var blob;

        if ($window.Blob && typeof($window.Blob) === typeof(Function)) {
            blob = new $window.Blob([strWorker], config);
        }
        else if ($window.BlobBuilder) {
            var blobBuilder = new $window.BlobBuilder();
            blobBuilder.append(strWorker);
            blob = blobBuilder.getBlob();
        }

        return $window.URL.createObjectURL(blob);
    }
}