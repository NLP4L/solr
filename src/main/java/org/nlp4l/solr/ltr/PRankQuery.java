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

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.*;

public class PRankQuery extends AbstractLTRQuery {

  private final List<Float> bs;

  public PRankQuery(List<FieldFeatureExtractorFactory> featuresSpec, List<Float> weights, List<Float> bs){
    super(featuresSpec, weights);
    this.bs = bs;
  }

  @Override
  public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
    return new PRankWeight(this);
  }

  @Override
  public String toString(String ignored) {
    StringBuilder sb = new StringBuilder(super.toString(ignored));
    sb.append(", bs=[");
    if(bs != null){
      for(int i = 0; i < bs.size(); i++){
        if(i > 0) sb.append(',');
        sb.append(bs.get(i).toString());
      }
    }
    sb.append(']');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if(!super.equals(o)) return false;
    if(o == null || !(o instanceof PRankQuery)) return false;
    PRankQuery other = (PRankQuery)o;
    return equalsBs(other.bs);
  }

  @Override
  public int hashCode() {
    final int prime = 71;
    int result = super.hashCode(prime);
    if(bs != null){
      for(Float b: bs){
        result = prime * result + b.hashCode();
      }
    }

    return result;
  }

  protected boolean equalsBs(List<Float> oBs){
    if(this.bs == null){
      return oBs == null;
    }
    else{
      if(oBs == null) return false;
      else{
        if(this.bs.size() != oBs.size()) return false;
        else{
          for(int i = 0; i < this.bs.size(); i++){
            if(!this.bs.get(i).equals(oBs.get(i))) return false;
          }
          return true;
        }
      }
    }
  }

  public final class PRankWeight extends Weight {

    protected PRankWeight(Query query){
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
      // TODO: use PRankScorer
      PRankScorer scorer = (PRankScorer)scorer(leafReaderContext);
      if(scorer != null){
        int newDoc = scorer.iterator().advance(doc);
        if (newDoc == doc) {
          StringBuilder sb = new StringBuilder();
          sb.append("bs(");
          for(int i = 0; i < bs.size(); i++){
            if(i > 0) sb.append(',');
            sb.append(bs.get(i));
          }
          sb.append(')');
          return Explanation.match(scorer.score(),
                  String.format("is the index of %s > %f sum of:", sb.toString(), scorer.innerProduct()),
                  scorer.subExplanations(doc));
        }
      }
      return Explanation.noMatch("no matching terms");
    }

    @Override
    public float getValueForNormalization() throws IOException {
      // nothing to do
      return 1.0f;
    }

    @Override
    public void normalize(float norm, float boost) {
      // nothing to do
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
        return new PRankScorer(this, spec, weights, bs, getIterator(allDocs));
      }
    }
  }
}
