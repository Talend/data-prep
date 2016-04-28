describe('Preparation REST Service', () => {
    'use strict';

    let $httpBackend;
    const allPreparations = [
        {
            id: 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
            dataSetId: 'ddb74c89-6d23-4528-9f37-7a9860bb468e',
            author: 'anonymousUser',
            creationDate: 1427447300300,
            steps: [
                '35890aabcf9115e4309d4ce93367bf5e4e77b82a',
                '4ff5d9a6ca2e75ebe3579740a4297fbdb9b7894f',
                '8a1c49d1b64270482e8db8232357c6815615b7cf',
                '599725f0e1331d5f8aae24f22cd1ec768b10348d',
            ],
            actions: [
                {
                    action: 'lowercase',
                    parameters: {
                        column_name: 'birth',
                    },
                },
                {
                    action: 'uppercase',
                    parameters: {
                        column_name: 'country',
                    },
                },
                {
                    action: 'cut',
                    parameters: {
                        pattern: '.',
                        column_name: 'first_item',
                    },
                },
            ]
        },
        {
            id: 'fbaa18e82e913e97e5f0e9d40f04413412be1126',
            dataSetId: '8ec053b1-7870-4bc6-af54-523be91dc774',
            author: 'anonymousUser',
            creationDate: 1427447330693,
            steps: [
                '47e2444dd1301120b539804507fd307072294048',
                'ae1aebf4b3fa9b983c895486612c02c766305410',
                '24dcd68f2117b9f93662cb58cc31bf36d6e2867a',
                '599725f0e1331d5f8aae24f22cd1ec768b10348d',
            ],
            actions: [
                {
                    action: 'cut',
                    parameters: {
                        pattern: '-',
                        column_name: 'birth',
                    }
                },
                {
                    action: 'fillemptywithdefault',
                    parameters: {
                        default_value: 'N/A',
                        column_name: 'state',
                    }
                },
                {
                    action: 'uppercase',
                    parameters: {
                        column_name: 'lastname',
                    }
                },
            ]
        }
    ];

    const records = {
        records: [
            {
                firstname: 'Grover',
                avgAmount: '82.4',
                city: 'BOSTON',
                birth: '01-09-1973',
                registration: '17-02-2008',
                id: '1',
                state: 'AR',
                nbCommands: '41',
                lastname: 'Quincy',
            },
            {
                firstname: 'Warren',
                avgAmount: '87.6',
                city: 'NASHVILLE',
                birth: '11-02-1960',
                registration: '18-08-2007',
                id: '2',
                state: 'WA',
                nbCommands: '17',
                lastname: 'Johnson',
            }
        ],
    };

    beforeEach(angular.mock.module('data-prep.services.preparation'));

    beforeEach(inject(($injector, RestURLs) => {
        RestURLs.setServerUrl('');
        $httpBackend = $injector.get('$httpBackend');
    }));

    describe('preparation lifecycle', () => {
        it('should create a new preparation', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            const datasetId = '8ec053b1-7870-4bc6-af54-523be91dc774';
            const name = 'The new preparation';

            $httpBackend
                .expectPOST(RestURLs.preparationUrl, { dataSetId: datasetId, name: name })
                .respond(200, 'fbaa18e82e913e97e5f0e9d40f04413412be1126');

            //when
            PreparationRestService.create(datasetId, name);
            $httpBackend.flush();
            $rootScope.$digest();
        }));

        it('should update preparation name', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            const name = 'The new preparation name';
            const preparationId = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';

            $httpBackend
                .expectPUT(RestURLs.preparationUrl + '/fbaa18e82e913e97e5f0e9d40f04413412be1126', { name: name })
                .respond(200);

            //when
            PreparationRestService.update(preparationId, { name: name });
            $httpBackend.flush();
            $rootScope.$digest();
        }));

        it('should delete the preparation', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            const preparationId = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';
            $httpBackend
                .expectDELETE(RestURLs.preparationUrl + '/' + preparationId)
                .respond(200);

            //when
            PreparationRestService.delete(preparationId);
            $httpBackend.flush();
            $rootScope.$digest();
        }));

        it('should copy the preparation', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            const preparationId = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';
            const folderPath = '/toto/tata';
            const name = 'jso_prep';
            $httpBackend
                .expectPOST(`${RestURLs.preparationUrl}/${preparationId}/copy?destination=${encodeURIComponent(folderPath)}&newName=${encodeURIComponent(name)}`)
                .respond(200);

            //when
            PreparationRestService.copy(preparationId, folderPath, name);
            $httpBackend.flush();
            $rootScope.$digest();
        }));

        it('should move the preparation', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            const preparationId = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';
            const folderPath = '/toto/tata';
            const destinationPath = '/toto/tata';
            const name = 'jso_prep';
            $httpBackend
                .expectPUT(`${RestURLs.preparationUrl}/${preparationId}/move?folder=${encodeURIComponent(folderPath)}&destination=${encodeURIComponent(destinationPath)}&newName=${encodeURIComponent(name)}`)
                .respond(200);

            //when
            PreparationRestService.move(preparationId, folderPath, destinationPath, name);
            $httpBackend.flush();
            $rootScope.$digest();
        }));
    });

    describe('preparation getters', () => {
        it('should get all preparations', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            let preparations = null;
            $httpBackend
                .expectGET(RestURLs.preparationUrl)
                .respond(200, allPreparations);

            //when
            PreparationRestService.getPreparations()
                .then((response) => {
                    preparations = response.data
                });
            $httpBackend.flush();
            $rootScope.$digest();

            //then
            expect(preparations).toEqual(allPreparations);
        }));

        it('should get the current preparation details', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            let details = null;
            const preparationId = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';
            $httpBackend
                .expectGET(RestURLs.preparationUrl + '/' + preparationId + '/details')
                .respond(200, allPreparations[1]);

            //when
            PreparationRestService.getDetails(preparationId)
                .then((response) => {
                    details = response.data
                });
            $httpBackend.flush();
            $rootScope.$digest();

            //then
            expect(details).toEqual(allPreparations[1]);
        }));

        it('should get the requested version of preparation content', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            let content = null;
            const preparationId = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';
            $httpBackend
                .expectGET(RestURLs.preparationUrl + '/' + preparationId + '/content?version=head')
                .respond(200, records);

            //when
            PreparationRestService.getContent(preparationId, 'head')
                .then((response) => {
                    content = response
                });
            $httpBackend.flush();
            $rootScope.$digest();

            //then
            expect(content).toEqual(records);
        }));
    });

    describe('preparation step lifecycle', () => {
        it('should append a transformation step in the current preparation', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            const actionParams = {
                action: 'fillemptywithdefault',
                parameters: {
                    action: 'fillemptywithdefault',
                    parameters: { default_value: 'N/A', column_name: 'state', column_id: '0000' }
                }
            };

            const preparationId = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';

            //given : preparation step append request
            $httpBackend
                .expectPOST(RestURLs.preparationUrl + '/' + preparationId + '/actions', {
                    actions: [actionParams]
                })
                .respond(200);

            //when
            PreparationRestService.appendStep(preparationId, actionParams);
            $httpBackend.flush();
            $rootScope.$digest();

            //then : append request to have been called
        }));

        it('should append a transformation step in the current preparation at a provided insertion point', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            const actionParams = {
                action: 'fillemptywithdefault',
                parameters: {
                    action: 'fillemptywithdefault',
                    parameters: { default_value: 'N/A', column_name: 'state', column_id: '0000' }
                }
            };
            const insertionPoint = '65ab26e169174ef68b434';

            const preparationId = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';

            //given : preparation step append request
            $httpBackend
                .expectPOST(RestURLs.preparationUrl + '/' + preparationId + '/actions', {
                    insertionStepId: insertionPoint,
                    actions: [actionParams]
                })
                .respond(200);

            //when
            PreparationRestService.appendStep(preparationId, actionParams, insertionPoint);
            $httpBackend.flush();
            $rootScope.$digest();

            //then : append request to have been called
        }));

        it('should append a list of transformation step in the current preparation', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            const actionParams = [
                {
                    action: 'fillemptywithdefault',
                    parameters: { default_value: 'N/A', column_name: 'state', column_id: '0000' }
                },
                { action: 'uppercase', parameters: { column_name: 'lastname', column_id: '0001' } }
            ];

            const preparationId = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';

            //given : preparation step append request
            $httpBackend
                .expectPOST(RestURLs.preparationUrl + '/' + preparationId + '/actions', {
                    actions: actionParams
                })
                .respond(200);

            //when
            PreparationRestService.appendStep(preparationId, actionParams);
            $httpBackend.flush();
            $rootScope.$digest();

            //then : update request to have been called
        }));

        it('should update a transformation step in the current preparation', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            const actionParams = {
                action: 'fillemptywithdefault',
                parameters: {
                    action: 'fillemptywithdefault',
                    parameters: { default_value: 'N/A', column_name: 'state', column_id: '0000' }
                }
            };
            const stepId = '18046df82f0946af05ee766d0ac06f92f63e7047';

            const preparationId = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';

            //given : preparation step update request
            $httpBackend
                .expectPUT(RestURLs.preparationUrl + '/' + preparationId + '/actions/' + stepId, {
                    actions: [actionParams]
                })
                .respond(200);

            //when
            PreparationRestService.updateStep(preparationId, stepId, actionParams);
            $httpBackend.flush();
            $rootScope.$digest();

            //then : update request to have been called
        }));

        it('should remove a transformation step in the preparation', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            const preparationId = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';
            const stepId = '856980bacf0890c89bc318856980bacf0890c89b';

            $httpBackend
                .expectDELETE(RestURLs.preparationUrl + '/' + preparationId + '/actions/' + stepId)
                .respond(200);

            //when
            PreparationRestService.removeStep(preparationId, stepId);
            $httpBackend.flush();
            $rootScope.$digest();

            //then : delete request to have been called
        }));

        it('should move preparation head', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            const preparationId = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';
            const headId = '856980bacf0890c89bc318856980bacf0890c89b';

            $httpBackend
                .expectPUT(RestURLs.preparationUrl + '/' + preparationId + '/head/' + headId)
                .respond(200);

            //when
            PreparationRestService.setHead(preparationId, headId);
            $httpBackend.flush();
            $rootScope.$digest();

            //then : put request to have been called
        }));

        it('should copy preparation reference steps', inject(($rootScope, RestURLs, PreparationRestService) => {
            //given
            const preparationId = 'fbaa18e82e913e97e5f0e9d40f';
            const referenceId = '371aa397d835db4fa1584ee854';

            $httpBackend
                .expectPUT(RestURLs.preparationUrl + '/' + preparationId + '/steps/copy?from=' + referenceId)
                .respond(200);

            //when
            PreparationRestService.copySteps(preparationId, referenceId);
            $httpBackend.flush();
            $rootScope.$digest();

            //then : put request to have been called
        }));
    });

    describe('preview', () => {
        it('should call preview POST request', inject(($rootScope, $q, RestURLs, PreparationRestService) => {
            //given
            let done = false;
            let canceled = false;
            const canceler = $q.defer();

            const expectedParams = {
                tdpIds: [1, 2, 3, 4, 5],
                currentStepId: '18046df82f0946af05ee766d0ac06f92f63e7047',
                previewStepId: '856980bacf0890c89bc318856980bacf0890c89b',
                preparationId: 'fbaa18e82e913e97e5f0e9d40f04413412be1126'
            };

            $httpBackend
                .expectPOST(RestURLs.previewUrl + '/diff', expectedParams)
                .respond(200);


            //when
            PreparationRestService.getPreviewDiff(expectedParams, canceler)
                .then(() => {
                    done = true;
                })
                .catch(() => {
                    canceled = true;
                });
            $httpBackend.flush();
            $rootScope.$digest();

            //then
            expect(done).toBe(true);
            expect(canceled).toBe(false);
        }));

        it('should cancel POST preview by resolving the given promise', inject(($rootScope, $q, RestURLs, PreparationRestService) => {
            //given
            let done = false;
            let canceled = false;
            const canceler = $q.defer();

            const expectedParams = {
                tdpIds: [1, 2, 3, 4, 5],
                currentStepId: '18046df82f0946af05ee766d0ac06f92f63e7047',
                previewStepId: '856980bacf0890c89bc318856980bacf0890c89b',
                preparationId: 'fbaa18e82e913e97e5f0e9d40f04413412be1126'
            };

            $httpBackend
                .expectPOST(RestURLs.previewUrl + '/diff', expectedParams)
                .respond(200);

            //when
            PreparationRestService.getPreviewDiff(expectedParams, canceler)
                .then(() => {
                    done = true;
                })
                .catch(() => {
                    canceled = true;
                });
            canceler.resolve(true);
            $rootScope.$digest();

            //then
            expect(done).toBe(false);
            expect(canceled).toBe(true);
        }));

        it('should call update preview POST request', inject(($rootScope, $q, RestURLs, PreparationRestService) => {
            //given
            let done = false;
            let canceled = false;
            const newParams = { value: 'toto' };
            const canceler = $q.defer();

            const params = {
                action: {
                    action: 'cut',
                    parameters: newParams
                },
                tdpIds: [1, 2, 3, 4, 5],
                currentStepId: '18046df82f0946af05ee766d0ac06f92f63e7047',
                updateStepId: '856980bacf0890c89bc318856980bacf0890c89b',
                preparationId: 'fbaa18e82e913e97e5f0e9d40f04413412be1126'
            };

            $httpBackend
                .expectPOST(RestURLs.previewUrl + '/update', params)
                .respond(200);


            //when
            PreparationRestService.getPreviewUpdate(params, canceler)
                .then(() => {
                    done = true;
                })
                .catch(() => {
                    canceled = true;
                });
            $httpBackend.flush();
            $rootScope.$digest();

            //then
            expect(done).toBe(true);
            expect(canceled).toBe(false);
        }));

        it('should cancel POST update preview by resolving the given promise', inject(($rootScope, $q, RestURLs, PreparationRestService) => {
            //given
            let done = false;
            let canceled = false;
            const newParams = { value: 'toto' };
            const canceler = $q.defer();

            const params = {
                action: {
                    action: 'cut',
                    parameters: newParams
                },
                tdpIds: [1, 2, 3, 4, 5],
                currentStepId: '18046df82f0946af05ee766d0ac06f92f63e7047',
                updateStepId: '856980bacf0890c89bc318856980bacf0890c89b',
                preparationId: 'fbaa18e82e913e97e5f0e9d40f04413412be1126'
            };

            $httpBackend
                .expectPOST(RestURLs.previewUrl + '/update', params)
                .respond(200);


            //when
            PreparationRestService.getPreviewUpdate(params, canceler)
                .then(() => {
                    done = true;
                })
                .catch(() => {
                    canceled = true;
                });
            canceler.resolve(true);
            $rootScope.$digest();

            //then
            expect(done).toBe(false);
            expect(canceled).toBe(true);
        }));

        it('should call add preview POST request', inject(($rootScope, $q, RestURLs, PreparationRestService) => {
            //given
            let done = false;
            let canceled = false;
            const canceler = $q.defer();

            const params = {
                action: {
                    action: 'cut',
                    parameters: { value: 'toto' }
                },
                tdpIds: [1, 2, 3, 4, 5],
                preparationId: 'fbaa18e82e913e97e5f0e9d40f04413412be1126',
                datasetId: '856980bacf0890c89bc318856980bacf0890c89b'
            };

            $httpBackend
                .expectPOST(RestURLs.previewUrl + '/add', params)
                .respond(200);

            //when
            PreparationRestService.getPreviewAdd(params, canceler)
                .then(() => {
                    done = true;
                })
                .catch(() => {
                    canceled = true;
                });
            $httpBackend.flush();
            $rootScope.$digest();

            //then
            expect(done).toBe(true);
            expect(canceled).toBe(false);
        }));

        it('should cancel POST add preview by resolving the given promise', inject(($rootScope, $q, RestURLs, PreparationRestService) => {
            //given
            let done = false;
            let canceled = false;
            const canceler = $q.defer();

            const params = {
                action: {
                    action: 'cut',
                    parameters: { value: 'toto' }
                },
                tdpIds: [1, 2, 3, 4, 5],
                preparationId: 'fbaa18e82e913e97e5f0e9d40f04413412be1126',
                datasetId: '856980bacf0890c89bc318856980bacf0890c89b'
            };

            $httpBackend
                .expectPOST(RestURLs.previewUrl + '/add', params)
                .respond(200);

            //when
            PreparationRestService.getPreviewAdd(params, canceler)
                .then(() => {
                    done = true;
                })
                .catch(() => {
                    canceled = true;
                });
            canceler.resolve(true);
            $rootScope.$digest();

            //then
            expect(done).toBe(false);
            expect(canceled).toBe(true);
        }));
    });

});