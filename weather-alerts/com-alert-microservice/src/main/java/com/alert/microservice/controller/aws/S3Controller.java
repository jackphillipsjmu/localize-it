package com.alert.microservice.controller.aws;

import com.alert.microservice.api.AwsLambdaProperties;
import com.alert.microservice.service.aws.S3FileService;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller to handle AWS S3 related requests.
 *
 * Annotations Explained:
 * <ul>
 *     <li>
 *         RestController = A convenience annotation that is itself annotated with Controller  and ResponseBody.
 *     </li>
 *     <li>
 *         RequestMapping = Annotation for mapping web requests onto methods in request-handling classes with
 *         flexible method signatures.
 *     </li>
 *     <li>
 *        ApiOperation = Describes an operation or typically a HTTP method against a specific path.
 *     </li>
 *     <li>
 *        PathVariable = Indicates that a method parameter should be bound to a URI template variable.
 *     </li>
 *     <li>
 *        RequestParam = Indicates that a method parameter should be bound to a web request parameter.
 *     </li>
 *     <li>
 *         RequestPart = Annotation that can be used to associate the part of a "multipart/form-data" request with a
 *         method argument.
 *     </li>
 * </ul>
 */
@Api(tags = "AWS S3 API")
@RestController
@RequestMapping("/aws/s3")
public class S3Controller {
    // Final variables that are injected in the constructor
    private final S3FileService s3FileService;

    /**
     * Constructor for this {@link S3Controller}
     *
     * @param s3FileService Service to perform AWS S3 related operations
     */
    public S3Controller(S3FileService s3FileService) {
        this.s3FileService = s3FileService;
    }

    @ApiOperation(value = "Downloads the S3 object to your machine", response = ResponseEntity.class)
    @GetMapping(value = "/download/{bucketName}/{bucketKey}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable("bucketName") String bucketName,
                                                            @PathVariable("bucketKey") String bucketKey) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=" + bucketKey)
                .body(new InputStreamResource(s3FileService.retrieveBucketInputStream(bucketName, bucketKey)));
    }

    @ApiOperation(value = "Retrieves a List of AWS S3 Buckets", response = Bucket.class, responseContainer = "List")
    @GetMapping("/buckets/info")
    public List<Bucket> getBuckets() {
        return s3FileService.buckets();
    }

    @ApiOperation(value = "Retrieves a List of AWS S3 Bucket Names", response = String.class, responseContainer = "List")
    public List<String> getBucketNames() {
        return s3FileService.bucketNames();
    }

    @ApiOperation(value = "Uploads file to the specified bucket", response = PutObjectResult.class)
    @PostMapping("/upload/{bucketName}")
    public PutObjectResult uploadData(
            @PathVariable("bucketName") String bucketName,
            @RequestPart("file") MultipartFile file) throws IOException {
        return s3FileService.uploadToBucket(file, bucketName);
    }

    @ApiOperation(value = "Creates the specified bucket if it does NOT exist already", response = ResponseEntity.class)
    @PostMapping("/bucket/{bucketName}")
    public ResponseEntity<String> createBucket(@PathVariable("bucketName") String bucketName) {
        // Create bucket if possible, if it returns false then a bucket already existed with that name
        boolean bucketCreated = s3FileService.createBucket(bucketName);
        // Based on if the bucket was created or not respond with the appropriated HTTP status
        return ResponseEntity.status(bucketCreated ? 201 : 200)
                .body("Bucket Created = " + bucketCreated);
    }

    @ApiOperation(value = "Deletes S3 Buckets forcing Deletion if Specified", response = ResponseEntity.class)
    @DeleteMapping("/buckets")
    public ResponseEntity deleteS3Buckets(@RequestParam(name = "force", required = false) Optional<Boolean> force) {
        s3FileService.deleteAllBuckets(force.orElse(false));
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Deletes S3 Bucket forcing Deletion if Specified", response = ResponseEntity.class)
    @DeleteMapping("/bucket/{bucketName}")
    public ResponseEntity deleteS3Bucket(@PathVariable("bucketName") String bucketName,
                                         @RequestParam(name = "force", required = false) Optional<Boolean> force) {
        s3FileService.deleteBucket(bucketName, force.orElse(false));
        return ResponseEntity.ok().build();
    }
}
