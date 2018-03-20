/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Easter Eggs', function () {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.state'));

    describe('state service', function () {
        it('should enable an easter egg', inject(function (easterEggsState, EasterEggsStateService) {
            //given
            //when
            EasterEggsStateService.enableEasterEgg('back to the future');

            //then
            expect(easterEggsState.currentEasterEgg).toBe('back to the future');
            expect(easterEggsState.displayEasterEgg).toBe(true);
        }));

        it('should disable an easter egg', inject(function (easterEggsState, EasterEggsStateService) {
            //given
            //when
            EasterEggsStateService.disableEasterEgg();

            //then
            expect(easterEggsState.currentEasterEgg).toBe('');
            expect(easterEggsState.displayEasterEgg).toBe(false);
        }));
    });
});
