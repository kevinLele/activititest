package com.hq.cloud.activiti.demo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class FreeMarkerTest {

    @Test
    public void markerTest() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        Map<String, Object> root = new HashMap<>();
        root.put("user", "world");
        String templateStr = "<div>Hello, ${user}</div>";

        try {
            Template template = new Template(null, new StringReader(templateStr), cfg);
            StringWriter writer = new StringWriter();
            template.process(root, writer);
            System.out.println(writer.toString());
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
