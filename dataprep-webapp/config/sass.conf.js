module.exports = `
	$brand-primary: #00A1B3;
	$brand-secondary:  #168AA6;
	
	$brand-primary-t7: #00a1b3;	
	$brand-secondary-t7: #168aa6;

    $tc-drawer-content-max-width: 100%;
    @mixin flex-full-height(){
        display: flex;
        flex-direction: column;
        flex-grow: 1;
    }
    @mixin flex-scroll(){
        min-height: 0;
        overflow-y: auto;
    }

    @import '~@talend/bootstrap-theme/src/theme/guidelines';
    @import '~@talend/bootstrap-theme/src/theme/variations/tdp';
`;
