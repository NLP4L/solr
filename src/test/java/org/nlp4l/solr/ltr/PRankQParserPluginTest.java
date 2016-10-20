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

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PRankQParserPluginTest extends SolrTestCaseJ4 {

  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig.xml", "schema.xml", getFile("src/test/resources/collection1").getParent());
  }

  @Override
  @Before
  public void setUp() throws Exception {
    // if you override setUp or tearDown, you better call
    // the super classes version
    super.setUp();
    clearIndex();
    assertU(commit());
  }

  @Test
  public void testDirectUse() throws Exception {
    assertU(adoc("id", "1", "title", "this is title", "body", "this is body"));
    assertU(commit());

    ModifiableSolrParams params = new ModifiableSolrParams();
    params.add("q", "{!prank}title body").add("fl", "*,score");
    //params.add("debugQuery", "true");
    // f = 3 * 1 + 4 * 1 = 7 -> b(0) < f < b(1)
    assertQ(req(params, "indent", "on"), "*[count(//doc)=1]",
            "//result/doc[1]/str[@name='id'][.='1']",
            "//result/doc[1]/float[@name='score'][.='1.0']"
    );
  }

  @Test
  public void testReRank() throws Exception {
    assertU(adoc("id", "1", "title", "this is title", "body", "this is body body"));
    assertU(commit());

    ModifiableSolrParams params = new ModifiableSolrParams();
    params.add("q", "{!rerank reRankQuery=$rqq reRankWeight=3.0}title body");
    params.add("rqq", "{!prank}title body").add("fl","*,score");
    //params.add("debugQuery","true");
    // score = 1 [first pass] + 2 [second pass; (3 * 1 + 4 * 2 = 11 -> less than b(2)) * 3 [reRankWeight] = 7
    assertQ(req(params, "indent", "on"), "*[count(//doc)=1]",
            "//result/doc[1]/str[@name='id'][.='1']",
            "//result/doc[1]/float[@name='score'][.='7.0']"
    );
  }
}
