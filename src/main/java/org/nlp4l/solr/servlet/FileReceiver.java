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

package org.nlp4l.solr.servlet;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileReceiver extends HttpServlet {

  @Override
  public void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    String file = req.getParameter("file");
    if(file == null){
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "file parameter is not specified");
      return;
    }
    File f = new File(file).getAbsoluteFile();
    File parent = f.getParentFile();
    if(!parent.exists()){
      System.out.printf("*** directory '%s' doesn't exist\n", parent);
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, String.format("directory '%s' doesn't exist", parent));
    }
    else{
      ServletInputStream sis = null;
      FileOutputStream fos = null;
      try{
        sis = req.getInputStream();
        fos = new FileOutputStream(f);
        int b;
        while((b = sis.read()) != -1){
          fos.write(b);
        }
      }
      finally {
        if(fos != null) fos.close();
        if(sis != null) sis.close();
      }
    }
  }
}
