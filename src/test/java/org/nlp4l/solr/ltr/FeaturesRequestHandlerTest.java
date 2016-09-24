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
import org.junit.Test;

import java.util.List;

public class FeaturesRequestHandlerTest extends SolrTestCaseJ4 {

  @Test
  public void testLoadConfigSimple() throws Exception {
    String CONF_JSON = "{\n" +
            "  features: [\n" +
            "    {\n" +
            "      name: \"isBook\",\n" +
            "      type: \"org.nlp4l.solr.ltr.SolrFeature\",\n" +
            "      params: { \"fq\": [\"{!terms f=category}book\"] }\n" +
            "    },\n" +
            "    {\n" +
            "      name: \"TF feature\",\n" +
            "      type: \"org.nlp4l.solr.ltr.TFFeature\",\n" +
            "      params: \"dummy\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    List<LtrFeatureSetting> settings = FeaturesRequestHandler.loadFeatureSettings(CONF_JSON);

    assertEquals(2, settings.size());

    assertEquals("isBook", settings.get(0).name);
    assertEquals("org.nlp4l.solr.ltr.SolrFeature", settings.get(0).fType);
    assertNull(settings.get(0).param);
    assertNotNull(settings.get(0).params);
    List<LtrFeatureParam> params = settings.get(0).params;
    assertEquals(1, params.size());
    assertEquals("fq", params.get(0).name);
    assertNull(params.get(0).value);
    assertNotNull(params.get(0).values);
    assertEquals(1, params.get(0).values.size());
    assertEquals("{!terms f=category}book", params.get(0).values.get(0));

    assertEquals("TF feature", settings.get(1).name);
    assertEquals("org.nlp4l.solr.ltr.TFFeature", settings.get(1).fType);
    assertNotNull(settings.get(1).param);
    assertEquals("dummy", settings.get(1).param);
    assertNull(settings.get(1).params);
  }

  @Test
  public void testLoadConfigNoParams() throws Exception {
    String CONF_JSON = "{\n" +
            "  features: [\n" +
            "    {\n" +
            "      name: \"isBook\",\n" +
            "      type: \"org.nlp4l.solr.ltr.SolrFeature\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    List<LtrFeatureSetting> settings = FeaturesRequestHandler.loadFeatureSettings(CONF_JSON);

    assertEquals(1, settings.size());

    assertEquals("isBook", settings.get(0).name);
    assertEquals("org.nlp4l.solr.ltr.SolrFeature", settings.get(0).fType);
    assertNull(settings.get(0).param);
    assertNull(settings.get(0).params);
  }
}
