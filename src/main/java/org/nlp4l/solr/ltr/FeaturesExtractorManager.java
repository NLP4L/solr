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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.lucene.util.IOUtils;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FeaturesExtractorManager {

  private final File featuresFile;
  private final FeaturesExtractor extractor;
  private final ExecutorService executor;
  private final Future<Integer> future;

  public FeaturesExtractorManager(SolrQueryRequest req, List<FieldFeatureExtractorFactory> featuresSpec, String json) throws IOException {
    featuresFile = File.createTempFile("features-", ".json");
    extractor = new FeaturesExtractor(req, featuresSpec, json, featuresFile);
    executor = Executors.newSingleThreadExecutor();
    future = executor.submit(extractor);
    executor.shutdown();
  }

  public FeaturesExtractor getExtractor(){
    return extractor;
  }

  public int getProgress(){
    return extractor.reportProgress();
  }

  public boolean isDone(){
    return future.isDone();
  }

  public void delete(){
    if(featuresFile != null){
      featuresFile.delete();
    }
  }

  public SimpleOrderedMap<Object> getResult(){
    if(future.isDone()){
      SimpleOrderedMap<Object> result = new SimpleOrderedMap<Object>();
      //Config json = ConfigFactory.parseFile(featuresFile);   // I don't know why, but this throws parse error
      // Let's use parseReader to avoid parse error
      Reader r = null;
      try{
        r = new FileReader(featuresFile);
        Config json = ConfigFactory.parseReader(new FileReader(featuresFile));
        result.add("data", parseData(json.getConfig("data")));
        return result;
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      } finally{
        IOUtils.closeWhileHandlingException(r);
      }
    }
    else return null;
  }

  SimpleOrderedMap<Object> parseData(Config data){
    SimpleOrderedMap<Object> result = new SimpleOrderedMap<Object>();
    result.add("feature", data.getStringList("feature"));
    result.add("queries", parseQueries(data.getConfigList("queries")));
    return result;
  }

  SimpleOrderedMap<Object> parseQueries(List<? extends Config> queries){
    SimpleOrderedMap<Object> result = new SimpleOrderedMap<Object>();
    for(Config q: queries){
      result.add("q", parseQ(q));
    }
    return result;
  }

  SimpleOrderedMap<Object> parseQ(Config q){
    SimpleOrderedMap<Object> result = new SimpleOrderedMap<Object>();
    result.add("qid", q.getInt("qid"));
    result.add("query", q.getString("query"));
    result.add("docs", parseDocs(q.getConfigList("docs")));
    return result;
  }

  SimpleOrderedMap<Object> parseDocs(List<? extends Config> docs){
    SimpleOrderedMap<Object> result = new SimpleOrderedMap<Object>();
    for(Config doc: docs){
      result.add("doc", parseDoc(doc));
    }
    return result;
  }

  SimpleOrderedMap<Object> parseDoc(Config doc){
    SimpleOrderedMap<Object> result = new SimpleOrderedMap<Object>();
    result.add("id", doc.getString("id"));
    result.add("feature", doc.getDoubleList("feature"));
    return result;
  }
}
