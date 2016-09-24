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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FeaturesExtractorManager {

  private final FeaturesExtractor extractor;
  private final ExecutorService executor;
  private final Future<Integer> future;

  public FeaturesExtractorManager(List<LtrFeatureSetting> settings){
    extractor = new FeaturesExtractor(settings);
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
}
