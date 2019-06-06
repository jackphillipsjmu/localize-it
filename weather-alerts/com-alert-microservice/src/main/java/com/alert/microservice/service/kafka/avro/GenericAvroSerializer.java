package com.alert.microservice.service.kafka.avro;

import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Converts objects to bytes to be used by Kafka Avro
 *
 * @param <T> Generic Type
 */
public class GenericAvroSerializer<T> extends AbstractAvro implements Serializer<T> {
    /**
     * Convert {@code data} into a byte array.
     *
     * @param topic topic associated with data
     * @param data typed data
     * @return serialized bytes
     */
    @Override
    public byte[] serialize(String topic, T data) {
        try {
            byte[] result = null;
            // If we have data then continue to process it
            if (Objects.nonNull(data)) {
                // Use Jackson AvroMapper to acquire schema and write to byte array
                AvroMapper avroMapper = new AvroMapper();
                result = avroMapper
                        .writer(extractFormatSchema(data.getClass()))
                        .writeValueAsBytes(data);
            }
            return result;
        } catch (Exception ex) {
            throw new SerializationException("Can't serialize data='" + data + "' for topic='" + topic + "'", ex);
        }
    }

    /**
     * Configure this class.
     * @param configs configs in key/value pairs
     * @param isKey whether is for key or value
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
