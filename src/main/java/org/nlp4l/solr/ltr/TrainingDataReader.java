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

public class TrainingDataReader extends AbstractConfigReader {

  private final QueryDataDesc[] queryDataDescs;
  private final String idField;
  private final int totalDocs;

  public TrainingDataReader(String content) {
    super(content, "data");
    idField = config.getString("idField");
    List<? extends Config> queriesData = config.getConfigList("queries");
    queryDataDescs = new QueryDataDesc[queriesData.size()];
    int i = 0;
    int totalDocs = 0;
    for(Config queryData: queriesData){
      int qid = queryData.getInt("qid");
      String queryStr = queryData.getString("query");
      List<String> docs = queryData.getStringList("docs");
      totalDocs += docs.size();
      queryDataDescs[i++] = new QueryDataDesc(qid, queryStr, docs.toArray(new String[docs.size()]));
    }
    this.totalDocs = totalDocs;
  }

  public String getIdField(){
    return idField;
  }

  public QueryDataDesc[] getQueryDataDescs(){
    return queryDataDescs;
  }

  public int getTotalDocs(){
    return totalDocs;
  }

  public static class QueryDataDesc {
    public final int qid;
    public final String queryStr;
    public final String[] docs;
    public QueryDataDesc(int qid, String queryStr, String[] docs){
      this.qid = qid;
      this.queryStr = queryStr;
      this.docs = docs;
    }
  }
}
