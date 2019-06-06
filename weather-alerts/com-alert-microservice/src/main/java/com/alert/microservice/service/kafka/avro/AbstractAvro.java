package com.alert.microservice.service.kafka.avro;

import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.avro.AvroFactory;
import com.fasterxml.jackson.dataformat.avro.schema.AvroSchemaGenerator;

/**
 * Abstract class to house common Avro related functionality
 */
abstract class AbstractAvro {
    /**
     * Takes in the provided Class and generates a Avro formatted schema from it.
     *
     * @param clazz Class to build schema from
     * @return FormatSchema Avro used to mark schema objects that are used by some JsonParser and JsonGenerator
     * implementations to further specify structure of expected format.
     * @throws JsonMappingException
     */
    FormatSchema extractFormatSchema(Class<?> clazz) throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper(new AvroFactory());
        AvroSchemaGenerator schemaGenerator = new AvroSchemaGenerator();
        mapper.acceptJsonFormatVisitor(clazz, schemaGenerator);
        return schemaGenerator.getGeneratedSchema();
    }
}
