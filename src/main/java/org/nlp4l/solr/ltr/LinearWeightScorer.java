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

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LinearWeightScorer extends Scorer {

  private final List<FieldFeatureExtractor[]> featuresSpec;
  private final List<Float> weights;
  private final DocIdSetIterator iterator;

  public LinearWeightScorer(Weight luceneWeight, List<FieldFeatureExtractor[]> featuresSpec,
                            List<Float> weights, DocIdSetIterator iterator) {
    super(luceneWeight);
    this.featuresSpec = featuresSpec;
    this.weights = weights;
    this.iterator = iterator;
  }

  @Override
  public int docID() {
    return iterator.docID();
  }

  @Override
  public float score() throws IOException {
    final int target = docID();
    float score = 0;
    int idx = 0;
    for(FieldFeatureExtractor[] extractors: featuresSpec){
      float feature = 0;
      for(FieldFeatureExtractor extractor: extractors){
        feature += extractor.feature(target);
      }

      score += weights.get(idx) * feature;
      idx++;
    }

    return score;
  }

  @Override
  public int freq() throws IOException {
    // TODO: implement
    return 1;
  }

  @Override
  public DocIdSetIterator iterator() {
    return iterator;
  }
}
