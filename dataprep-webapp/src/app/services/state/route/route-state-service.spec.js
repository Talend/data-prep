/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { HOME_PREPARATIONS_ROUTE } from '../../../index-route';

describe('Route state service', () => {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.state'));

    describe('previous', () => {
        it('should init previous route', inject((routeState) => {
            //then
            expect(routeState.previous).toBe(HOME_PREPARATIONS_ROUTE);
            expect(routeState.previousOptions).toEqual({ folderId: '' });
        }));

        it('should set previous route', inject((routeState, RouteStateService) => {
            //given
            const previous = 'previous.route';
            const previousOptions = { opt: 'my options' };

            expect(routeState.previous).not.toBe(previous);
            expect(routeState.previousOptions).not.toBe(previousOptions);

            //when
            RouteStateService.setPrevious(previous, previousOptions);

            //then
            expect(routeState.previous).toBe(previous);
            expect(routeState.previousOptions).toBe(previousOptions);
        }));

        it('should NOT change previous route if it is falsy', inject((routeState, RouteStateService) => {
            //given
            const previous = '';
            const previousOptions = { opt: 'my options' };

            const originalPrevious = 'toto';
            const originalPreviousOptions = {};

            routeState.previous = originalPrevious;
            routeState.previousOptions = originalPreviousOptions;

            //when
            RouteStateService.setPrevious(previous, previousOptions);

            //then
            expect(routeState.previous).toBe(originalPrevious);
            expect(routeState.previousOptions).toBe(originalPreviousOptions);
        }));

        it('should reset previous route', inject((routeState, RouteStateService) => {
            //given
            const previous = 'previous.route';
            const previousOptions = { opt: 'my options' };

            routeState.previous = previous;
            routeState.previousOptions = previousOptions;

            //when
            RouteStateService.resetPrevious();

            //then
            expect(routeState.previous).toBe(HOME_PREPARATIONS_ROUTE);
            expect(routeState.previousOptions).toEqual({ folderId: '' });
        }));
    });

    describe('next', () => {
        it('should init next route', inject((routeState) => {
            //then
            expect(routeState.next).toBe(HOME_PREPARATIONS_ROUTE);
            expect(routeState.nextOptions).toEqual({ folderId: '' });
        }));

        it('should set next route', inject((routeState, RouteStateService) => {
            //given
            const next = 'previous.route';
            const nextOptions = { opt: 'my options' };

            expect(routeState.next).not.toBe(next);
            expect(routeState.nextOptions).not.toBe(nextOptions);

            //when
            RouteStateService.setNext(next, nextOptions);

            //then
            expect(routeState.next).toBe(next);
            expect(routeState.nextOptions).toBe(nextOptions);
        }));

        it('should NOT change next route when it is falsy', inject((routeState, RouteStateService) => {
            //given
            const next = '';
            const nextOptions = {};

            const originalNext = 'previous.route';
            const originalNextOptions = { opt: 'my options' };

            routeState.next = originalNext;
            routeState.nextOptions = originalNextOptions;

            //when
            RouteStateService.setNext(next, nextOptions);

            //then
            expect(routeState.next).toBe(originalNext);
            expect(routeState.nextOptions).toBe(originalNextOptions);
        }));

        it('should reset next route', inject((routeState, RouteStateService) => {
            //given
            const next = 'previous.route';
            const nextOptions = { opt: 'my options' };

            routeState.next = next;
            routeState.nextOptions = nextOptions;

            //when
            RouteStateService.resetNext();

            //then
            expect(routeState.next).toBe(HOME_PREPARATIONS_ROUTE);
            expect(routeState.nextOptions).toEqual({ folderId: '' });
        }));
    });

    describe('reset', () => {
        it('should reset all routes', inject((routeState, RouteStateService) => {
            //given
            const previous = 'previous.route';
            const previousOptions = { opt: 'my options' };

            const next = 'previous.route';
            const nextOptions = { opt: 'my options' };

            routeState.previous = previous;
            routeState.previousOptions = previousOptions;

            routeState.next = next;
            routeState.nextOptions = nextOptions;

            //when
            RouteStateService.reset();

            //then
            expect(routeState.previous).toBe(HOME_PREPARATIONS_ROUTE);
            expect(routeState.previousOptions).toEqual({ folderId: '' });
            expect(routeState.next).toBe(HOME_PREPARATIONS_ROUTE);
            expect(routeState.nextOptions).toEqual({ folderId: '' });
        }));
    });
});
