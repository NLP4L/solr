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

import org.apache.solr.core.SolrResourceLoader;

import java.util.List;

public class PRankModelReader extends LinearWeightModelReader {

  protected final float[] bs;

  public PRankModelReader(String fileName){
    this(null, fileName);
  }

  public PRankModelReader(SolrResourceLoader loader, String fileName){
    super(loader, fileName);
    List<Double> bbs = config.getDoubleList("bs");
    bs = new float[bbs.size()];
    for(int i = 0; i < bs.length; i++){
      bs[i] = (float)bbs.get(i).doubleValue();
    }
  }

  public float[] getBs(){
    return bs;
  }
}