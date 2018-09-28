describe('Early Preview Service', () => {
    'use strict';

    const dataset = { id: '123456' };
    const preparation = { id: '456789' };
    const firstnameColumn = { id: '0001', name: 'firstname' };
    const lastnameColumn = { id: '0002', name: 'lastname' };
    const transformation = { name: 'replace_on_value' };
    const transfoScope = 'column';
    const params = { value: 'James', replace: 'Jimmy' };
    let stateMock;

    beforeEach(angular.mock.module('data-prep.services.early-preview', ($provide) => {
        stateMock = {
            playground: {
                dataset: dataset,
                preparation: preparation,
                grid: {
                    selectedColumns: [firstnameColumn],
                },
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, PreviewService, RecipeService, EarlyPreviewService) => {
        spyOn(RecipeService, 'earlyPreview').and.returnValue();
        spyOn(RecipeService, 'cancelEarlyPreview').and.returnValue();
        spyOn(PreviewService, 'getPreviewAddRecords').and.returnValue($q.when());
        spyOn(PreviewService, 'cancelPreview').and.returnValue();
        spyOn(EarlyPreviewService, 'cancelPendingPreview').and.returnValue();
    }));

    describe('early preview', () => {
        it('should trigger preview after 700ms delay', inject(($timeout, PreviewService, EarlyPreviewService, RecipeService) => {
            // when
            EarlyPreviewService.earlyPreview(transformation, transfoScope)(params);
            expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();
            $timeout.flush(700);

            // then
            expect(RecipeService.earlyPreview).toHaveBeenCalled();
            expect(PreviewService.getPreviewAddRecords).toHaveBeenCalled();
        }));

        it('should trigger a preview on columns scope', inject(($timeout, PreviewService, EarlyPreviewService, RecipeService) => {
            // given
            stateMock.playground.grid.selectedColumns = [firstnameColumn, lastnameColumn];

            // when
            EarlyPreviewService.earlyPreview(transformation, 'column')(params);
            expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();
            $timeout.flush(700);

            // then
            const parametersByColumn = [
                {
                    value: 'James',
                    replace: 'Jimmy',
                    scope: 'column',
                    column_id: firstnameColumn.id,
                    column_name: firstnameColumn.name,
                },
                {
                    value: 'James',
                    replace: 'Jimmy',
                    scope: 'column',
                    column_id: lastnameColumn.id,
                    column_name: lastnameColumn.name,
                }
            ];
            expect(PreviewService.getPreviewAddRecords).toHaveBeenCalledWith(
                preparation.id,
                dataset.id,
                'replace_on_value',
                parametersByColumn
            );
            expect(RecipeService.earlyPreview).toHaveBeenCalledWith(
                transformation,
                parametersByColumn
            );
        }));

        it('should trigger a preview on lines scope', inject(($timeout, PreviewService, EarlyPreviewService, RecipeService) => {
            // given
            stateMock.playground.grid.selectedLine = { tdpId: 125 };

            // when
            EarlyPreviewService.earlyPreview(transformation, 'line')(params);
            expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();
            $timeout.flush(700);

            // then
            const parameters = [
                {
                    value: 'James',
                    replace: 'Jimmy',
                    scope: 'line',
                    row_id: 125,
                }
            ];
            expect(PreviewService.getPreviewAddRecords).toHaveBeenCalledWith(
                preparation.id,
                dataset.id,
                'replace_on_value',
                parameters
            );
            expect(RecipeService.earlyPreview).toHaveBeenCalledWith(
                transformation,
                parameters
            );
        }));

        it('should trigger a preview on dataset scope', inject(($timeout, PreviewService, EarlyPreviewService, RecipeService) => {
            // given
            stateMock.playground.grid.selectedColumns = [firstnameColumn, lastnameColumn];

            // when
            EarlyPreviewService.earlyPreview(transformation, 'dataset')(params);
            expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();
            $timeout.flush(700);

            // then
            const parametersByColumn = [
                {
                    value: 'James',
                    replace: 'Jimmy',
                    scope: 'dataset',
                }
            ];
            expect(PreviewService.getPreviewAddRecords).toHaveBeenCalledWith(
                preparation.id,
                dataset.id,
                'replace_on_value',
                parametersByColumn
            );
            expect(RecipeService.earlyPreview).toHaveBeenCalledWith(
                transformation,
                parametersByColumn
            );
        }));

	    it('should trigger a preview on multi_columns scope', inject(($timeout, PreviewService, EarlyPreviewService, RecipeService) => {
		    // given
		    stateMock.playground.grid.selectedColumns = [firstnameColumn, lastnameColumn];

		    // when
		    EarlyPreviewService.earlyPreview(transformation, 'multi_columns')(params);
		    expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();
		    $timeout.flush(700);

		    // then
		    const parametersByColumn = [
			    {
				    value: 'James',
				    replace: 'Jimmy',
				    scope: 'multi_columns',
				    column_id: firstnameColumn.id,
				    column_name: firstnameColumn.name,
			    },
			    {
				    value: 'James',
				    replace: 'Jimmy',
				    scope: 'multi_columns',
				    column_id: lastnameColumn.id,
				    column_name: lastnameColumn.name,
			    }
		    ];

		    expect(PreviewService.getPreviewAddRecords).toHaveBeenCalledWith(
			    preparation.id,
			    dataset.id,
			    'replace_on_value',
			    parametersByColumn
		    );
		    expect(RecipeService.earlyPreview).toHaveBeenCalledWith(
			    transformation,
			    parametersByColumn
		    );
	    }));
    });

    describe('cancel preview', () => {
        it('should cancel pending early preview', inject(($timeout, RecipeService, PreviewService, EarlyPreviewService) => {
            //given
            EarlyPreviewService.earlyPreview(transformation, transfoScope)(params);
            expect(RecipeService.earlyPreview).not.toHaveBeenCalled();
            expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();

            //when
            EarlyPreviewService.cancelEarlyPreview();
            $timeout.flush();

            //then
            expect(RecipeService.earlyPreview).not.toHaveBeenCalled();
            expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();
        }));

        it('should cancel current early preview', inject((RecipeService, EarlyPreviewService, PreviewService) => {
            //when
            EarlyPreviewService.cancelEarlyPreview();

            //then
            expect(RecipeService.cancelEarlyPreview).toHaveBeenCalled();
            expect(PreviewService.cancelPreview).toHaveBeenCalled();
        }));
    });

    describe('activation', () => {
        it('should NOT trigger preview when it is disabled', inject(($timeout, PreviewService, EarlyPreviewService, RecipeService) => {
            //given
	        stateMock.playground.transformationInProgress = true;

            //when
            EarlyPreviewService.earlyPreview(transformation, transfoScope)(params);
            $timeout.flush(700);

            //then
            expect(RecipeService.earlyPreview).not.toHaveBeenCalled();
            expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();
        }));


        it('should trigger preview when it is enabled', inject(($timeout, PreviewService, EarlyPreviewService, RecipeService, StateService) => {
            //given
	        stateMock.playground.transformationInProgress = true;
            EarlyPreviewService.earlyPreview(transformation, transfoScope)(params);
            $timeout.flush(700);
            expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();

            //when
	        stateMock.playground.transformationInProgress = false;
            EarlyPreviewService.earlyPreview(transformation, transfoScope)(params);
            $timeout.flush(700);

            //then
            expect(RecipeService.earlyPreview).toHaveBeenCalled();
            expect(PreviewService.getPreviewAddRecords).toHaveBeenCalled();
        }));
    });
});
