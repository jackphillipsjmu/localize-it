package com.alert.microservice.service.kafka.avro;

import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

/**
 * Converts Avro Kafka bytes to objects.
 *
 * @param <T> Generic Type
 */
public class GenericAvroDeserializer<T> extends AbstractAvro implements Deserializer<T> {
    // Necessary for deserializing data correctly
    private final Class<T> targetType;

    /**
     * Constructor for this Kafka {@link Deserializer}
     *
     * @param targetType Class to aid when creating the result deserialized object
     */
    public GenericAvroDeserializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    /**
     * Deserialize a record value from a byte array into a value or object.
     *
     * @param topic topic associated with the data
     * @param data  serialized bytes; may be null; implementations are recommended to handle null by returning a value
     *              or null rather than throwing an exception.
     * @return deserialized typed data; may be null
     */
    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            // Extract Avro schema and process byte data into an object
            AvroMapper mapper = new AvroMapper();
            return mapper.readerFor(targetType)
                    .with(extractFormatSchema(targetType))
                    .readValue(data);
        } catch (Exception ex) {
            throw new SerializationException("Cannot Deserialize Kafka Data from topic " + topic, ex);
        }
    }

    /**
     * Configure this class.
     *
     * @param configs configs in key/value pairs
     * @param isKey   whether is for key or value
     */
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Empty method body, no configurations to set at this time
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() {
        // Empty method body, no close operations are being performed at this time
    }
}
