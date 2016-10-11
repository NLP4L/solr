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

import java.io.IOException;
import java.util.Set;

public class FieldFeatureTFIDFExtractorFactory extends FieldFeatureExtractorFactory {
  private int numDocs;

  public FieldFeatureTFIDFExtractorFactory(String fieldName){
    super(fieldName);
  }

  @Override
  public void init(IndexReaderContext context, Term[] terms) {
    super.init(context, terms);
    numDocs = reader.numDocs();
  }

  @Override
  public FieldFeatureExtractor[] create(LeafReaderContext context, Set<Integer> allDocs) throws IOException {
    FieldFeatureExtractor[] extractors = new FieldFeatureExtractor[terms.length];
    int i = 0;
    for(Term term: terms){
      final TermsEnum termsEnum = getTermsEnum(context, term);
      if (termsEnum == null) {
        extractors[i] = new FieldFeatureNullExtractor();
      }
      else{
        extractors[i] = new FieldFeatureTFIDFExtractor(termsEnum.postings(null, PostingsEnum.FREQS), numDocs, reader.docFreq(term));
        // get it twice without reuse to clone it...
        PostingsEnum docs = termsEnum.postings(null, PostingsEnum.FREQS);
        for(int docId = docs.nextDoc(); docId != PostingsEnum.NO_MORE_DOCS; docId = docs.nextDoc()){
          allDocs.add(docId);
        }
      }
      i++;
    }
    return extractors;
  }
}
