package org.codehaus.jackson.map.jsontype;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.BaseMapTest;
import org.codehaus.jackson.map.ObjectMapper;

public class TestScalars extends BaseMapTest
{
    private static class AbstractWrapper {
        @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY)
        public Object value;
        
        @SuppressWarnings("unused")
        public AbstractWrapper() { }
        public AbstractWrapper(Object v) { value = v; }
    }

    static enum TestEnum { A, B; }
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    
    /**
     * Ensure that per-property dynamic types work, both for "native" types
     * and others
     */
    public void testScalarsAsAbstract() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        String json;
        AbstractWrapper result;

        // first, check "native" types
        json = m.writeValueAsString(new AbstractWrapper(Integer.valueOf(3)));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals(Integer.valueOf(3), result.value);

        json = m.writeValueAsString(new AbstractWrapper("abc"));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals("abc", result.value);

        json = m.writeValueAsString(new AbstractWrapper("abc"));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals("abc", result.value);

        json = m.writeValueAsString(new AbstractWrapper(Boolean.TRUE));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals(Boolean.TRUE, result.value);
        
        // then verify other scalars
        json = m.writeValueAsString(new AbstractWrapper(Long.valueOf(7L)));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals(Long.valueOf(7), result.value);

        json = m.writeValueAsString(new AbstractWrapper(TestEnum.B));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals(TestEnum.B, result.value);
    }
}