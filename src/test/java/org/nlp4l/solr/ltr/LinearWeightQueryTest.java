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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class LinearWeightQueryTest extends LuceneTestCase {

  @Test
  public void testToStringBeforeInit() throws Exception {
    LinearWeightQuery linearWeightQuery = new LinearWeightQuery(buildFeaturesSpec(
            getTF("TF in title", "title"), getTF("TF in body", "body"), getIDF("IDF in title", "title"), getTFIDF("TFIDF in body", "body")),
            buildWeights(3.0F, 4.0F, 1.5F, 2.5F));
    assertEquals("org.nlp4l.solr.ltr.LinearWeightQuery featuresSpec=[" +
                    "org.nlp4l.solr.ltr.FieldFeatureTFExtractorFactory[]," +
                    "org.nlp4l.solr.ltr.FieldFeatureTFExtractorFactory[]," +
                    "org.nlp4l.solr.ltr.FieldFeatureIDFExtractorFactory[]," +
                    "org.nlp4l.solr.ltr.FieldFeatureTFIDFExtractorFactory[]]," +
                    " weights=[3.0,4.0,1.5,2.5]",
            linearWeightQuery.toString());
  }

  @Test
  public void testToStringAfterInit() throws Exception {
    Directory dir = newDirectory();
    RandomIndexWriter w = new RandomIndexWriter(random(), dir, newIndexWriterConfig().setMergePolicy(NoMergePolicy.INSTANCE));

    DirectoryReader reader = w.getReader();
    IndexReaderContext context = reader.getContext();

    LinearWeightQuery linearWeightQuery = new LinearWeightQuery(buildFeaturesSpec(
            getTF("TF in title", "title", context, new Term("title", "foo")),
            getTF("TF in body", "body", context, new Term("body", "bar")),
            getIDF("IDF in title", "title", context, new Term("title", "foo"))),
            buildWeights(3.0F, 4.0F, 1.5F));
    assertEquals("org.nlp4l.solr.ltr.LinearWeightQuery featuresSpec=[" +
                    "org.nlp4l.solr.ltr.FieldFeatureTFExtractorFactory[title:foo]," +
                    "org.nlp4l.solr.ltr.FieldFeatureTFExtractorFactory[body:bar]," +
                    "org.nlp4l.solr.ltr.FieldFeatureIDFExtractorFactory[title:foo]], weights=[3.0,4.0,1.5]",
            linearWeightQuery.toString());

    IOUtils.close(reader, w, dir);
  }

  @Test
  public void testLinearWeightQuery() throws Exception {
    Directory dir = newDirectory();
    RandomIndexWriter w = new RandomIndexWriter(random(), dir, newIndexWriterConfig().setMergePolicy(NoMergePolicy.INSTANCE));

    Document doc = new Document();
    doc.add(new StringField("title", "foo", Field.Store.YES));
    doc.add(new StringField("body", "foo", Field.Store.YES));
    doc.add(new StringField("len", "12", Field.Store.YES));
    w.addDocument(doc);
    w.getReader().close();

    doc = new Document();
    doc.add(new StringField("title", "foo", Field.Store.YES));
    doc.add(new StringField("body", "bar", Field.Store.YES));
    doc.add(new StringField("len", "22", Field.Store.YES));
    w.addDocument(doc);
    w.getReader().close();

    DirectoryReader reader = w.getReader();
    IndexReaderContext context = reader.getContext();

    LinearWeightQuery linearWeightQuery = new LinearWeightQuery(buildFeaturesSpec(
            getTF("TF in title", "title", context, new Term("title", "foo")),
            getTF("TF in body", "body", context, new Term("body", "bar")),
            getIDF("IDF in title", "title", context, new Term("title", "foo")),
            getTFIDF("TFIDF in body", "body", context, new Term("body", "bar")),
            getSV("length in body", "len", context, new Term("title", "foo"), new Term("body", "foo"))),
            buildWeights(3.0F, 4.0F, 1.5F, 2.5F, 6.0F));

    IndexSearcher searcher = new IndexSearcher(reader);
    TopDocs topDocs = searcher.search(linearWeightQuery, 10);
    assertEquals(2, topDocs.totalHits);

    // title: foo, body:bar
    assertEquals("bar", searcher.doc(topDocs.scoreDocs[0].doc).get("body"));
    float expectedScore = 3 * 1 + 4 * 1 + 1.5F * (float)Math.log(3.0 / 2.0) + 2.5F * 1 * (float)Math.log(3.0 / 1.0) + 22 * 6.0F;
    assertEquals(expectedScore, topDocs.scoreDocs[0].score, 0.0005);

    // title: foo, body:foo
    assertEquals("foo", searcher.doc(topDocs.scoreDocs[1].doc).get("body"));
    expectedScore = 3 * 1 + 4 * 0 + 1.5F * (float)Math.log(3.0 / 2.0) + 2.5F * 0 * (float)Math.log(3.0 / 1.0) + 12 * 6.0F;
    assertEquals(expectedScore, topDocs.scoreDocs[1].score, 0.0005);

    IOUtils.close(reader, w, dir);
  }

  private FieldFeatureExtractorFactory getTF(String featureName, String fieldName){
    FieldFeatureExtractorFactory factory = new FieldFeatureTFExtractorFactory(featureName, fieldName);
    return factory;
  }

  private FieldFeatureExtractorFactory getTF(String featureName, String fieldName, IndexReaderContext context, Term... terms){
    FieldFeatureExtractorFactory factory = new FieldFeatureTFExtractorFactory(featureName, fieldName);
    if(context != null){
      factory.init(context, terms);
    }
    return factory;
  }

  private FieldFeatureExtractorFactory getIDF(String featureName, String fieldName){
    FieldFeatureExtractorFactory factory = new FieldFeatureIDFExtractorFactory(featureName, fieldName);
    return factory;
  }

  private FieldFeatureExtractorFactory getIDF(String featureName, String fieldName, IndexReaderContext context, Term... terms){
    FieldFeatureExtractorFactory factory = new FieldFeatureIDFExtractorFactory(featureName, fieldName);
    if(context != null){
      factory.init(context, terms);
    }
    return factory;
  }

  private FieldFeatureExtractorFactory getTFIDF(String featureName, String fieldName){
    FieldFeatureExtractorFactory factory = new FieldFeatureTFIDFExtractorFactory(featureName, fieldName);
    return factory;
  }

  private FieldFeatureExtractorFactory getTFIDF(String featureName, String fieldName, IndexReaderContext context, Term... terms){
    FieldFeatureExtractorFactory factory = new FieldFeatureTFIDFExtractorFactory(featureName, fieldName);
    if(context != null){
      factory.init(context, terms);
    }
    return factory;
  }

  private FieldFeatureExtractorFactory getSV(String featureName, String fieldName){
    FieldFeatureExtractorFactory factory = new FieldFeatureStoredValueExtractorFactory(featureName, fieldName);
    return factory;
  }

  private FieldFeatureExtractorFactory getSV(String featureName, String fieldName, IndexReaderContext context, Term... terms){
    FieldFeatureExtractorFactory factory = new FieldFeatureStoredValueExtractorFactory(featureName, fieldName);
    if(context != null){
      factory.init(context, terms);
    }
    return factory;
  }

  private List<FieldFeatureExtractorFactory> buildFeaturesSpec(FieldFeatureExtractorFactory... factories){
    List<FieldFeatureExtractorFactory> featuresSpec = new ArrayList<FieldFeatureExtractorFactory>();
    for(FieldFeatureExtractorFactory factory: factories){
      featuresSpec.add(factory);
    }
    return featuresSpec;
  }

  private List<Float> buildWeights(float... data){
    List<Float> weights = new ArrayList<Float>();
    for(float f: data){
      weights.add(f);
    }
    return weights;
  }
}
