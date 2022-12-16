package org.apache.ibatis.reflection.type.jackson.databind.type;

import org.apache.ibatis.reflection.type.jackson.databind.util.ClassUtil;
import org.apache.ibatis.reflection.type.jackson.databind.JavaType;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Simple recursive-descent parser for parsing canonical {@link JavaType}
 * representations and constructing type instances.
 */
public class TypeParser
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    protected final TypeFactory _factory;

    public TypeParser(TypeFactory f) {
        _factory = f;
    }

    /**
     * @since 2.6.2
     */
    public TypeParser withFactory(TypeFactory f) {
        return (f == _factory) ? this : new TypeParser(f);
    }

    public JavaType parse(String canonical) throws IllegalArgumentException
    {
        MyTokenizer tokens = new MyTokenizer(canonical.trim());
        JavaType type = parseType(tokens);
        // must be end, now
        if (tokens.hasMoreTokens()) {
            throw _problem(tokens, "Unexpected tokens after complete type");
        }
        return type;
    }

    protected JavaType parseType(MyTokenizer tokens)
        throws IllegalArgumentException
    {
        if (!tokens.hasMoreTokens()) {
            throw _problem(tokens, "Unexpected end-of-string");
        }
        Class<?> base = findClass(tokens.nextToken(), tokens);

        // either end (ok, non generic type), or generics
        if (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if ("<".equals(token)) {
                List<JavaType> parameterTypes = parseTypes(tokens);
                TypeBindings b = TypeBindings.create(base, parameterTypes);
                return _factory._fromClass(null, base, b);
            }
            // can be comma that separates types, or closing '>'
            tokens.pushBack(token);
        }
        return _factory._fromClass(null, base, TypeBindings.emptyBindings());
    }

    protected List<JavaType> parseTypes(MyTokenizer tokens)
        throws IllegalArgumentException
    {
        ArrayList<JavaType> types = new ArrayList<JavaType>();
        while (tokens.hasMoreTokens()) {
            types.add(parseType(tokens));
            if (!tokens.hasMoreTokens()) break;
            String token = tokens.nextToken();
            if (">".equals(token)) return types;
            if (!",".equals(token)) {
                throw _problem(tokens, "Unexpected token '"+token+"', expected ',' or '>')");
            }
        }
        throw _problem(tokens, "Unexpected end-of-string");
    }

    protected Class<?> findClass(String className, MyTokenizer tokens)
    {
        try {
            return _factory.findClass(className);
        } catch (Exception e) {
            ClassUtil.throwIfRTE(e);
            throw _problem(tokens, "Cannot locate class '"+className+"', problem: "+e.getMessage());
        }
    }

    protected IllegalArgumentException _problem(MyTokenizer tokens, String msg)
    {
        return new IllegalArgumentException(String.format("Failed to parse type '%s' (remaining: '%s'): %s",
                tokens.getAllInput(), tokens.getRemainingInput(), msg));
    }

    final static class MyTokenizer extends StringTokenizer
    {
        protected final String _input;

        protected int _index;

        protected String _pushbackToken;

        public MyTokenizer(String str) {
            super(str, "<,>", true);
            _input = str;
        }

        @Override
        public boolean hasMoreTokens() {
            return (_pushbackToken != null) || super.hasMoreTokens();
        }

        @Override
        public String nextToken() {
            String token;
            if (_pushbackToken != null) {
                token = _pushbackToken;
                _pushbackToken = null;
            } else {
                token = super.nextToken();
                _index += token.length();
                token = token.trim();
            }
            return token;
        }

        public void pushBack(String token) {
            _pushbackToken = token;
            // let's NOT change index for now, since token may have been trim()ed
        }

        public String getAllInput() { return _input; }
//        public String getUsedInput() { return _input.substring(0, _index); }
        public String getRemainingInput() { return _input.substring(_index); }
    }
}
