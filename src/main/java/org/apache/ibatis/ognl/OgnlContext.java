//--------------------------------------------------------------------------
//	Copyright (c) 1998-2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//	Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//	Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//	Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//	Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
//  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
//  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
//  AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
//  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
//  DAMAGE.
//--------------------------------------------------------------------------
package org.apache.ibatis.ognl;

import java.util.*;

/**
 * This class defines the execution context for an OGNL expression
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class OgnlContext extends Object implements Map {
  public static final String CONTEXT_CONTEXT_KEY = "context";
  public static final String ROOT_CONTEXT_KEY = "root";
  public static final String THIS_CONTEXT_KEY = "this";
  public static final String TRACE_EVALUATIONS_CONTEXT_KEY = "_traceEvaluations";
  public static final String LAST_EVALUATION_CONTEXT_KEY = "_lastEvaluation";
  public static final String KEEP_LAST_EVALUATION_CONTEXT_KEY = "_keepLastEvaluation";
  public static final String CLASS_RESOLVER_CONTEXT_KEY = "_classResolver";
  public static final String TYPE_CONVERTER_CONTEXT_KEY = "_typeConverter";
  public static final String MEMBER_ACCESS_CONTEXT_KEY = "_memberAccess";

  private static final String PROPERTY_KEY_PREFIX = "ognl";
  private static boolean DEFAULT_TRACE_EVALUATIONS = false;
  private static boolean DEFAULT_KEEP_LAST_EVALUATION = false;

  public static final ClassResolver DEFAULT_CLASS_RESOLVER = new DefaultClassResolver();
  public static final TypeConverter DEFAULT_TYPE_CONVERTER = new DefaultTypeConverter();
  public static final MemberAccess DEFAULT_MEMBER_ACCESS = new DefaultMemberAccess(false);

  private static Map RESERVED_KEYS = new HashMap(11);

  private Object root;
  private Object currentObject;
  private Node currentNode;
  private boolean traceEvaluations = DEFAULT_TRACE_EVALUATIONS;
  private Evaluation rootEvaluation;
  private Evaluation currentEvaluation;
  private Evaluation lastEvaluation;
  private boolean keepLastEvaluation = DEFAULT_KEEP_LAST_EVALUATION;
  private Map values = new HashMap(23);
  private ClassResolver classResolver = DEFAULT_CLASS_RESOLVER;
  private TypeConverter typeConverter = DEFAULT_TYPE_CONVERTER;
  private MemberAccess memberAccess = DEFAULT_MEMBER_ACCESS;

  static {
    String s;

    RESERVED_KEYS.put(CONTEXT_CONTEXT_KEY, null);
    RESERVED_KEYS.put(ROOT_CONTEXT_KEY, null);
    RESERVED_KEYS.put(THIS_CONTEXT_KEY, null);
    RESERVED_KEYS.put(TRACE_EVALUATIONS_CONTEXT_KEY, null);
    RESERVED_KEYS.put(LAST_EVALUATION_CONTEXT_KEY, null);
    RESERVED_KEYS.put(KEEP_LAST_EVALUATION_CONTEXT_KEY, null);
    RESERVED_KEYS.put(CLASS_RESOLVER_CONTEXT_KEY, null);
    RESERVED_KEYS.put(TYPE_CONVERTER_CONTEXT_KEY, null);
    RESERVED_KEYS.put(MEMBER_ACCESS_CONTEXT_KEY, null);

    try {
      if ((s = System.getProperty(PROPERTY_KEY_PREFIX + ".traceEvaluations")) != null) {
        DEFAULT_TRACE_EVALUATIONS = Boolean.valueOf(s.trim()).booleanValue();
      }
      if ((s = System.getProperty(PROPERTY_KEY_PREFIX + ".keepLastEvaluation")) != null) {
        DEFAULT_KEEP_LAST_EVALUATION = Boolean.valueOf(s.trim()).booleanValue();
      }
    } catch (SecurityException ex) {
      // restricted access environment, just keep defaults
    }
  }

  /**
   * Constructs a new OgnlContext with the default class resolver, type converter and
   * member access.
   */
  public OgnlContext() {
    super();
  }

  /**
   * Constructs a new OgnlContext with the given class resolver, type converter and
   * member access.  If any of these parameters is null the default will be used.
   */
  public OgnlContext(ClassResolver classResolver, TypeConverter typeConverter, MemberAccess memberAccess) {
    this();
    if (classResolver != null) {
      this.classResolver = classResolver;
    }
    if (typeConverter != null) {
      this.typeConverter = typeConverter;
    }
    if (memberAccess != null) {
      this.memberAccess = memberAccess;
    }
  }

  public OgnlContext(Map values) {
    super();
    this.values = values;
  }

  public OgnlContext(ClassResolver classResolver, TypeConverter typeConverter, MemberAccess memberAccess, Map values) {
    this(classResolver, typeConverter, memberAccess);
    this.values = values;
  }

  public void setValues(Map value) {
    for (Iterator it = value.keySet().iterator(); it.hasNext();) {
      Object k = it.next();

      values.put(k, value.get(k));
    }
  }

  public Map getValues() {
    return values;
  }

  public void setClassResolver(ClassResolver value) {
    if (value == null) {
      throw new IllegalArgumentException("cannot set ClassResolver to null");
    }
    classResolver = value;
  }

  public ClassResolver getClassResolver() {
    return classResolver;
  }

  public void setTypeConverter(TypeConverter value) {
    if (value == null) {
      throw new IllegalArgumentException("cannot set TypeConverter to null");
    }
    typeConverter = value;
  }

  public TypeConverter getTypeConverter() {
    return typeConverter;
  }

  public void setMemberAccess(MemberAccess value) {
    if (value == null) {
      throw new IllegalArgumentException("cannot set MemberAccess to null");
    }
    memberAccess = value;
  }

  public MemberAccess getMemberAccess() {
    return memberAccess;
  }

  public void setRoot(Object value) {
    root = value;
  }

  public Object getRoot() {
    return root;
  }

  public boolean getTraceEvaluations() {
    return traceEvaluations;
  }

  public void setTraceEvaluations(boolean value) {
    traceEvaluations = value;
  }

  public Evaluation getLastEvaluation() {
    return lastEvaluation;
  }

  public void setLastEvaluation(Evaluation value) {
    lastEvaluation = value;
  }

  /**
   * This method can be called when the last evaluation has been used
   * and can be returned for reuse in the free pool maintained by the
   * runtime.  This is not a necessary step, but is useful for keeping
   * memory usage down.  This will recycle the last evaluation and then
   * set the last evaluation to null.
   */
  public void recycleLastEvaluation() {
    OgnlRuntime.getEvaluationPool().recycleAll(lastEvaluation);
    lastEvaluation = null;
  }

  /**
   * Returns true if the last evaluation that was done on this
   * context is retained and available through <code>getLastEvaluation()</code>.
   * The default is true.
   */
  public boolean getKeepLastEvaluation() {
    return keepLastEvaluation;
  }

  /**
   * Sets whether the last evaluation that was done on this
   * context is retained and available through <code>getLastEvaluation()</code>.
   * The default is true.
   */
  public void setKeepLastEvaluation(boolean value) {
    keepLastEvaluation = value;
  }

  public void setCurrentObject(Object value) {
    currentObject = value;
  }

  public Object getCurrentObject() {
    return currentObject;
  }

  public void setCurrentNode(Node value) {
    currentNode = value;
  }

  public Node getCurrentNode() {
    return currentNode;
  }

  /**
   * Gets the current Evaluation from the top of the stack.
   * This is the Evaluation that is in process of evaluating.
   */
  public Evaluation getCurrentEvaluation() {
    return currentEvaluation;
  }

  public void setCurrentEvaluation(Evaluation value) {
    currentEvaluation = value;
  }

  /**
   * Gets the root of the evaluation stack.
   * This Evaluation contains the node representing
   * the root expression and the source is the root
   * source object.
   */
  public Evaluation getRootEvaluation() {
    return rootEvaluation;
  }

  public void setRootEvaluation(Evaluation value) {
    rootEvaluation = value;
  }

  /**
   * Returns the Evaluation at the relative index given.  This should be
   * zero or a negative number as a relative reference back up the evaluation
   * stack.  Therefore getEvaluation(0) returns the current Evaluation.
   */
  public Evaluation getEvaluation(int relativeIndex) {
    Evaluation result = null;

    if (relativeIndex <= 0) {
      result = currentEvaluation;
      while ((++relativeIndex < 0) && (result != null)) {
        result = result.getParent();
      }
    }
    return result;
  }

  /**
   * Pushes a new Evaluation onto the stack.  This is done
   * before a node evaluates.  When evaluation is complete
   * it should be popped from the stack via <code>popEvaluation()</code>.
   */
  public void pushEvaluation(Evaluation value) {
    if (currentEvaluation != null) {
      currentEvaluation.addChild(value);
    } else {
      setRootEvaluation(value);
    }
    setCurrentEvaluation(value);
  }

  /**
   * Pops the current Evaluation off of the top of the stack.
   * This is done after a node has completed its evaluation.
   */
  public Evaluation popEvaluation() {
    Evaluation result;

    result = currentEvaluation;
    setCurrentEvaluation(result.getParent());
    if (currentEvaluation == null) {
      setLastEvaluation(getKeepLastEvaluation() ? result : null);
      setRootEvaluation(null);
      setCurrentNode(null);
    }
    return result;
  }

  /*================= Map interface =================*/
  public int size() {
    return values.size();
  }

  public boolean isEmpty() {
    return values.isEmpty();
  }

  public boolean containsKey(Object key) {
    return values.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return values.containsValue(value);
  }

  public Object get(Object key) {
    Object result;

    if (RESERVED_KEYS.containsKey(key)) {
      if (key.equals(OgnlContext.THIS_CONTEXT_KEY)) {
        result = getCurrentObject();
      } else {
        if (key.equals(OgnlContext.ROOT_CONTEXT_KEY)) {
          result = getRoot();
        } else {
          if (key.equals(OgnlContext.CONTEXT_CONTEXT_KEY)) {
            result = this;
          } else {
            if (key.equals(OgnlContext.TRACE_EVALUATIONS_CONTEXT_KEY)) {
              result = getTraceEvaluations() ? Boolean.TRUE : Boolean.FALSE;
            } else {
              if (key.equals(OgnlContext.LAST_EVALUATION_CONTEXT_KEY)) {
                result = getLastEvaluation();
              } else {
                if (key.equals(OgnlContext.KEEP_LAST_EVALUATION_CONTEXT_KEY)) {
                  result = getKeepLastEvaluation() ? Boolean.TRUE : Boolean.FALSE;
                } else {
                  if (key.equals(OgnlContext.CLASS_RESOLVER_CONTEXT_KEY)) {
                    result = getClassResolver();
                  } else {
                    if (key.equals(OgnlContext.TYPE_CONVERTER_CONTEXT_KEY)) {
                      result = getTypeConverter();
                    } else {
                      if (key.equals(OgnlContext.MEMBER_ACCESS_CONTEXT_KEY)) {
                        result = getMemberAccess();
                      } else {
                        throw new IllegalArgumentException("unknown reserved key '" + key + "'");
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    } else {
      result = values.get(key);
    }
    return result;
  }

  public Object put(Object key, Object value) {
    Object result;

    if (RESERVED_KEYS.containsKey(key)) {
      if (key.equals(OgnlContext.THIS_CONTEXT_KEY)) {
        result = getCurrentObject();
        setCurrentObject(value);
      } else {
        if (key.equals(OgnlContext.ROOT_CONTEXT_KEY)) {
          result = getRoot();
          setRoot(value);
        } else {
          if (key.equals(OgnlContext.CONTEXT_CONTEXT_KEY)) {
            throw new IllegalArgumentException("can't change " + OgnlContext.CONTEXT_CONTEXT_KEY + " in context");
          } else {
            if (key.equals(OgnlContext.TRACE_EVALUATIONS_CONTEXT_KEY)) {
              result = getTraceEvaluations() ? Boolean.TRUE : Boolean.FALSE;
              setTraceEvaluations(OgnlOps.booleanValue(value));
            } else {
              if (key.equals(OgnlContext.LAST_EVALUATION_CONTEXT_KEY)) {
                result = getLastEvaluation();
                lastEvaluation = (Evaluation) value;
              } else {
                if (key.equals(OgnlContext.KEEP_LAST_EVALUATION_CONTEXT_KEY)) {
                  result = getKeepLastEvaluation() ? Boolean.TRUE : Boolean.FALSE;
                  setKeepLastEvaluation(OgnlOps.booleanValue(value));
                } else {
                  if (key.equals(OgnlContext.CLASS_RESOLVER_CONTEXT_KEY)) {
                    result = getClassResolver();
                    setClassResolver((ClassResolver) value);
                  } else {
                    if (key.equals(OgnlContext.TYPE_CONVERTER_CONTEXT_KEY)) {
                      result = getTypeConverter();
                      setTypeConverter((TypeConverter) value);
                    } else {
                      if (key.equals(OgnlContext.MEMBER_ACCESS_CONTEXT_KEY)) {
                        result = getMemberAccess();
                        setMemberAccess((MemberAccess) value);
                      } else {
                        throw new IllegalArgumentException("unknown reserved key '" + key + "'");
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    } else {
      result = values.put(key, value);
    }
    return result;
  }

  public Object remove(Object key) {
    Object result;

    if (RESERVED_KEYS.containsKey(key)) {
      if (key.equals(OgnlContext.THIS_CONTEXT_KEY)) {
        result = getCurrentObject();
        setCurrentObject(null);
      } else {
        if (key.equals(OgnlContext.ROOT_CONTEXT_KEY)) {
          result = getRoot();
          setRoot(null);
        } else {
          if (key.equals(OgnlContext.CONTEXT_CONTEXT_KEY)) {
            throw new IllegalArgumentException("can't remove " + OgnlContext.CONTEXT_CONTEXT_KEY + " from context");
          } else {
            if (key.equals(OgnlContext.TRACE_EVALUATIONS_CONTEXT_KEY)) {
              throw new IllegalArgumentException("can't remove " + OgnlContext.TRACE_EVALUATIONS_CONTEXT_KEY + " from context");
            } else {
              if (key.equals(OgnlContext.LAST_EVALUATION_CONTEXT_KEY)) {
                result = lastEvaluation;
                setLastEvaluation(null);
              } else {
                if (key.equals(OgnlContext.KEEP_LAST_EVALUATION_CONTEXT_KEY)) {
                  throw new IllegalArgumentException("can't remove " + OgnlContext.KEEP_LAST_EVALUATION_CONTEXT_KEY + " from context");
                } else {
                  if (key.equals(OgnlContext.CLASS_RESOLVER_CONTEXT_KEY)) {
                    result = getClassResolver();
                    setClassResolver(null);
                  } else {
                    if (key.equals(OgnlContext.TYPE_CONVERTER_CONTEXT_KEY)) {
                      result = getTypeConverter();
                      setTypeConverter(null);
                    } else {
                      if (key.equals(OgnlContext.MEMBER_ACCESS_CONTEXT_KEY)) {
                        result = getMemberAccess();
                        setMemberAccess(null);
                      } else {
                        throw new IllegalArgumentException("unknown reserved key '" + key + "'");
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    } else {
      result = values.remove(key);
    }
    return result;
  }

  public void putAll(Map t) {
    for (Iterator it = t.keySet().iterator(); it.hasNext();) {
      Object k = it.next();

      put(k, t.get(k));
    }
  }

  public void clear() {
    values.clear();
    setRoot(null);
    setCurrentObject(null);
    setRootEvaluation(null);
    setCurrentEvaluation(null);
    setLastEvaluation(null);
    setCurrentNode(null);
    setClassResolver(DEFAULT_CLASS_RESOLVER);
    setTypeConverter(DEFAULT_TYPE_CONVERTER);
    setMemberAccess(DEFAULT_MEMBER_ACCESS);
  }

  public Set keySet() {
    /* Should root, currentObject, classResolver, typeConverter & memberAccess be included here? */
    return values.keySet();
  }

  public Collection values() {
    /* Should root, currentObject, classResolver, typeConverter & memberAccess be included here? */
    return values.values();
  }

  public Set entrySet() {
    /* Should root, currentObject, classResolver, typeConverter & memberAccess be included here? */
    return values.entrySet();
  }

  public boolean equals(Object o) {
    return values.equals(o);
  }

  public int hashCode() {
    return values.hashCode();
  }
}

