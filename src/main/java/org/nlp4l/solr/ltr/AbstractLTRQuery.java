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

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class AbstractLTRQuery extends Query {

  protected final List<FieldFeatureExtractorFactory> featuresSpec;
  protected final List<Float> weights;

  public AbstractLTRQuery(List<FieldFeatureExtractorFactory> featuresSpec, List<Float> weights){
    this.featuresSpec = featuresSpec;
    this.weights = weights;
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
    if(o == null || !(o instanceof AbstractLTRQuery)) return false;
    AbstractLTRQuery other = (AbstractLTRQuery)o;
    return equalsWeights(other.weights) && equalsFeaturesSpec(other.featuresSpec);
  }

  public int hashCode(int prime) {
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

  protected boolean equalsWeights(List<Float> oWeights){
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

  protected boolean equalsFeaturesSpec(List<FieldFeatureExtractorFactory> oSpec){
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

  protected DocIdSetIterator getIterator(Set<Integer> allDocs){
    final List<Integer> docs = new ArrayList<Integer>(allDocs);
    Collections.sort(docs);
    return new DocIdSetIterator() {
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
    };
  }
}
