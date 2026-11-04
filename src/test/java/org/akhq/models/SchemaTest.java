package org.akhq.models;

import io.confluent.kafka.schemaregistry.json.JsonSchema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SchemaTest {

    @Test
    void preservesJsonSchemaDefinitionsAndReferences() {
        String jsonSchemaString = """
            {
              "$schema": "https://json-schema.org/draft/2020-12/schema",
              "$defs": {
                "customDef": {
                  "type": "object",
                  "properties": {
                    "name": {
                      "type": "string"
                    }
                  }
                }
              },
              "type": "object",
              "properties": {
                "custom": {
                  "$ref": "#/$defs/customDef"
                }
              }
            }
            """;
        JsonSchema parsedSchema = new JsonSchema(jsonSchemaString);
        io.confluent.kafka.schemaregistry.client.rest.entities.Schema restSchema = mock(
            io.confluent.kafka.schemaregistry.client.rest.entities.Schema.class
        );
        when(restSchema.getSubject()).thenReturn("json-schema");
        when(restSchema.getSchemaType()).thenReturn(JsonSchema.TYPE);

        Schema schema = new Schema(
            restSchema,
            parsedSchema,
            new Schema.Config(Schema.Config.CompatibilityLevelConfig.NONE)
        );

        assertTrue(schema.getSchema().contains("\"$defs\""));
        assertTrue(schema.getSchema().contains("\"#/$defs/customDef\""));
        assertFalse(schema.getSchema().contains("UNKNOWN#/$defs/customDef"));
    }
}
