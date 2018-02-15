/*
 * Copyright 2016 org.NLP4L
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nlp4l.solr.ltr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

public final class LinearWeightQuery extends AbstractLTRQuery {

  public LinearWeightQuery(List<FieldFeatureExtractorFactory> featuresSpec, List<Float> weights){
    super(featuresSpec, weights);
  }

  @Override
  public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
    return new LinearWeight(this);
  }

  @Override
  public int hashCode() {
    final int prime = 61;
    return super.hashCode(prime);
  }

  public final class LinearWeight extends Weight {

    protected LinearWeight(Query query) {
      super(query);
    }

    /**
     * Expert: adds all terms occurring in this query to the terms set. If the
     * {@link Weight} was created with {@code needsScores == true} then this
     * method will only extract terms which are used for scoring, otherwise it
     * will extract all terms which are used for matching.
     */
    @Override
    public void extractTerms(Set<Term> set) {
      for(FieldFeatureExtractorFactory factory: featuresSpec){
        // TODO: need to remove redundant terms...
        for(Term term: factory.getTerms()){
          set.add(term);
        }
      }
    }

    @Override
    public Explanation explain(LeafReaderContext leafReaderContext, int doc) throws IOException {
      LinearWeightScorer scorer = (LinearWeightScorer)scorer(leafReaderContext);
      if(scorer != null){
        int newDoc = scorer.iterator().advance(doc);
        if (newDoc == doc) {
          return Explanation.match(scorer.score(), "sum of:", scorer.subExplanations(doc));
        }
      }
      return Explanation.noMatch("no matching terms");
    }

    @Override
    public Scorer scorer(LeafReaderContext context) throws IOException {
      List<FieldFeatureExtractor[]> spec = new ArrayList<FieldFeatureExtractor[]>();
      Set<Integer> allDocs = new HashSet<Integer>();
      for(FieldFeatureExtractorFactory factory: featuresSpec){
        FieldFeatureExtractor[] extractors = factory.create(context, allDocs);
        spec.add(extractors);
      }

      if(allDocs.size() == 0) return null;
      else{
        return new LinearWeightScorer(this, spec, weights, getIterator(allDocs));
      }
    }

    @Override
    public boolean isCacheable(LeafReaderContext leafReaderContext) {
      return false;
    }
  }
}
