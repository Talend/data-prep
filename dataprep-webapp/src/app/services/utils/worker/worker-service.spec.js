/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Worker service', function () {
    'use strict';

    var $rootScope, createWorker, createWorkerFromFunction, createWorkerWithNamedHelpers;
    var jasmineOriginalTimeout;

    beforeEach(angular.mock.module('data-prep.services.utils'));

    beforeEach(inject(function() {
        jasmineOriginalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
        jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;//default value === 5000 ms
    }));

    afterEach(function() {
        jasmine.DEFAULT_TIMEOUT_INTERVAL = jasmineOriginalTimeout;
    });

    beforeEach(inject(function(_$rootScope_, $window, WorkerService) {
        $rootScope = _$rootScope_;

        function add(a, b) {
            return a + b;
        }
        function multiply(a, b) {
            return a * b;
        }
        function calculate(a, b) {
            return multiply(a, b) - add(a, b);
        }

        createWorker = function() {
            return WorkerService.create(null, [add, multiply], calculate);
        };

        createWorkerWithNamedHelpers = function() {
            var myAdd = add;
            var myMultiply = multiply;
            return WorkerService.create(null, [{
                myAdd: myAdd,
                myMultiply: myMultiply
            }], function calculate(a, b) {
                return myMultiply(a, b) - myAdd(a, b);
            });
        };

        createWorkerFromFunction = function(fn) {
            return WorkerService.create(null, null, fn);
        };

        spyOn($window.URL, 'revokeObjectURL').and.callThrough();
    }));

    it('should execute a named function code', function (done) {
        //given
        var workerWrapper = createWorker();

        //when
        workerWrapper.postMessage([5, 8])

            //then
            .then(function(result) {
                expect(result).toBe(27);
                done();
            });

        // $q needs a digest so lets call one after the worker should be finished
        setTimeout(function() { //eslint-disable-line angular/timeout-service
            $rootScope.$digest();
        }, 500);
    });

    it('should execute an unnamed function code', function (done) {
        //given
        var workerWrapper = createWorkerFromFunction(function() {
            return 3 + 4;
        });

        //when
        workerWrapper.postMessage()

            //then
            .then(function(result) {
                expect(result).toBe(7);
                done();
            });

        // $q needs a digest so lets call one after the worker should be finished
        setTimeout(function() { //eslint-disable-line angular/timeout-service
            $rootScope.$digest();
        }, 500);
    });

    it('should execute a function code with named helper functions', function (done) {
        //given
        var workerWrapper = createWorkerWithNamedHelpers();

        //when
        workerWrapper.postMessage([5, 8])

            //then
            .then(function(result) {
                expect(result).toBe(27);
                done();
            });

        // $q needs a digest so lets call one after the worker should be finished
        setTimeout(function() { //eslint-disable-line angular/timeout-service
            $rootScope.$digest();
        }, 500);
    });

    it('should revoke blob url', inject(function ($window) {
        //given
        var workerWrapper = createWorker();

        //when
        workerWrapper.terminate();

        //Then
        expect($window.URL.revokeObjectURL).toHaveBeenCalled();
    }));
});