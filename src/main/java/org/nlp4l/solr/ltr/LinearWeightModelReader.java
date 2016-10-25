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
import org.apache.solr.core.SolrResourceLoader;

import java.util.List;

public class LinearWeightModelReader extends AbstractConfigReader {

  protected final WeightDesc[] weightDescs;

  public LinearWeightModelReader(String fileName){
    this(null, fileName);
  }

  public LinearWeightModelReader(SolrResourceLoader loader, String fileName){
    super(loader, fileName);
    List<? extends Config> weightsConfig = config.getConfigList("weights");
    weightDescs = new WeightDesc[weightsConfig.size()];
    int i = 0;
    for(Config weightConfig: weightsConfig){
      String name = weightConfig.getString("name");
      double weight = weightConfig.getDouble("weight");
      weightDescs[i] = new WeightDesc(name, (float)weight);
      i++;
    }
  }

  public WeightDesc[] getWeightDescs(){
    return weightDescs;
  }

  public static class WeightDesc {
    public final String name;
    public final float weight;
    public WeightDesc(String name, float weight){
      this.name = name;
      this.weight = weight;
    }

    @Override
    public String toString(){
      StringBuilder sb = new StringBuilder();
      sb.append("name=").append(name).append(",weight=").append(weight);
      return sb.toString();
    }
  }
}
