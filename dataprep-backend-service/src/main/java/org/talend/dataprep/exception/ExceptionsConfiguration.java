// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.exception;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.talend.daikon.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;
import static org.talend.dataprep.conversions.BeanConversionService.fromBean;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.processor.BeanConversionServiceWrapper;

@Configuration
@SuppressWarnings("InsufficientBranchCoverage")
public class ExceptionsConfiguration {

    @Bean
    public Aspects getAspect() {
        return new Aspects();
    }

    @Component
    public class ExceptionsConversions extends BeanConversionServiceWrapper {

        @Override
        public BeanConversionService doWith(BeanConversionService conversionService, String beanName, ApplicationContext applicationContext) {

            conversionService.register(fromBean(TalendRuntimeException.class)
                    .toBeans(TdpExceptionDto.class).using(TdpExceptionDto.class, (internal, dto) -> {
                        ErrorCode errorCode = internal.getCode();
                        String serializedCode = errorCode.getProduct() + '_' + errorCode.getGroup() + '_' + errorCode.getCode();
                        TdpExceptionDto cause = internal.getCause() instanceof TDPException
                                ? conversionService.convert(internal.getCause(), TdpExceptionDto.class) : null;
                        Map<String, Object> context = new HashMap<>();
                        for (Map.Entry<String, Object> contextEntry : internal.getContext().entries()) {
                            context.put(contextEntry.getKey(), contextEntry.getValue());
                        }

                        dto.setMessage(internal.getLocalizedMessage());
                        dto.setDefaultMessage(internal.getMessage());
                        dto.setCode(serializedCode);
                        dto.setCause(cause);
                        dto.setContext(context);
                        return dto;
                    }).build());


            conversionService.register(fromBean(TdpExceptionDto.class)
                    .toBeans(TalendRuntimeException.class).using(TalendRuntimeException.class, (dto, internal) -> {
                        String completeErrorCode = dto.getCode();
                        String productCode = substringBefore(completeErrorCode, "_");
                        String groupCode = substringBefore(substringAfter(completeErrorCode, "_"), "_"); //$NON-NLS-1$ //$NON-NLS-2$
                        String errorCode;
                        if (completeErrorCode == null) {
                            errorCode = UNEXPECTED_EXCEPTION.getCode();
                        } else {
                            errorCode = substringAfter(completeErrorCode, productCode + '_' + groupCode + '_');
                        }

                        ErrorCodeDto errorCodeDto = new ErrorCodeDto().setCode(errorCode)
                                .setGroup(groupCode)
                                .setProduct(productCode)
                                .setHttpStatus(null); // default that may be changed after

                        return new TDPException(errorCodeDto, null, dto.getMessage(), dto.getMessageTitle(),
                                ExceptionContext.build().from(dto.getContext()).put("cause", dto.getCause()));
                    }).build());

            return conversionService;
        }
    }

}
