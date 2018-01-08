//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.transformer.suggestion.rules;

import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.HIGH;
import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.MEDIUM;
import static org.talend.dataprep.transformation.api.transformer.suggestion.rules.GenericRule.GenericRuleBuilder.forActions;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.talend.dataprep.transformation.actions.datamasking.MaskDataByDomain;
import org.talend.dataprep.transformation.actions.date.ChangeDatePattern;
import org.talend.dataprep.transformation.actions.date.ComputeTimeSince;
import org.talend.dataprep.transformation.actions.date.ExtractDateTokens;
import org.talend.dataprep.transformation.actions.net.ExtractEmailDomain;
import org.talend.dataprep.transformation.actions.net.ExtractUrlTokens;
import org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

@Component
public class TypeDomainRules extends BasicRules {

    /**
     * @return A {@link SuggestionEngineRule rule} that shows date actions if column is a date column.
     */
    @Bean
    public static SuggestionEngineRule dateRule() {
        return forActions(ExtractDateTokens.ACTION_NAME, ChangeDatePattern.ACTION_NAME, ComputeTimeSince.TIME_SINCE_ACTION_NAME) //
                .when(IS_DATE) //
                .then(columnMetadata -> MEDIUM) //
                .build();
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that shows email actions if column's semantic domain is email.
     */
    @Bean
    public static SuggestionEngineRule emailRule() {
        return forActions(ExtractEmailDomain.EXTRACT_DOMAIN_ACTION_NAME) //
                .when(IS_EMAIL) //
                .then(columnMetadata -> HIGH) //
                .build();
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that shows url actions if column's semantic domain is url.
     */
    @Bean
    public static SuggestionEngineRule urlRule() {
        return forActions(ExtractUrlTokens.EXTRACT_URL_TOKENS_ACTION_NAME) //
                .when(IS_URL) //
                .then(columnMetadata -> HIGH) //
                .build();
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that shows date actions if column is a phone column.
     */
    @Bean
    public static SuggestionEngineRule phoneRule() {
        return forActions(FormatPhoneNumber.ACTION_NAME) //
                .when(IS_PHONE) //
                .then(columnMetadata -> MEDIUM) //
                .build();
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that shows date actions if column is a phone column.
     */
    @Bean
    public static SuggestionEngineRule dataMaskingRule() {
        Set<String> domainsToMask = new HashSet<>();
        domainsToMask.add(SemanticCategoryEnum.EMAIL.getId());
        domainsToMask.add(SemanticCategoryEnum.LAST_NAME.getId());
        domainsToMask.add(SemanticCategoryEnum.FIRST_NAME.getId());
        domainsToMask.add(SemanticCategoryEnum.DE_PHONE.getId());
        domainsToMask.add(SemanticCategoryEnum.FR_PHONE.getId());
        domainsToMask.add(SemanticCategoryEnum.UK_PHONE.getId());
        domainsToMask.add(SemanticCategoryEnum.US_PHONE.getId());

        return forActions(MaskDataByDomain.ACTION_NAME) //
                .when(columnMetadata -> domainsToMask.contains(columnMetadata.getDomain())) //
                .then(columnMetadata -> MEDIUM) //
                .build();
    }

}
