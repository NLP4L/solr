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

import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class FeaturesRequestHandler extends RequestHandlerBase {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  Map<Long, FeaturesExtractorManager> managers = new HashMap<Long, FeaturesExtractorManager>();

  /*
   * available commands:
   *   - /features?command=extract&conf=<json config file name> (async, returns procId)
   *   - /features?command=progress&id=<procId>
   *   - /features?command=download&id=<procId>
   *   - /features?command=delete&id=<procId>
   */
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
    SimpleOrderedMap<Object> results = new SimpleOrderedMap<Object>();
    String command = req.getParams().required().get("command");
    results.add("command", command);

    if(command.equals("extract")){
      FeaturesConfigReader fcReader = new FeaturesConfigReader(req.getCore().getResourceLoader(),
              req.getParams().required().get("conf"));
      FeaturesConfigReader.FeatureDesc[] featuresSpec = fcReader.getFeatureDescs();
      if(req.getContentStreams() == null){
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "no queries found");
      }
      StringBuilder queries = new StringBuilder();
      for(ContentStream cs: req.getContentStreams()){
        Reader reader = cs.getReader();
        try{
          queries.append(IOUtils.toString(reader));
        }
        finally{
          IOUtils.closeQuietly(reader);
        }
      }
      long procId = startExtractor(featuresSpec, queries.toString());
      FeaturesExtractorManager manager = getManager(procId);
      results.add("procId", procId);
      results.add("progress", manager.getProgress());
    }
    else if(command.equals("progress")){
      long procId = req.getParams().required().getLong("id");
      FeaturesExtractorManager manager = getManager(procId);
      results.add("procId", procId);
      results.add("progress", manager.getProgress());
    }
    else if(command.equals("download")){
      long procId = req.getParams().required().getLong("id");
      FeaturesExtractorManager manager = getManager(procId);
      results.add("procId", procId);
      // TODO
      results.add("progress", manager.getProgress());
    }
    else if(command.equals("delete")){
      long procId = req.getParams().required().getLong("id");
      FeaturesExtractorManager manager = getManager(procId);
      results.add("procId", procId);
      // TODO
      results.add("progress", manager.getProgress());
    }
    else{
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "unknown command " + command);
    }

    rsp.add("results", results);
  }

  @Override
  public String getDescription() {
    return "Feature extraction for NLP4L-LTR";
  }

  public long startExtractor(FeaturesConfigReader.FeatureDesc[] featuresSpec, String json){
    // use current server time as the procId
    long procId = System.currentTimeMillis();

    FeaturesExtractorManager manager = new FeaturesExtractorManager(featuresSpec, json);
    synchronized(manager) {
      managers.put(procId, manager);
    }

    return procId;
  }

  public FeaturesExtractorManager getManager(long procId){
    FeaturesExtractorManager manager = null;
    synchronized (managers){
      manager = managers.get(procId);
    }
    if(manager == null){
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, String.format("no such process (id=%d)", procId));
    }
    else{
      return manager;
    }
  }
}
