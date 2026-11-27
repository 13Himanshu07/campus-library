package com.library;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class JspSecurityAuditTest {
    @Test void jspViewsAvoidScriptletsAndInlineEventHandlers() throws Exception {
        try(Stream<Path> paths=Files.walk(Path.of("src/main/webapp"))) {
            for(Path jsp:paths.filter(p->p.toString().endsWith(".jsp")||p.toString().endsWith(".jspf")).toList()) {
                String source=Files.readString(jsp);
                assertFalse(source.matches("(?s).*<%(?!@|--).*?%>.*"),"scriptlet in "+jsp);
                assertFalse(source.matches("(?is).*\\son[a-z]+\\s*=.*"),"inline event handler in "+jsp);
                assertFalse(source.contains("escapeXml=\"false\""),"unescaped JSTL output in "+jsp);
            }
        }
    }
}
