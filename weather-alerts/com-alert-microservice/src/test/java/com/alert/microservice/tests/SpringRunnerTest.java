package com.alert.microservice.tests;

import com.alert.microservice.Application;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Abstract class that child classes can extend to run Spring Runner/JUnit flavored tests that will pick
 * up Springs Application Context as well.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public abstract class SpringRunnerTest {
}
