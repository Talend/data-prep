/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Moment filter', function () {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.utils'));

    it('should transform timestamp to human readable moment', inject(function ($filter) {
        // given
        var date = new Date();
        date.setDate(date.getDate() - 7);

        // when
        var result = $filter('TDPMoment')(date.getTime());

        // then
        expect(result).toEqual('7 days ago');
    }));

    it('should transform date to human readable moment', inject(function ($filter) {
        // given
        var date = new Date();
        date.setDate(date.getDate() - 7);

        var dateAsString = date.getFullYear() + '/' +
            (date.getMonth() < 9 ? '0' : '') + (date.getMonth() + 1) + '/' +
            (date.getDate() < 10 ? '0' : '') + date.getDate() +
            ' ' + date.getHours() + ':' + date.getMinutes();

        // when
        var result = $filter('TDPMoment')(dateAsString, 'YYYY/MM/DD HH:mm');

        // then
        expect(result).toEqual('7 days ago');
    }));
});