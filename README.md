# NLP4L/solr Project
This project provides a collection of Apache Solr plugins that collaborate with NLP4L/framework.

# How to build
This project uses maven. To build the project, just do:

```
$ mvn package
```

# How to deploy fileReceiver servlet
Copy the project jar file to the lib directory of Solr webapp. It usually exists under ${solr_install_dir}/server/solr-webapp/webapp/WEB-INF/lib.

```
$ cp target/nlp4l-solr-VERSION.jar ${solr_install_dir}/server/solr-webapp/webapp/WEB-INF/lib
```

In addition, the fileReceiver servlet needs to be registered in web.xml.

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
