package spark.to.s3.processor;

/**
 * Interface for use by underlying data processor classes
 * @param <R>
 */
public interface DataProcessor<R> {

    /**
     * Process some form of data and return R
     * @return R
     */
    R process();
}
