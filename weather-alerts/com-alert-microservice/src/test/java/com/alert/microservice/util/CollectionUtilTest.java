package com.alert.microservice.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static com.alert.microservice.util.CollectionUtil.*;

public class CollectionUtilTest {
    // Test Constants
    private static final String TEST_STR = "TEST";

    @Test
    public void testIsEmptyArray() {
        String[] nullArray = null;
        Assert.assertTrue(CollectionUtil.isEmpty(nullArray));
        Assert.assertTrue(CollectionUtil.isEmpty(new String[]{}));
        Assert.assertFalse(CollectionUtil.isEmpty(new String[]{TEST_STR}));
    }

    @Test
    public void testIsNotEmptyArray() {
        String[] nullArray = null;
        Assert.assertFalse(CollectionUtil.isNotEmpty(nullArray));
        Assert.assertFalse(CollectionUtil.isNotEmpty(new String[]{}));
        Assert.assertTrue(CollectionUtil.isNotEmpty(new String[]{TEST_STR}));
    }

    @Test
    public void testIsEmptyCollection() {
        List nullList = null;
        Assert.assertTrue(CollectionUtil.isEmpty(nullList));
        Assert.assertTrue(CollectionUtil.isEmpty(Collections.EMPTY_LIST));
        Assert.assertFalse(CollectionUtil.isEmpty(Collections.singleton(TEST_STR)));
    }

    @Test
    public void testIsNotEmptyCollection() {
        List nullList = null;
        Assert.assertFalse(CollectionUtil.isNotEmpty(nullList));
        Assert.assertFalse(CollectionUtil.isNotEmpty(Collections.EMPTY_LIST));
        Assert.assertTrue(CollectionUtil.isNotEmpty(Collections.singleton(TEST_STR)));
    }

    @Test
    public void testFirstOrNull() {
        Assert.assertEquals(TEST_STR, CollectionUtil.firstOrNull(CollectionUtil.listOf(TEST_STR)));
        Assert.assertNull(CollectionUtil.firstOrNull(null));
    }

    @Test
    public void testFirstOrNullWithPredicate() {
        final Predicate<String> isGreaterThan1Char = str -> (str.length() > 1);
        List<String> strings = CollectionUtil.listOf(TEST_STR);
        CollectionUtil.listOf(TEST_STR).toArray();

        Assert.assertEquals(TEST_STR, CollectionUtil.firstOrNull(strings, isGreaterThan1Char));
        Assert.assertNull(CollectionUtil.firstOrNull(null, null));
        Assert.assertNull(CollectionUtil.firstOrNull(strings, null));
        Assert.assertNull(CollectionUtil.firstOrNull(null, isGreaterThan1Char));
    }

    @Test
    public void testRemoveNulls() {
        Assert.assertEquals(0, removeNulls(null).size());
        Assert.assertEquals(0, removeNulls(listOf(null, null)).size());
        Assert.assertEquals(1, removeNulls(listOf(null, TEST_STR, null)).size());
    }

    @Test
    public void testRemoveNullsAndDuplicates() {
        Assert.assertEquals(0, removeNullsAndDuplicates(null).size());
        Assert.assertEquals(0, removeNullsAndDuplicates(listOf(null, null)).size());
        Assert.assertEquals(1, removeNullsAndDuplicates(listOf(null, TEST_STR, null)).size());
        Assert.assertEquals(1, removeNullsAndDuplicates(listOf(null, TEST_STR, TEST_STR, null)).size());
    }

    @Test
    public void testListOf() {
        Assert.assertEquals(0, listOf().size());
        Assert.assertEquals(1, listOf(TEST_STR).size());
        Assert.assertEquals(2, listOf(TEST_STR, TEST_STR).size());
    }

    @Test
    public void testListOfFromCollection() {
        Assert.assertEquals(0, listOf(listOf()).size());
        Assert.assertEquals(1, listOf(listOf(TEST_STR)).size());
        Assert.assertEquals(2, listOf(listOf(TEST_STR, TEST_STR)).size());
    }

    @Test
    public void testSetOf() {
        Assert.assertEquals(0, setOf().size());
        Assert.assertEquals(1, setOf(TEST_STR).size());
        // Sets inherently remove duplicates
        Assert.assertEquals(1, setOf(TEST_STR, TEST_STR).size());
    }

    @Test
    public void testSetOfFromCollection() {
        Assert.assertEquals(0, setOf(setOf()).size());
        Assert.assertEquals(1, setOf(setOf(TEST_STR)).size());
        // Sets inherently remove duplicates
        Assert.assertEquals(1, setOf(setOf(TEST_STR, TEST_STR)).size());
    }

    @Test
    public void testSize() {
        Assert.assertEquals(0, size(null));
        Assert.assertEquals(1, size(listOf((String) null)));
        Assert.assertEquals(1, size(listOf(TEST_STR)));
    }

    @Test
    public void testStreamOnSequential() {
        String joinedElements = streamOn(setOf(TEST_STR).iterator()).collect(Collectors.joining());
        Assert.assertEquals(TEST_STR, joinedElements);
    }

    @Test
    public void testStreamOnParallel() {
        String joinedElements = streamOn(setOf(TEST_STR).iterator(), true).collect(Collectors.joining());
        Assert.assertEquals(TEST_STR, joinedElements);
    }
}
