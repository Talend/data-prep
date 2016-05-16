package org.talend.dataprep.transformation.api.transformer.json;

import java.util.Collections;
import java.util.List;

import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

public class NullAnalyzer implements Analyzer<Analyzers.Result> {

    public static final Analyzer<Analyzers.Result> INSTANCE = new NullAnalyzer();

    private static final long serialVersionUID = 1L;

    private NullAnalyzer() {
    }

    @Override
    public void init() {
        // Nothing to do
    }

    @Override

    public boolean analyze(String... strings) {
        // Nothing to do
        return true;
    }

    @Override
    public void end() {
        // Nothing to do
    }

    @Override
    public List<Analyzers.Result> getResult() {
        return Collections.emptyList();
    }

    @Override
    public Analyzer<Analyzers.Result> merge(Analyzer<Analyzers.Result> analyzer) {
        return this;
    }

    @Override
    public void close() throws Exception {
        // Nothing to do
    }
}
