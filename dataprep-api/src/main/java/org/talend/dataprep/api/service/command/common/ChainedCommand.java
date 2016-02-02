//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.command.common;

import org.apache.http.client.HttpClient;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 *
 * @param <O> Output of this command.
 * @param <I> Result of the previous command chained into this one as input.
 */
public abstract class ChainedCommand<O, I> extends GenericCommand<O> {

    /** The command to execute to get the input for this one. */
    private final HystrixCommand<I> input;

    /**
     * Constructor.
     *
     * @param group the command group.
     * @param client the http client to use.
     * @param input the command to execute to get the input.
     */
    public ChainedCommand(HystrixCommandGroupKey group, HttpClient client, HystrixCommand<I> input) {
        super(group, client);
        this.input = input;
    }

    /**
     * Simplified constructor.
     *
     * @param client the http client to use.
     * @param input the command to execute to get the input.
     */
    public ChainedCommand(HttpClient client, HystrixCommand<I> input) {
        this(input.getCommandGroup(), client, input);
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
