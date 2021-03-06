/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.IndexSettingsModule;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IndexAnalyzersTests extends ESTestCase {

    /**
     * test the checks in the constructor
     */
    public void testAnalyzerMapChecks() {
        Map<String, NamedAnalyzer> analyzers = new HashMap<>();
        {
            NullPointerException ex = expectThrows(NullPointerException.class,
                    () -> new IndexAnalyzers(IndexSettingsModule.newIndexSettings("index", Settings.EMPTY), analyzers,
                            Collections.emptyMap(), Collections.emptyMap()));
            assertEquals("the default analyzer must be set", ex.getMessage());
        }
        {
            analyzers.put(AnalysisRegistry.DEFAULT_ANALYZER_NAME,
                    new NamedAnalyzer("otherName", AnalyzerScope.INDEX, new StandardAnalyzer()));
            IllegalStateException ex = expectThrows(IllegalStateException.class,
                    () -> new IndexAnalyzers(IndexSettingsModule.newIndexSettings("index", Settings.EMPTY), analyzers,
                            Collections.emptyMap(), Collections.emptyMap()));
            assertEquals("default analyzer must have the name [default] but was: [otherName]", ex.getMessage());
        }
    }

    public void testAnalyzerDefaults() throws IOException {
        Map<String, NamedAnalyzer> analyzers = new HashMap<>();
        NamedAnalyzer analyzer = new NamedAnalyzer("default", AnalyzerScope.INDEX, new StandardAnalyzer());
        analyzers.put(AnalysisRegistry.DEFAULT_ANALYZER_NAME, analyzer);

        // if only "default" is set in the map, all getters should return the same analyzer
        try (IndexAnalyzers indexAnalyzers = new IndexAnalyzers(IndexSettingsModule.newIndexSettings("index", Settings.EMPTY), analyzers,
                Collections.emptyMap(), Collections.emptyMap())) {
            assertSame(analyzer, indexAnalyzers.getDefaultIndexAnalyzer());
            assertSame(analyzer, indexAnalyzers.getDefaultSearchAnalyzer());
            assertSame(analyzer, indexAnalyzers.getDefaultSearchQuoteAnalyzer());
        }

        analyzers.put(AnalysisRegistry.DEFAULT_SEARCH_ANALYZER_NAME,
                new NamedAnalyzer("my_search_analyzer", AnalyzerScope.INDEX, new StandardAnalyzer()));
        try (IndexAnalyzers indexAnalyzers = new IndexAnalyzers(IndexSettingsModule.newIndexSettings("index", Settings.EMPTY), analyzers,
                Collections.emptyMap(), Collections.emptyMap())) {
            assertSame(analyzer, indexAnalyzers.getDefaultIndexAnalyzer());
            assertEquals("my_search_analyzer", indexAnalyzers.getDefaultSearchAnalyzer().name());
            assertEquals("my_search_analyzer", indexAnalyzers.getDefaultSearchQuoteAnalyzer().name());
        }

        analyzers.put(AnalysisRegistry.DEFAULT_SEARCH_QUOTED_ANALYZER_NAME,
                new NamedAnalyzer("my_search_quote_analyzer", AnalyzerScope.INDEX, new StandardAnalyzer()));
        try (IndexAnalyzers indexAnalyzers = new IndexAnalyzers(IndexSettingsModule.newIndexSettings("index", Settings.EMPTY), analyzers,
                Collections.emptyMap(), Collections.emptyMap())) {
            assertSame(analyzer, indexAnalyzers.getDefaultIndexAnalyzer());
            assertEquals("my_search_analyzer", indexAnalyzers.getDefaultSearchAnalyzer().name());
            assertEquals("my_search_quote_analyzer", indexAnalyzers.getDefaultSearchQuoteAnalyzer().name());
        }
    }

}
