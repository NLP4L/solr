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

import org.junit.Test;
import static org.junit.Assert.*;

public class FeaturesConfigReaderTest {

  @Test
  public void testLoader() throws Exception {
    FeaturesConfigReader fcReader = new FeaturesConfigReader("ltr_features.conf");
    FeaturesConfigReader.FeatureDesc[] featureDescs = fcReader.getFeatureDescs();
    assertEquals(featureDescs.length, 4);
    assertEquals("name=TF in title,class=org.nlp4l.solr.ltr.FieldFeatureTFExtractorFactory,params=title",
            featureDescs[0].toString());
    assertEquals("name=TF in body,class=org.nlp4l.solr.ltr.FieldFeatureTFExtractorFactory,params=body",
            featureDescs[1].toString());
    assertEquals("name=IDF in title,class=org.nlp4l.solr.ltr.FieldFeatureIDFExtractorFactory,params=title",
            featureDescs[2].toString());
    assertEquals("name=IDF in body,class=org.nlp4l.solr.ltr.FieldFeatureIDFExtractorFactory,params=body",
            featureDescs[3].toString());
  }

  @Test
  public void testGetFeatureDesc() throws Exception {
    FeaturesConfigReader fcReader = new FeaturesConfigReader("ltr_features.conf");
    assertEquals("name=TF in title,class=org.nlp4l.solr.ltr.FieldFeatureTFExtractorFactory,params=title",
            fcReader.getFeatureDesc("TF in title").toString());
    assertEquals("name=TF in body,class=org.nlp4l.solr.ltr.FieldFeatureTFExtractorFactory,params=body",
            fcReader.getFeatureDesc("TF in body").toString());
    assertEquals("name=IDF in title,class=org.nlp4l.solr.ltr.FieldFeatureIDFExtractorFactory,params=title",
            fcReader.getFeatureDesc("IDF in title").toString());
    assertEquals("name=IDF in body,class=org.nlp4l.solr.ltr.FieldFeatureIDFExtractorFactory,params=body",
            fcReader.getFeatureDesc("IDF in body").toString());
  }

  @Test
  public void testLoadFactory() throws Exception {
    FeaturesConfigReader fcReader = new FeaturesConfigReader("ltr_features.conf");
    assertEquals(FieldFeatureTFExtractorFactory.class, FeaturesConfigReader.loadFactory(fcReader.getFeatureDesc("TF in title")).getClass());
    assertEquals(FieldFeatureTFExtractorFactory.class, FeaturesConfigReader.loadFactory(fcReader.getFeatureDesc("TF in body")).getClass());
    assertEquals(FieldFeatureIDFExtractorFactory.class, FeaturesConfigReader.loadFactory(fcReader.getFeatureDesc("IDF in title")).getClass());
    assertEquals(FieldFeatureIDFExtractorFactory.class, FeaturesConfigReader.loadFactory(fcReader.getFeatureDesc("IDF in body")).getClass());
  }
}
