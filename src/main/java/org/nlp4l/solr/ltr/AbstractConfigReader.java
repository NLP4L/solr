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
import org.apache.solr.core.SolrResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class AbstractConfigReader {

  protected final Config config;

  public AbstractConfigReader(SolrResourceLoader loader, String fileName){
    this(loader, fileName, null);
  }

  public AbstractConfigReader(SolrResourceLoader loader, String fileName, String path){
    Config c = load(loader, fileName);
    if(path == null) config = c;
    else config = c.getConfig(path);
  }

  public AbstractConfigReader(String content){
    config = ConfigFactory.parseString(content);
  }

  public static Config load(SolrResourceLoader loader, String fileName){
    if(loader == null)
      loader = new SolrResourceLoader();
    InputStream is = null;
    try {
      is = loader.openResource(fileName);
      return ConfigFactory.parseReader(new InputStreamReader(is));
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeWhileHandlingException(is);
    }
  }
}
