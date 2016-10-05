/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

const SuggestionsStats = {
	template: `
    <div id="suggestions-stats-details">
        <sc-splitter orientation="vertical">
            <sc-split-first-pane id="help-suggestions">
                <actions-suggestions class="suggestions-part"></actions-suggestions>
            </sc-split-first-pane>
            <sc-split-second-pane id="help-stats">
                <stats-details class="stats-part"></stats-details>
            </sc-split-second-pane>
        </sc-splitter>
    </div>`,
};

export default SuggestionsStats;
