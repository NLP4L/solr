# NLP4L/solr Project
This project provides a collection of Apache Solr plugins that collaborate with NLP4L/framework.

# How to build
This project uses maven. To build the project, just do:

```
$ mvn package
```

# How to deploy the project jar file
Copy the project jar file to the lib directory of Solr webapp. It usually exists under ${solr_install_dir}/server/solr-webapp/webapp/WEB-INF/lib.

```
$ cp target/nlp4l-solr-VERSION.jar ${solr_install_dir}/server/solr-webapp/webapp/WEB-INF/lib
```

This project needs [config library developed by typesafe](https://github.com/typesafehub/config/releases) to be copied in the same directory.

```
$ cp config-1.3.1.jar ${solr_install_dir}/server/solr-webapp/webapp/WEB-INF/lib
```

# How to set up the fileReceiver servlet

The fileReceiver servlet needs to be registered in web.xml.

```
  <servlet>
    <servlet-name>fileReceiver</servlet-name>
    <servlet-class>org.nlp4l.solr.servlet.FileReceiver</servlet-class>
    <init-param>
      <param-name>root_path</param-name>
      <param-value>/path/to/solr_core</param-value>
    </init-param>
  </servlet>

  <servlet-mapping>
    <servlet-name>fileReceiver</servlet-name>
    <url-pattern>/nlp4l/receive/file</url-pattern>
  </servlet-mapping>
```

# How to set up FeaturesRequestHandler

FeaturesRequestHandler needs to be registered in solrconfig.xml.

```
<requestHandler name="/features" class="org.nlp4l.solr.ltr.FeaturesRequestHandler" startup="lazy">
  <lst name="defaults">
    <str name="conf">ltr_features.conf</str>
  </lst>
</requestHandler>
```

Where, ltr_features.conf is provided in the same directory as the directory where solrconfig.xml exists and has the structure of this file looks like below:

```
conf: {
  features: [
    {
      name: "TF in title",
      class: "org.nlp4l.solr.ltr.FieldFeatureTFExtractorFactory",
      params: { field: "title" }
    },
    {
      name: "TF in body",
      class: "org.nlp4l.solr.ltr.FieldFeatureTFExtractorFactory",
      params: { field: "body" }
    },
    {
      name: "IDF in title",
      class: "org.nlp4l.solr.ltr.FieldFeatureIDFExtractorFactory",
      params: { field: "title" }
    },
    {
      name: "IDF in body",
      class: "org.nlp4l.solr.ltr.FieldFeatureIDFExtractorFactory",
      params: { field: "body" }
    }
  ]
}
```

## test features extractor using curl

```
$ curl -X POST -H "Content-type: text/json" -d @examples/ltr-queries.json "http://localhost:8983/solr/collection1/features?command=extract"
```

You'll get procId so that you can know the progress and download the result.

## how to get the progress of the extraction process

Using procId, access the following URL to see the progress:

```
http://localhost:8983/solr/collection1/features?command=progress&procId=<proc id> 
```

## how to download the result of the features extraction

Using procId, access the following URL to download the result:

```
http://localhost:8983/solr/collection1/features?command=download&procId=<proc id> 
```

## how to delete the temporary file

As the extraction produces a temporary file, the client who requested the extraction had better delete the temporary file when it is no longer needed.

```
http://localhost:8983/solr/collection1/features?command=delete&procId=<proc id> 
```

Optionally, you can delete it when you download the result by using optional parameter "delete=true"

```
http://localhost:8983/solr/collection1/features?command=download&procId=<proc id>&delete=true
```

# How to set up QParserPlugins

## LinearWeightQParserPlugin

```
<queryParser name="linearWeight" class="org.nlp4l.solr.ltr.LinearWeightQParserPlugin">
  <lst name="settings">
    <str name="features">collection1/conf/ltr_features.conf</str>
    <str name="model">collection1/conf/linearweight_model.conf</str>
  </lst>
</queryParser>
```
