package com.hankcs.lucene;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettings;


public class HanLPAnalyzerProvider  extends AbstractIndexAnalyzerProvider<HanLPAnalyzer> {

    private final HanLPAnalyzer analyzer;

    @Inject
    public HanLPAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        analyzer = new HanLPAnalyzer();
    }

    @Override
    public HanLPAnalyzer get() {
        return this.analyzer;
    }
}
