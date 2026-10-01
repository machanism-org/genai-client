package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.machanism.machai.genai.provider.TypeConverter;
import org.machanism.machai.process.tools.Param;

/** Thorough, network-free coverage for the provider type conversion utility. */
class TypeConverterCoverageTest {

    @Test
    void schemaTypesCoverSupportedAndUnknownClasses() {
        // Arrange and Act
        assertEquals("string", TypeConverter.get(String.class));
        assertEquals("string", TypeConverter.get(File.class));
        assertEquals("integer", TypeConverter.get(Integer.class));
        assertEquals("integer", TypeConverter.get(int.class));
        assertEquals("number", TypeConverter.get(Double.class));
        assertEquals("number", TypeConverter.get(double.class));
        assertEquals("boolean", TypeConverter.get(Boolean.class));
        assertEquals("boolean", TypeConverter.get(boolean.class));
        assertEquals("object", TypeConverter.get(Map.class));
        assertEquals("array", TypeConverter.get(List.class));

        // Assert
        assertEquals("object", TypeConverter.get(Object.class));
    }

    @Test
    void undefinedInputsBecomeNullWithoutAttemptingConversion() throws Exception {
        // Arrange
        Parameter parameter = parameter("string", "value");

        // Act and Assert
        assertNull(TypeConverter.convertToType(parameter, null));
        assertNull(TypeConverter.convertToType(parameter, Param.NULL));
        assertNull(TypeConverter.convertToType(parameter, Param.NOT_DEFINED));
    }

    @Test
    void convertsStringsNumbersBooleansFilesAndJsonObjects() throws Exception {
        // Arrange
        Parameter string = parameter("string", "value");
        Parameter integer = parameter("integer", "value");
        Parameter flag = parameter("flag", "value");
        Parameter file = parameter("file", "value");
        Parameter object = parameter("object", "value");

        // Act
        Object text = TypeConverter.convertToType(string, "hello");
        Object number = TypeConverter.convertToType(integer, "42");
        Object bool = TypeConverter.convertToType(flag, "true");
        Object path = TypeConverter.convertToType(file, "work.txt");
        Object json = TypeConverter.convertToType(object, "{\"name\":\"Ada\"}");

        // Assert
        assertEquals("hello", text);
        assertEquals(42, number);
        assertEquals(Boolean.TRUE, bool);
        assertEquals(new File("work.txt"), path);
        assertEquals("Ada", ((com.fasterxml.jackson.databind.JsonNode) json).get("name").asText());
    }

    @Test
    void convertsListsAndMapsUsingTheirGenericValueTypes() throws Exception {
        // Arrange
        Parameter list = parameter("strings", "value");
        Parameter integerMap = parameter("integerMap", "value");
        Parameter doubleMap = parameter("doubleMap", "value");
        Parameter stringMap = parameter("stringMap", "value");
        Parameter rawMap = parameter("rawMap", "value");

        // Act
        Object listValue = TypeConverter.convertToType(list, "[\"a\",\"b\"]");
        Object integers = TypeConverter.convertToType(integerMap, "{\"a\":2}");
        Object doubles = TypeConverter.convertToType(doubleMap, "{\"a\":2.5}");
        Object strings = TypeConverter.convertToType(stringMap, "{\"a\":\"x\"}");
        Object blankMap = TypeConverter.convertToType(stringMap, "   ");
        Object raw = TypeConverter.convertToType(rawMap, "{\"a\":\"x\"}");

        // Assert
        assertEquals(java.util.Arrays.asList("a", "b"), listValue);
        assertEquals(Integer.valueOf(2), ((Map<?, ?>) integers).get("a"));
        assertEquals(Double.valueOf(2.5), ((Map<?, ?>) doubles).get("a"));
        assertEquals("x", ((Map<?, ?>) strings).get("a"));
        assertTrue(((Map<?, ?>) blankMap).isEmpty());
        assertEquals("x", ((Map<?, ?>) raw).get("a"));
    }

    @Test
    void malformedJsonIsReportedAsAnIllegalArgumentException() throws Exception {
        // Arrange
        Parameter integer = parameter("integer", "value");

        // Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> TypeConverter.convertToType(integer, "not-a-number"));
    }

    private static Parameter parameter(String methodName, String parameterName) throws Exception {
        Method method = Samples.class.getDeclaredMethod(methodName, parameterType(methodName));
        return method.getParameters()[0];
    }

    private static Class<?> parameterType(String methodName) {
        if ("string".equals(methodName)) return String.class;
        if ("file".equals(methodName)) return File.class;
        if ("integer".equals(methodName)) return int.class;
        if ("flag".equals(methodName)) return boolean.class;
        if ("object".equals(methodName)) return com.fasterxml.jackson.databind.JsonNode.class;
        if ("strings".equals(methodName)) return List.class;
        return Map.class;
    }

    private static final class Samples {
        void string(String value) { }
        void integer(int value) { }
        void flag(boolean value) { }
        void file(File value) { }
        void object(com.fasterxml.jackson.databind.JsonNode value) { }
        void strings(List<String> value) { }
        void integerMap(Map<String, Integer> value) { }
        void doubleMap(Map<String, Double> value) { }
        void stringMap(Map<String, String> value) { }
        void rawMap(Map value) { }
    }
}
