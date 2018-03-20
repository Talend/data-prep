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

package org.talend.dataprep.api.service.command.common;

import org.talend.dataprep.command.GenericCommand;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 *
 * @param <O> Output of this command.
 * @param <I> Result of the previous command chained into this one as input.
 */
public abstract class ChainedCommand<O, I> extends GenericCommand<O> {

    /** The command to execute to get the input for this one. */
    protected final HystrixCommand<I> input;

    /**
     * Constructor.
     *
     * @param group the command group.
     * @param input the command to execute to get the input.
     */
    public ChainedCommand(HystrixCommandGroupKey group, HystrixCommand<I> input) {
        super(group);
        this.input = input;
    }

    /**
     * Simplified constructor.
     *
     * @param input the command to execute to get the input.
     */
    public ChainedCommand(HystrixCommand<I> input) {
        this(input.getCommandGroup(), input);
    }

    /**
     * Execute the input command to get its result as input for this one.
     *
     * @return the input command result.
     */
    public I getInput() {
        return input.execute();
    }

}
