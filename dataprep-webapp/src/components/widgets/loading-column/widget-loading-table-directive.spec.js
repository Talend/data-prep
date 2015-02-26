describe('Dropdown directive', function () {
    'use strict';

    var scope, element;

    beforeEach(module('talend.widget'));
    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function ($rootScope, $compile, $timeout) {
        scope = $rootScope.$new();

        var html = '<div>' +
            '<table talend-loading-table id="myTable">' +
            '     <tr>' +
            '         <th>Title 1</th>' +
            '         <th id="secondHeader">Title 2</th>' +
            '     </tr>' +
            '     <tr>' +
            '         <td>Title 1</td>' +
            '         <td>Title 2</td>' +
            '     </tr>' +
            '     <tr id="lastRow">' +
            '         <td>Title 1</td>' +
            '         <td>Title 2</td>' +
            '     </tr>' +
            '</table>' +
            '</div>';
        element = $compile(html)(scope);
        scope.$digest();
        $timeout.flush();
    }));

    it('should add loading div', inject(function ($rootScope) {
        //given

        //when
        $rootScope.$emit('talend.loading.myTable.start', {col: 1});
        $rootScope.$digest();

        //then
        expect(element.find('.loading-column').length).toBe(1);
    }));

    it('should remove loading div', inject(function ($rootScope, $timeout) {
        //given
        $rootScope.$emit('talend.loading.myTable.start', {col: 1});
        $rootScope.$digest();
        expect(element.find('.loading-column').length).toBe(1);

        //when
        $rootScope.$emit('talend.loading.myTable.stop', {col: 1});
        $rootScope.$digest();
        $timeout.flush();

        //then
        expect(element.find('.loading-column').length).toBe(0);
    }));

    it('should remove nothing id loading div on col is not present', inject(function ($rootScope, $timeout) {
        //given
        $rootScope.$emit('talend.loading.myTable.start', {col: 1});
        $rootScope.$digest();
        expect(element.find('.loading-column').length).toBe(1);

        //when
        $rootScope.$emit('talend.loading.myTable.stop', {col: 0});
        $rootScope.$digest();
        $timeout.flush();

        //then
        expect(element.find('.loading-column').length).toBe(1);
    }));

    it('should not add another loading div if there is already one on the target col', inject(function ($rootScope) {
        //given
        $rootScope.$emit('talend.loading.myTable.start', {col: 1});
        $rootScope.$digest();
        expect(element.find('.loading-column').length).toBe(1);

        //when
        $rootScope.$emit('talend.loading.myTable.start', {col: 1});
        $rootScope.$digest();

        //then
        expect(element.find('.loading-column').length).toBe(1);
    }));

    it('should add another loading div if the target is on another col', inject(function ($rootScope) {
        //given
        $rootScope.$emit('talend.loading.myTable.start', {col: 1});
        $rootScope.$digest();
        expect(element.find('.loading-column').length).toBe(1);

        //when
        $rootScope.$emit('talend.loading.myTable.start', {col: 0});
        $rootScope.$digest();

        //then
        expect(element.find('.loading-column').length).toBe(2);
    }));

    it('should remove all loading div', inject(function ($rootScope, $timeout) {
        //given
        $rootScope.$emit('talend.loading.myTable.start', {col: 0});
        $rootScope.$emit('talend.loading.myTable.start', {col: 1});
        $rootScope.$digest();
        expect(element.find('.loading-column').length).toBe(2);

        //when
        $rootScope.$emit('talend.loading.myTable.stop.all');
        $rootScope.$digest();
        $timeout.flush();

        //then
        expect(element.find('.loading-column').length).toBe(0);
    }));

    it('should remove all loading div and deregister rootScope listeners on scope destroy', inject(function ($rootScope, $timeout) {
        //given
        $rootScope.$emit('talend.loading.myTable.start', {col: 0});
        $rootScope.$emit('talend.loading.myTable.start', {col: 1});
        $rootScope.$digest();

        expect(element.find('.loading-column').length).toBe(2);
        expect($rootScope.$$listeners['talend.loading.myTable.start'].length).toBe(1);
        expect($rootScope.$$listeners['talend.loading.myTable.stop'].length).toBe(1);
        expect($rootScope.$$listeners['talend.loading.myTable.stop.all'].length).toBe(1);

        //when
        scope.$destroy();
        scope.$digest();
        $timeout.flush();

        //then
        expect(element.find('.loading-column').length).toBe(0);
        expect($rootScope.$$listeners['talend.loading.myTable.start'][0]).toBeFalsy();
        expect($rootScope.$$listeners['talend.loading.myTable.stop'][0]).toBeFalsy();
        expect($rootScope.$$listeners['talend.loading.myTable.stop.all'][0]).toBeFalsy();
    }));
});