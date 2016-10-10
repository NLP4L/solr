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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.SyntaxError;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class LinearWeightQParserPlugin extends QParserPlugin {
  List<FieldFeatureExtractorFactory> featuresSpec = new ArrayList<FieldFeatureExtractorFactory>();
  List<Float> weights = new ArrayList<Float>();

  @Override
  public void init(NamedList args){
    NamedList settings = (NamedList)args.get("settings");
    String featuresFileName = (String)settings.get("features");
    String modelFileName = (String)settings.get("model");

    FeaturesConfigReader fcReader = new FeaturesConfigReader(featuresFileName);
    ModelConfigReader mcReader = new ModelConfigReader(modelFileName);

    ModelConfigReader.WeightDesc[] weightDescs = mcReader.getWeightDescs();

    for(ModelConfigReader.WeightDesc weightDesc: weightDescs){
      weights.add(weightDesc.weight);
      FeaturesConfigReader.FeatureDesc featureDesc = fcReader.getFeatureDesc(weightDesc.name);
      if(featureDesc == null){
        throw new IllegalArgumentException("no such feature " + weightDesc.name + " in the feature conf file");
      }
      FieldFeatureExtractorFactory dfeFactory = loadFactory(featureDesc);
      featuresSpec.add(dfeFactory);
    }
  }

  private FieldFeatureExtractorFactory loadFactory(FeaturesConfigReader.FeatureDesc featureDesc){
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try {
      Class<? extends FieldFeatureExtractorFactory> cls = (Class<? extends FieldFeatureExtractorFactory>) loader.loadClass(featureDesc.klass);
      Class<?>[] types = {String.class};
      Constructor<? extends FieldFeatureExtractorFactory> constructor;
      constructor = cls.getConstructor(types);
      return constructor.newInstance(featureDesc.param);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public QParser createParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    return new LinearWeightQParser(query, localParams, params, req);
  }

  public class LinearWeightQParser extends QParser {

    public LinearWeightQParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
      super(query, localParams, params, req);
    }

    @Override
    public Query parse() throws SyntaxError {
      IndexReaderContext context = req.getSearcher().getTopReaderContext();
      for(FieldFeatureExtractorFactory factory: featuresSpec){
        String fieldName = factory.getFieldName();
        FieldType fieldType = req.getSchema().getFieldType(fieldName);
        Analyzer analyzer = fieldType.getQueryAnalyzer();
        factory.init(context, terms(fieldName, qstr, analyzer));
      }

      return new LinearWeightQuery(featuresSpec, weights);
    }

    private Term[] terms(String fieldName, String qstr, Analyzer analyzer){
      List<Term> terms = new ArrayList<Term>();
      TokenStream stream = analyzer.tokenStream(fieldName, qstr);
      CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
      try {
        stream.reset();
        while(stream.incrementToken()){
          terms.add(new Term(fieldName, termAtt.toString()));
        }
        stream.end();
        stream.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      return terms.toArray(new Term[terms.size()]);
    }
  }
}
