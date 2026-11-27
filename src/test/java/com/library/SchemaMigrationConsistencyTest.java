package com.library;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaMigrationConsistencyTest {
    @Test void schemaSnapshotMatchesFlywayBaseline() throws Exception {
        String schema=Files.readString(Path.of("database/schema.sql")).replaceAll("(?m)^--.*(?:\\r?\\n|$)","");
        schema=schema.replaceAll("(?m)^CREATE DATABASE[^\\r\\n]*\\r?\\nUSE library_management;\\r?\\n?", "")
            .replace("CREATE TABLE IF NOT EXISTS","CREATE TABLE");
        String migration=Files.readString(Path.of("src/main/resources/db/migration/V1__initial_schema.sql"));
        assertEquals(normalize(schema),normalize(migration));
    }
    private String normalize(String sql){return sql.replaceAll("\\s+"," ").trim();}
}

