/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Date service', function () {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.utils'));

    it('should check if a timestamp is in the range limits or not: outside the range', inject(function (DateService) {
        // given
        const patterns = ['DD-MM-YYYY', 'M-D-YYYY', 'M-D-YY', 'YY-MM-DD', 'D-M-YY', 'YY-M-D', 'DD-MM-YY'];
        const minTimestamp = 1167606000000;//Jan 01 2007
        const maxTimestamp = 1175378400000;//Apr 01 2007

        // when
        const result = DateService.isInDateLimits(minTimestamp, maxTimestamp, patterns)('17-02-2008');

        // then
        expect(result).toEqual(false);
    }));

    it('should check if a timestamp is in the range limits or not: inside the range', inject(function (DateService) {
        // given
        const patterns = ['DD-MM-YYYY', 'M-D-YYYY', 'M-D-YY', 'YY-MM-DD', 'D-M-YY', 'YY-M-D', 'DD-MM-YY'];
        const minTimestamp = 1167606000000;//Jan 01 2007
        const maxTimestamp = 1175378400000;//Apr 01 2007

        // when
        const result = DateService.isInDateLimits(minTimestamp, maxTimestamp, patterns)('17-02-2007');

        // then
        expect(result).toEqual(true);
    }));

    it('should check if a timestamp is in the range limits or not: no match with the pattern', inject(function (DateService) {
        // given
        const patterns = ['DD-MM-YY'];
        const minTimestamp = 1167606000000;//Jan 01 2007
        const maxTimestamp = 1175378400000;//Apr 01 2007

        // when
        const result = DateService.isInDateLimits(minTimestamp, maxTimestamp, patterns)('17-02-2007');

        // then
        expect(result).toEqual(false);
    }));
});
