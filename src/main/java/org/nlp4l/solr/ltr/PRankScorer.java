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
import org.apache.lucene.search.Weight;

import java.io.IOException;
import java.util.List;

public class PRankScorer extends AbstractLinearWeightScorer {

  private final List<Float> bs;

  public PRankScorer(Weight luceneWeight, List<FieldFeatureExtractor[]> featuresSpec,
                     List<Float> weights, List<Float> bs, DocIdSetIterator iterator) {
    super(luceneWeight, featuresSpec, weights, iterator);
    this.bs = bs;
  }

  @Override
  public float score() throws IOException {
    final int BASE = 0;    // it may be 1 in the future
    final float s = innerProduct();

    for(int i = 0; i < bs.size(); i++){
      if(s - bs.get(i) < 0) return i + BASE;
    }

    return bs.size() + BASE;
  }
}
