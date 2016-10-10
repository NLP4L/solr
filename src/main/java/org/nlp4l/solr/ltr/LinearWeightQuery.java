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

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.*;

public final class LinearWeightQuery extends Query {

  private final List<FieldFeatureExtractorFactory> featuresSpec;
  private final List<Float> weights;

  public LinearWeightQuery(List<FieldFeatureExtractorFactory> featuresSpec, List<Float> weights){
    this.featuresSpec = featuresSpec;
    this.weights = weights;
  }

  @Override
  public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
    return new LinearWeight(searcher, this);
  }

  @Override
  public String toString(String ignored) {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getName());
    sb.append(" featuresSpec=[");
    if(featuresSpec != null){
      for(int i = 0; i < featuresSpec.size(); i++){
        if(i > 0) sb.append(',');
        sb.append(featuresSpec.get(i).toString());
      }
    }
    sb.append("], weights=[");
    if(weights != null){
      for(int i = 0; i < weights.size(); i++){
        if(i > 0) sb.append(',');
        sb.append(weights.get(i).toString());
      }
    }
    sb.append(']');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if(o == null || !(o instanceof LinearWeightQuery)) return false;
    LinearWeightQuery other = (LinearWeightQuery)o;
    return equalsWeights(other.weights) && equalsFeaturesSpec(other.featuresSpec);
  }

  private boolean equalsWeights(List<Float> oWeights){
    if(this.weights == null){
      return oWeights == null;
    }
    else{
      if(oWeights == null) return false;
      else{
        if(this.weights.size() != oWeights.size()) return false;
        else{
          for(int i = 0; i < this.weights.size(); i++){
            if(!this.weights.get(i).equals(oWeights.get(i))) return false;
          }
          return true;
        }
      }
    }
  }

  private boolean equalsFeaturesSpec(List<FieldFeatureExtractorFactory> oSpec){
    if(this.featuresSpec == null){
      return oSpec == null;
    }
    else{
      if(oSpec == null) return false;
      else{
        if(this.featuresSpec.size() != oSpec.size()) return false;
        else{
          for(int i = 0; i < this.featuresSpec.size(); i++){
            if(!this.featuresSpec.get(i).equals(oSpec.get(i))) return false;
          }
          return true;
        }
      }
    }
  }

  @Override
  public int hashCode() {
    final int prime = 61;
    int result = 1;
    if(featuresSpec != null){
      for(FieldFeatureExtractorFactory factory: featuresSpec){
        result = prime * result + factory.hashCode();
      }
    }
    if(weights != null){
      for(Float weight: weights){
        result = prime * result + weight.hashCode();
      }
    }

    return result;
  }

  public final class LinearWeight extends Weight {

    protected LinearWeight(IndexSearcher searcher, Query query) {
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
    public Explanation explain(LeafReaderContext leafReaderContext, int i) throws IOException {
      // TODO: implement
      return null;
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
        final List<Integer> docs = new ArrayList<Integer>(allDocs);
        Collections.sort(docs);
        return new LinearWeightScorer(this, spec, weights, new DocIdSetIterator() {
          int pos = -1;
          int docId = -1;

          @Override
          public int docID() {
            return docId;
          }

          @Override
          public int nextDoc() throws IOException {
            pos++;
            docId = pos >= docs.size() ? NO_MORE_DOCS : docs.get(pos);
            return docId;
          }

          @Override
          public int advance(int target) throws IOException {
            while(docId < target){
              nextDoc();
            }
            return docId;
          }

          @Override
          public long cost() {
            // TODO: set proper cost value...
            return 1;
          }
        });
      }
    }
  }
}
