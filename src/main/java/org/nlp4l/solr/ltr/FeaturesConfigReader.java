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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeaturesConfigReader extends  AbstractConfigReader {

  private final FeatureDesc[] featureDescs;
  private final Map<String, FeatureDesc> fdMap;

  public FeaturesConfigReader(String fileName){
    super(fileName, "conf");
    List<? extends Config> featuresConfig = config.getConfigList("features");
    featureDescs = new FeatureDesc[featuresConfig.size()];
    fdMap = new HashMap<String, FeatureDesc>();
    int i = 0;
    for(Config featureConfig: featuresConfig){
      String name = featureConfig.getString("name");
      String klass = featureConfig.getString("class");
      String param = featureConfig.getConfig("params").getString("field");  // TODO: consider multiple parameters
      featureDescs[i] = new FeatureDesc(name, klass, param);
      fdMap.put(name, featureDescs[i]);
      i++;
    }
  }

  public FeatureDesc[] getFeatureDescs(){
    return featureDescs;
  }

  public FeatureDesc getFeatureDesc(String name){
    return fdMap.get(name);
  }

  public static class FeatureDesc {
    public final String name;
    public final String klass;
    public final String param;   // TODO: change param to params as a type of Map<key, value>
    public FeatureDesc(String name, String klass, String param){
      this.name = name;
      this.klass = klass;
      this.param = param;
    }
  }
}