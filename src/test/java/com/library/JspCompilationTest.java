package com.library;

import org.apache.jasper.JspC;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JspCompilationTest {
    @Test void allJspViewsCompileWithTomcatJasper() throws Exception {
        Path output=Path.of("target","generated-jsp");
        Files.createDirectories(output);
        JspC compiler=new JspC();
        compiler.setUriroot(Path.of("src","main","webapp").toAbsolutePath().toString());
        compiler.setOutputDir(output.toAbsolutePath().toString());
        compiler.setClassPath(System.getProperty("java.class.path"));
        compiler.setCompile(true);
        compiler.execute();
        try(var files=Files.walk(output)){
            assertTrue(files.anyMatch(p->p.toString().endsWith(".class")),"Jasper should generate compiled JSP classes");
        }
    }
}
