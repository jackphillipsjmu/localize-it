package com.alert.microservice.service.elasticsearch;

import com.alert.microservice.api.WeatherAlert;
import com.alert.microservice.config.weather.WeatherConstants;
import com.alert.microservice.service.ToggleComponent;
import com.alert.microservice.service.exception.AlertServiceException;
import com.alert.microservice.util.CollectionUtil;
import com.alert.microservice.util.CommonUtil;
import com.alert.microservice.util.TransformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service class to support Elasticsearch related operations
 * <p>
 * Annotations Explained:
 *
 * <ul>
 *  <li>
 *      Service = Indicates that an annotated class is a "Service".
 *  </li>
 *  <li>
 *      Value = Annotation at the field or method/constructor parameter level
 *      that indicates a default value expression for the affected argument.
 *  </li>
 * </ul>
 */
@Service
public class ElasticsearchService extends ToggleComponent  {
    // Logger for info/debug purposes
    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchService.class);
    // Constant for Elasticsearch type, this will be removed in future ES versions but for now is a necessary evil
    // that we have to deal with
    private static final String DEFAULT_ES_TYPE = "_doc";

    // Value from properties to dictate if Elasticsearch is enabled or not
    @Value(WeatherConstants.ENABLE_ELASTICSEARCH_PROPERTY)
    private Boolean elasticsearchIsEnabled;

    // Final variable that is injected in the service constructor
    private final RestHighLevelClient client;

    /**
     * Constructor for this {@link ElasticsearchService} class
     *
     * @param client {@link RestHighLevelClient}
     */
    public ElasticsearchService(RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * Performs an Elasticsearch query using data contained in the provided entity against the Elasticsearch index
     * specified. This will take out the work involved with parsing out data that is held in a typical Elasticsearch
     * {@link SearchResponse} so you can operate on the objects themselves.
     *
     * @param entity       T entity to process and extract search parameters from
     * @param index        String Elasticsearch index to query
     * @param isFuzzyQuery boolean to dictate if we should use fuzzy matching or not
     * @param clazz        Class of type T to dictate how the data is processed
     * @param <T>          Generic Type
     * @return Collection of response data pulled from an Elasticsearch {@link SearchResponse}
     * @throws IOException
     */
    public <T> Collection<T> search(final T entity, final String index, final boolean isFuzzyQuery, final Class<T> clazz) throws IOException {
        return retrieveEntityFromResponse(search(entity, index, isFuzzyQuery), clazz);
    }

    /**
     * Performs in essence a "SELECT *" query on the index data limiting the number of results if specified.
     *
     * @param index String Elasticsearch index to query
     * @param size  int the number of results to return
     * @param clazz Class to help convert the Elasticsearch response data to an object.
     * @param <T>   Generic Type
     * @return Collection of index data parsed to type T
     * @throws IOException
     */
    public <T> Collection<T> selectAll(final String index, final int size, Class<T> clazz) throws IOException {
        return retrieveEntityFromResponse(client.search(buildSearchRequest(QueryBuilders.matchAllQuery(), index, size)), clazz);
    }

    /**
     * Constructs a {@link IndexRequest} under the hood and indexes it to the specified index.
     *
     * @param entity object to index to Elasticsearch
     * @param id     String identifier to use as the ID in index, if null, one will be generated for you
     * @param index  String index name to index data into, if null, the entities class name will be used
     * @param <T>    Generic Type T
     * @return IndexResponse Elasticsearch response of an index operation
     */
    public <T> IndexResponse index(final T entity, final String id, final String index) {
        return index(toIndexRequest(entity, id, index));
    }

    /**
     * Parses an Elasticsearch {@link SearchResponse} into the provided Collection of {@link Class} objects.
     *
     * @param searchResponse {@link SearchResponse} from Elasticsearch
     * @param clazz          Class of type T to dictate how the data is processed
     * @param <T>            Generic Type
     * @return Collection of response data pulled from an Elasticsearch {@link SearchResponse}
     */
    private <T> Collection<T> retrieveEntityFromResponse(final SearchResponse searchResponse, final Class<T> clazz) {
        return CollectionUtil.streamOn(searchResponse.getHits().iterator())
                .map(hit -> TransformUtil.convert(hit.getSourceAsMap(), clazz))
                .collect(Collectors.toList());
    }

    /**
     * Attempts to index what is held in the provided {@link IndexRequest} to Elasticsearch.
     *
     * @param indexRequest used to index a typed JSON document into a specific index and make it searchable.
     * @return IndexResponse Elasticsearch response of an index operation
     */
    private IndexResponse index(final IndexRequest indexRequest) {
        try {
            return client.index(indexRequest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AlertServiceException("Cannot Index Elasticsearch Data", e);
        }
    }

    /**
     * Performs an Elasticsearch query using data contained in the provided entity against the Elasticsearch index
     * specified in this methods parameters.
     *
     * @param entity       T entity to process and extract search parameters from
     * @param index        String Elasticsearch index to query
     * @param isFuzzyQuery boolean to dictate if we should use fuzzy matching or not
     * @param <T>          Generic Type
     * @return {@link SearchResponse} from Elasticsearch
     * @throws IOException
     */
    private <T> SearchResponse search(final T entity, final String index, final boolean isFuzzyQuery) throws IOException {
        // Create a boolean query base to put in our query values
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // Process entity and put match or fuzzy queries into the bool query
        // this will build a map of entity field names -> entity field values and then run the consumer
        // over these values
        if (isFuzzyQuery) {
            processEntityMap(entity, (entry) -> boolQueryBuilder.must(new FuzzyQueryBuilder(entry.getKey(), entry.getValue())));
        } else {
            processEntityMap(entity, (entry) -> boolQueryBuilder.must(new MatchQueryBuilder(entry.getKey(), entry.getValue())));
        }
        // Create search request to Elasticsearch and perform the request
        // Note, with newer versions of ES, i.e. 7.0, you will need to append a RequestOptions param to the request
        // Ex. client.search(searchRequest, RequestOptions.DEFAULT);
        return client.search(buildSearchRequest(boolQueryBuilder, index));
    }

    /**
     * Transforms a {@link WeatherAlert} into a Elasticsearch {@link IndexRequest}
     *
     * @param entity             object to index to Elasticsearch
     * @param id                 String identifier to use as the ID in index, if null, one will be generated for you
     * @param elasticsearchIndex String index name to index data into, if null, the entities class name will be used
     * @return IndexRequest to index a typed JSON document into a specific index and make it searchable.
     */
    private <T> IndexRequest toIndexRequest(final T entity,
                                            final String id,
                                            final String elasticsearchIndex) {
        // If no entity exists, throw exception
        CommonUtil.ifNullThrowException(entity, new AlertServiceException("Cannot index null entity to Elasticsearch"));
        // If index is null or empty then use the entities class name for the index name
        final String index = CommonUtil.defaultIfNullOrEmpty(elasticsearchIndex, entity.getClass().getSimpleName().toLowerCase());
        // Create request and set type, note that type will be removed from Elasticsearch and in later versions
        // actually defaults to _doc but for ES 6.2.0 it will throw an exception so we set it explicitly
        IndexRequest indexRequest = Requests.indexRequest(index).type(DEFAULT_ES_TYPE);
        // Extract ID or generate a random UUID
        indexRequest.id(CommonUtil.defaultIfNullOrEmpty(id, UUID.randomUUID().toString()));
        // If there an existing document with the id, it will be replaced.
        indexRequest.opType(DocWriteRequest.OpType.INDEX);
        // Set the JSON representation of entity in index request
        try {
            indexRequest.source(Objects.requireNonNull(TransformUtil.writeAsJSONString(entity)), XContentType.JSON);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new AlertServiceException("Error indexing entity to Elasticsearch", e);
        }
        return indexRequest;
    }

    /**
     * Method will extract a Map of entity field names to field values and perform a {@link Consumer} action
     * on each Entry in the Map. Generally, this action is to create the specific Elasticsearch Query that
     * should be populated in the result ES query.
     *
     * @param entity T entity to process
     * @param action Represents an operation that accepts a single input argument and returns no result
     * @param <T>    Generic Type
     */
    private <T> void processEntityMap(final T entity, Consumer<Map.Entry<String, Object>> action) {
        // Build map of entity field names -> entity field values
        Map<String, Object> entityMap = TransformUtil.mapEntity(entity);
        // Stream map values from entity and perform consumer operation
        entityMap.entrySet().stream()
                .filter(this::entryNotNull)
                .forEach(action);
    }

    /**
     * Helper method to create a {@link SearchRequest} from the {@link QueryBuilder} against the
     * Elasticsearch index provided to the method.
     *
     * @param queryBuilder parent class for other Elasticsearch queries
     * @param index        String ES index name to search
     * @return SearchRequest built from the provided parameters.
     */
    private SearchRequest buildSearchRequest(final QueryBuilder queryBuilder, final String index) {
        // Build search request using the provided query and index
        // defaulting to 10 results as Elasticsearch would do
        return buildSearchRequest(queryBuilder, index, 10);
    }

    /**
     * Helper method to create a {@link SearchRequest} from the {@link QueryBuilder} against the
     * Elasticsearch index provided to the method.
     *
     * @param queryBuilder parent class for other Elasticsearch queries
     * @param index        String ES index name to search
     * @param size         size of results to return
     * @return SearchRequest built from the provided parameters.
     */
    private SearchRequest buildSearchRequest(final QueryBuilder queryBuilder, final String index, final int size) {
        // Build search request using the provided query and index
        SearchRequest searchRequest = Requests.searchRequest(index).source(new SearchSourceBuilder()
                .query(queryBuilder)
                .size(size));
        // Log out query if in debug mode then return the search request
        LOG.debug("Elasticsearch Search Request {}", searchRequest);
        return searchRequest;
    }

    /**
     * Helper method that will check the overall Map.Entry and internally stored Key/Value to see if it is null or not.
     *
     * @param entry Map.Entry to determine if it or its components are null
     * @param <K>   Generic Key Type
     * @param <V>   Generic Value Type
     * @return boolean true if the entry is not null, false otherwise
     */
    private <K, V> boolean entryNotNull(final Map.Entry<K, V> entry) {
        return Objects.nonNull(entry) && Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue());
    }

    @Override
    public boolean isEnabled() {
        return elasticsearchIsEnabled;
    }
}
