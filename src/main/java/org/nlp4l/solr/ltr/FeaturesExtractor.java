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

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

public class FeaturesExtractor implements Callable<Integer> {

  private int progress = 0;
  Random r = new Random();

  public FeaturesExtractor(List<LtrFeatureSetting> settings){

  }

  @Override
  public Integer call() {
    while(progress < 100){
      incProgress();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignored) {
      }
    }
    return progress;
  }

  public int reportProgress(){
    return getProgress();
  }

  private synchronized void incProgress(){
    int ri = r.nextInt(5);
    progress += ri;
    if(progress > 100) progress = 100;
  }

  private synchronized int getProgress(){
    return progress;
  }
}
