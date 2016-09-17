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

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.noggit.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class FeaturesRequestHandler extends RequestHandlerBase {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  static final int STATE_ERROR = -1;
  static final int[] NEXT_STATE_OBJECT_START = { 1, -1, -1,  4, -1, -1, -1, -1, -1, 10, -1, -1, -1, -1, -1};
  static final int[] NEXT_STATE_OBJECT_END =   {-1, -1, -1, -1, -1, -1, -1, -1,  3, -1, 13, -1, -1,  3, 15};
  static final int[] NEXT_STATE_ARRAY_START =  {-1, -1,  3, -1, -1, -1, -1, -1, -1, -1, -1, 12, -1, -1, -1};
  static final int[] NEXT_STATE_ARRAY_END =    {-1, -1, -1, 14, -1, -1, -1, -1, -1, -1, -1, -1, 10, -1, -1};
  static final int[] NEXT_STATE_FEATURES =     {-1,  2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
  static final int[] NEXT_STATE_NAME =         {-1, -1, -1, -1,  5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
  static final int[] NEXT_STATE_TYPE =         {-1, -1, -1, -1, -1, -1,  7, -1, -1, -1, -1, -1, -1, -1, -1};
  static final int[] NEXT_STATE_PARAMS =       {-1, -1, -1, -1, -1, -1, -1, -1,  9, -1, -1, -1, -1, -1, -1};
  static final int[] NEXT_STATE_ETCKEY =       {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 11, -1, -1, -1, -1};
  static final int[] NEXT_STATE_VALUE =        {-1, -1, -1, -1, -1,  6, -1,  8, -1, 13, -1, 10, 12, -1, -1};

  List<FeaturesExtractor> extManager = new ArrayList<FeaturesExtractor>();

  /*
   * available commands:
   *   - /features?command=list&conf=<json config file name>
   *   - /features?command=extract&features=<comma separated feature names>&conf=<json config file name> (async, returns procId)
   *   - /features?command=progress&id=<procId>
   */
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
    SimpleOrderedMap<Object> results = new SimpleOrderedMap<Object>();
    String command = req.getParams().required().get("command");
    results.add("command", command);

    if(command.equals("list")){
      List<LtrFeatureSetting> settings = loadFeatureSettings(loadConfig(req));
      SimpleOrderedMap<Object> features = new SimpleOrderedMap<Object>();
      for(LtrFeatureSetting setting: settings){
        features.add("feature", setting.name);
      }
      results.add("features", features);
    }
    else if(command.equals("extract")){
      FeaturesExtractor extractor = new FeaturesExtractor();
      int procId = addFeaturesExtractor(extractor);
      extractor.start();
      results.add("procId", procId);
      results.add("progress", extractor.reportProgress());
    }
    else if(command.equals("progress")){
      int procId = req.getParams().required().getInt("id");
      FeaturesExtractor extractor = getFeaturesExtractor(procId);
      results.add("procId", procId);
      results.add("progress", extractor.reportProgress());
    }
    else{
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "unknown command " + command);
    }

    rsp.add("results", results);
  }

  static InputStream loadConfig(SolrQueryRequest req) throws Exception {
    String confFile = req.getParams().required().get("conf");
    SolrResourceLoader loader = req.getCore().getResourceLoader();

    return loader.openConfig(confFile);
  }

  static List<LtrFeatureSetting> loadFeatureSettings(String jsonConf) throws Exception {
    return loadFeatureSettings(new StringReader(jsonConf));
  }

  static List<LtrFeatureSetting> loadFeatureSettings(InputStream stream) throws Exception {
    return loadFeatureSettings(new InputStreamReader(stream, "UTF-8"));
  }

  static List<LtrFeatureSetting> loadFeatureSettings(Reader reader) throws Exception {
    List<LtrFeatureSetting> settings = new ArrayList<LtrFeatureSetting>();
    LtrFeatureSetting setting = null;

    JSONParser parser = new JSONParser(reader);

    int state = 0;
    String currentValue = null;
    LtrFeatureParam fParam = null;
    List<String> values = null;

    int ev = parser.nextEvent();
    while (ev != JSONParser.EOF) {
      int[] stateChart;
      switch(ev){
        case JSONParser.OBJECT_START:
          stateChart = NEXT_STATE_OBJECT_START;
          break;
        case JSONParser.OBJECT_END:
          stateChart = NEXT_STATE_OBJECT_END;
          break;
        case JSONParser.ARRAY_START:
          stateChart = NEXT_STATE_ARRAY_START;
          break;
        case JSONParser.ARRAY_END:
          stateChart = NEXT_STATE_ARRAY_END;
          break;
        case JSONParser.STRING:
          String str = parser.getString();
          if(parser.wasKey()){
            if(str.equals("features")) {
              stateChart = NEXT_STATE_FEATURES;
            }
            else if(str.equals("name")) {
              stateChart = NEXT_STATE_NAME;
            }
            else if(str.equals("type")) {
              stateChart = NEXT_STATE_TYPE;
            }
            else if(str.equals("params")) {
              stateChart = NEXT_STATE_PARAMS;
            }
            else{
              stateChart = NEXT_STATE_ETCKEY;
              currentValue = str;
            }
          }
          else{
            stateChart = NEXT_STATE_VALUE;
            currentValue = str;
          }
          break;
        case JSONParser.LONG:
          stateChart = NEXT_STATE_VALUE;
          currentValue = Long.toString(parser.getLong());
          break;
        case JSONParser.NUMBER:
          stateChart = NEXT_STATE_VALUE;
          currentValue = Long.toString(parser.getLong());
          break;
        case JSONParser.BIGNUMBER:
          stateChart = NEXT_STATE_VALUE;
          currentValue = Long.toString(parser.getLong());
          break;
        case JSONParser.BOOLEAN:
          stateChart = NEXT_STATE_VALUE;
          currentValue = Boolean.toString(parser.getBoolean());
          break;
        default:
          stateChart = NEXT_STATE_VALUE;
          break;
      }

      int nextState = stateChart[state];
      if(nextState == STATE_ERROR){
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, String.format("syntax error at state = %d, event = %d", state, ev));
      }

      if(state == 3 && nextState == 4){
        setting = new LtrFeatureSetting();
      }
      else if((state == 8 || state == 13) && nextState == 3){
        settings.add(setting);
        setting = null;
      }
      else if(state == 5 && nextState == 6){
        setting.name = currentValue;
      }
      else if(state == 7 && nextState == 8){
        setting.fType = currentValue;
      }
      else if(state == 9 && nextState == 13){
        setting.param = currentValue;
        currentValue = null;
      }
      else if(state == 9 && nextState == 10){
        setting.params = new ArrayList<LtrFeatureParam>();
        fParam = new LtrFeatureParam();
      }
      else if(state == 10 && nextState == 11){
        fParam.name = currentValue;
      }
      else if(state == 11 && nextState == 10){
        fParam.value = currentValue;
      }
      else if(state == 11 && nextState == 12){
        values = new ArrayList<String>();
      }
      else if(state == 12 && nextState == 12){
        values.add(currentValue);
      }
      else if(state == 12 && nextState == 10){
        fParam.values = values;
        setting.params.add(fParam);
      }
      state = nextState;

      ev = parser.nextEvent();
    }

    return settings;
  }

  @Override
  public String getDescription() {
    return "Feature extraction for NLP4L-LTR";
  }

  public static class LtrFeatureParam {
    String name;
    String value;
    List<String> values;
  }

  public static class LtrFeatureSetting {
    String name;
    String fType;
    String param;
    List<LtrFeatureParam> params;
  }

  public synchronized int addFeaturesExtractor(FeaturesExtractor extractor){
    extManager.add(extractor);
    return extManager.size();
  }

  public synchronized FeaturesExtractor getFeaturesExtractor(int procId) throws Exception {
    if(extManager.size() < procId)
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "there is no such features extraction process. procId=" + procId);
    else{
      return extManager.get(procId - 1);
    }
  }
}
