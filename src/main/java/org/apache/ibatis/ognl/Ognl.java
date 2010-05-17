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

import java.io.StringReader;
import java.util.Map;

/**
 * <P>This class provides static methods for parsing and interpreting OGNL expressions.</P>
 * <p/>
 * <P>The simplest use of the Ognl class is to get the value of an expression from
 * an object, without extra context or pre-parsing.</P>
 * <p/>
 * <PRE>
 * import ognl.Ognl;
 * import ognl.OgnlException;
 * <p/>
 * try {
 * result = Ognl.getValue(expression, root);
 * } catch (OgnlException ex) {
 * // Report error or recover
 * }
 * </PRE>
 * <p/>
 * <P>This will parse the expression given and evaluate it against the root object
 * given, returning the result.  If there is an error in the expression, such
 * as the property is not found, the exception is encapsulated into an
 * {@link org.apache.ibatis.ognl.OgnlException OgnlException}.</P>
 * <p/>
 * <P>Other more sophisticated uses of Ognl can pre-parse expressions.  This
 * provides two advantages: in the case of user-supplied expressions it
 * allows you to catch parse errors before evaluation and it allows you to
 * cache parsed expressions into an AST for better speed during repeated use.
 * The pre-parsed expression is always returned as an <CODE>Object</CODE>
 * to simplify use for programs that just wish to store the value for
 * repeated use and do not care that it is an AST.  If it does care
 * it can always safely cast the value to an <CODE>AST</CODE> type.</P>
 * <p/>
 * <P>The Ognl class also takes a <I>context map</I> as one of the parameters
 * to the set and get methods.  This allows you to put your own variables
 * into the available namespace for OGNL expressions.  The default context
 * contains only the <CODE>#root</CODE> and <CODE>#context</CODE> keys,
 * which are required to be present.  The <CODE>addDefaultContext(Object, Map)</CODE>
 * method will alter an existing <CODE>Map</CODE> to put the defaults in.
 * Here is an example that shows how to extract the <CODE>documentName</CODE>
 * property out of the root object and append a string with the current user
 * name in parens:</P>
 * <p/>
 * <PRE>
 * private Map	context = new HashMap();
 * <p/>
 * public void setUserName(String value)
 * {
 * context.put("userName", value);
 * }
 * <p/>
 * try {
 * // get value using our own custom context map
 * result = Ognl.getValue("documentName + \" (\" + ((#userName == null) ? \"&lt;nobody&gt;\" : #userName) + \")\"", context, root);
 * } catch (OgnlException ex) {
 * // Report error or recover
 * }
 * <p/>
 * </PRE>
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 * @version 27 June 1999
 */
public abstract class Ognl {
  /**
   * Parses the given OGNL expression and returns a tree representation of the
   * expression that can be used by <CODE>Ognl</CODE> static methods.
   *
   * @param expression the OGNL expression to be parsed
   * @return a tree representation of the expression
   * @throws ExpressionSyntaxException if the expression is malformed
   * @throws OgnlException             if there is a pathological environmental problem
   */
  public static Object parseExpression(String expression) throws OgnlException {
    try {
      OgnlParser parser = new OgnlParser(new StringReader(expression));
      return parser.topLevelExpression();
    }
    catch (ParseException e) {
      throw new ExpressionSyntaxException(expression, e);
    }
    catch (TokenMgrError e) {
      throw new ExpressionSyntaxException(expression, e);
    }
  }

  /**
   * Creates and returns a new standard naming context for evaluating an OGNL
   * expression.
   *
   * @param root the root of the object graph
   * @return a new Map with the keys <CODE>root</CODE> and <CODE>context</CODE>
   *         set appropriately
   */
  public static Map createDefaultContext(Object root) {
    return addDefaultContext(root, null, null, null, new OgnlContext());
  }

  /**
   * Creates and returns a new standard naming context for evaluating an OGNL
   * expression.
   *
   * @param root the root of the object graph
   * @return a new OgnlContext with the keys <CODE>root</CODE> and <CODE>context</CODE>
   *         set appropriately
   */
  public static Map createDefaultContext(Object root, ClassResolver classResolver) {
    return addDefaultContext(root, classResolver, null, null, new OgnlContext());
  }

  /**
   * Creates and returns a new standard naming context for evaluating an OGNL
   * expression.
   *
   * @param root the root of the object graph
   * @return a new Map with the keys <CODE>root</CODE> and <CODE>context</CODE>
   *         set appropriately
   */
  public static Map createDefaultContext(Object root, ClassResolver classResolver, TypeConverter converter) {
    return addDefaultContext(root, classResolver, converter, null, new OgnlContext());
  }

  /**
   * Creates and returns a new standard naming context for evaluating an OGNL
   * expression.
   *
   * @param root the root of the object graph
   * @return a new Map with the keys <CODE>root</CODE> and <CODE>context</CODE>
   *         set appropriately
   */
  public static Map createDefaultContext(Object root, ClassResolver classResolver, TypeConverter converter, MemberAccess memberAccess) {
    return addDefaultContext(root, classResolver, converter, memberAccess, new OgnlContext());
  }

  /**
   * Appends the standard naming context for evaluating an OGNL expression
   * into the context given so that cached maps can be used as a context.
   *
   * @param root    the root of the object graph
   * @param context the context to which OGNL context will be added.
   * @return Context Map with the keys <CODE>root</CODE> and <CODE>context</CODE>
   *         set appropriately
   */
  public static Map addDefaultContext(Object root, Map context) {
    return addDefaultContext(root, null, null, null, context);
  }

  /**
   * Appends the standard naming context for evaluating an OGNL expression
   * into the context given so that cached maps can be used as a context.
   *
   * @param root    the root of the object graph
   * @param context the context to which OGNL context will be added.
   * @return Context Map with the keys <CODE>root</CODE> and <CODE>context</CODE>
   *         set appropriately
   */
  public static Map addDefaultContext(Object root, ClassResolver classResolver, Map context) {
    return addDefaultContext(root, classResolver, null, null, context);
  }

  /**
   * Appends the standard naming context for evaluating an OGNL expression
   * into the context given so that cached maps can be used as a context.
   *
   * @param root    the root of the object graph
   * @param context the context to which OGNL context will be added.
   * @return Context Map with the keys <CODE>root</CODE> and <CODE>context</CODE>
   *         set appropriately
   */
  public static Map addDefaultContext(Object root, ClassResolver classResolver, TypeConverter converter, Map context) {
    return addDefaultContext(root, classResolver, converter, null, context);
  }

  /**
   * Appends the standard naming context for evaluating an OGNL expression
   * into the context given so that cached maps can be used as a context.
   *
   * @param root    the root of the object graph
   * @param context the context to which OGNL context will be added.
   * @return Context Map with the keys <CODE>root</CODE> and <CODE>context</CODE>
   *         set appropriately
   */
  public static Map addDefaultContext(Object root, ClassResolver classResolver, TypeConverter converter, MemberAccess memberAccess, Map context) {
    OgnlContext result;

    if (!(context instanceof OgnlContext)) {
      result = new OgnlContext();
      result.setValues(context);
    } else {
      result = (OgnlContext) context;
    }
    if (classResolver != null) {
      result.setClassResolver(classResolver);
    }
    if (converter != null) {
      result.setTypeConverter(converter);
    }
    if (memberAccess != null) {
      result.setMemberAccess(memberAccess);
    }
    result.setRoot(root);
    return result;
  }

  public static void setClassResolver(Map context, ClassResolver classResolver) {
    context.put(OgnlContext.CLASS_RESOLVER_CONTEXT_KEY, classResolver);
  }

  public static ClassResolver getClassResolver(Map context) {
    return (ClassResolver) context.get(OgnlContext.CLASS_RESOLVER_CONTEXT_KEY);
  }

  public static void setTypeConverter(Map context, TypeConverter converter) {
    context.put(OgnlContext.TYPE_CONVERTER_CONTEXT_KEY, converter);
  }

  public static TypeConverter getTypeConverter(Map context) {
    return (TypeConverter) context.get(OgnlContext.TYPE_CONVERTER_CONTEXT_KEY);
  }

  public static void setMemberAccess(Map context, MemberAccess memberAccess) {
    context.put(OgnlContext.MEMBER_ACCESS_CONTEXT_KEY, memberAccess);
  }

  public static MemberAccess getMemberAccess(Map context) {
    return (MemberAccess) context.get(OgnlContext.MEMBER_ACCESS_CONTEXT_KEY);
  }

  public static void setRoot(Map context, Object root) {
    context.put(OgnlContext.ROOT_CONTEXT_KEY, root);
  }

  public static Object getRoot(Map context) {
    return context.get(OgnlContext.ROOT_CONTEXT_KEY);
  }

  public static Evaluation getLastEvaluation(Map context) {
    return (Evaluation) context.get(OgnlContext.LAST_EVALUATION_CONTEXT_KEY);
  }

  /**
   * Evaluates the given OGNL expression tree to extract a value from the given root
   * object. The default context is set for the given context and root via
   * <CODE>addDefaultContext()</CODE>.
   *
   * @param tree    the OGNL expression tree to evaluate, as returned by parseExpression()
   * @param context the naming context for the evaluation
   * @param root    the root object for the OGNL expression
   * @return the result of evaluating the expression
   * @throws MethodFailedException   if the expression called a method which failed
   * @throws NoSuchPropertyException if the expression referred to a nonexistent property
   * @throws InappropriateExpressionException
   *                                 if the expression can't be used in this context
   * @throws OgnlException           if there is a pathological environmental problem
   */
  public static Object getValue(Object tree, Map context, Object root) throws OgnlException {
    return getValue(tree, context, root, null);
  }

  /**
   * Evaluates the given OGNL expression tree to extract a value from the given root
   * object. The default context is set for the given context and root via
   * <CODE>addDefaultContext()</CODE>.
   *
   * @param tree       the OGNL expression tree to evaluate, as returned by parseExpression()
   * @param context    the naming context for the evaluation
   * @param root       the root object for the OGNL expression
   * @param resultType the converted type of the resultant object, using the context's type converter
   * @return the result of evaluating the expression
   * @throws MethodFailedException   if the expression called a method which failed
   * @throws NoSuchPropertyException if the expression referred to a nonexistent property
   * @throws InappropriateExpressionException
   *                                 if the expression can't be used in this context
   * @throws OgnlException           if there is a pathological environmental problem
   */
  public static Object getValue(Object tree, Map context, Object root, Class resultType) throws OgnlException {
    Object result;
    OgnlContext ognlContext = (OgnlContext) addDefaultContext(root, context);

    result = ((Node) tree).getValue(ognlContext, root);
    if (resultType != null) {
      result = getTypeConverter(context).convertValue(context, root, null, null, result, resultType);
    }
    return result;
  }

  /**
   * Evaluates the given OGNL expression to extract a value from the given root
   * object in a given context
   *
   * @param expression the OGNL expression to be parsed
   * @param context    the naming context for the evaluation
   * @param root       the root object for the OGNL expression
   * @return the result of evaluating the expression
   * @throws MethodFailedException   if the expression called a method which failed
   * @throws NoSuchPropertyException if the expression referred to a nonexistent property
   * @throws InappropriateExpressionException
   *                                 if the expression can't be used in this context
   * @throws OgnlException           if there is a pathological environmental problem
   * @see #parseExpression(String)
   * @see #getValue(Object,Object)
   */
  public static Object getValue(String expression, Map context, Object root) throws OgnlException {
    return getValue(expression, context, root, null);
  }

  /**
   * Evaluates the given OGNL expression to extract a value from the given root
   * object in a given context
   *
   * @param expression the OGNL expression to be parsed
   * @param context    the naming context for the evaluation
   * @param root       the root object for the OGNL expression
   * @param resultType the converted type of the resultant object, using the context's type converter
   * @return the result of evaluating the expression
   * @throws MethodFailedException   if the expression called a method which failed
   * @throws NoSuchPropertyException if the expression referred to a nonexistent property
   * @throws InappropriateExpressionException
   *                                 if the expression can't be used in this context
   * @throws OgnlException           if there is a pathological environmental problem
   * @see #parseExpression(String)
   * @see #getValue(Object,Object)
   */
  public static Object getValue(String expression, Map context, Object root, Class resultType) throws OgnlException {
    return getValue(parseExpression(expression), context, root, resultType);
  }

  /**
   * Evaluates the given OGNL expression tree to extract a value from the given root
   * object.
   *
   * @param tree the OGNL expression tree to evaluate, as returned by parseExpression()
   * @param root the root object for the OGNL expression
   * @return the result of evaluating the expression
   * @throws MethodFailedException   if the expression called a method which failed
   * @throws NoSuchPropertyException if the expression referred to a nonexistent property
   * @throws InappropriateExpressionException
   *                                 if the expression can't be used in this context
   * @throws OgnlException           if there is a pathological environmental problem
   */
  public static Object getValue(Object tree, Object root) throws OgnlException {
    return getValue(tree, root, null);
  }

  /**
   * Evaluates the given OGNL expression tree to extract a value from the given root
   * object.
   *
   * @param tree       the OGNL expression tree to evaluate, as returned by parseExpression()
   * @param root       the root object for the OGNL expression
   * @param resultType the converted type of the resultant object, using the context's type converter
   * @return the result of evaluating the expression
   * @throws MethodFailedException   if the expression called a method which failed
   * @throws NoSuchPropertyException if the expression referred to a nonexistent property
   * @throws InappropriateExpressionException
   *                                 if the expression can't be used in this context
   * @throws OgnlException           if there is a pathological environmental problem
   */
  public static Object getValue(Object tree, Object root, Class resultType) throws OgnlException {
    return getValue(tree, createDefaultContext(root), root, resultType);
  }

  /**
   * Convenience method that combines calls to <code> parseExpression </code> and
   * <code> getValue</code>.
   *
   * @param expression the OGNL expression to be parsed
   * @param root       the root object for the OGNL expression
   * @return the result of evaluating the expression
   * @throws ExpressionSyntaxException if the expression is malformed
   * @throws MethodFailedException     if the expression called a method which failed
   * @throws NoSuchPropertyException   if the expression referred to a nonexistent property
   * @throws InappropriateExpressionException
   *                                   if the expression can't be used in this context
   * @throws OgnlException             if there is a pathological environmental problem
   * @see #parseExpression(String)
   * @see #getValue(Object,Object)
   */
  public static Object getValue(String expression, Object root) throws OgnlException {
    return getValue(expression, root, null);
  }

  /**
   * Convenience method that combines calls to <code> parseExpression </code> and
   * <code> getValue</code>.
   *
   * @param expression the OGNL expression to be parsed
   * @param root       the root object for the OGNL expression
   * @param resultType the converted type of the resultant object, using the context's type converter
   * @return the result of evaluating the expression
   * @throws ExpressionSyntaxException if the expression is malformed
   * @throws MethodFailedException     if the expression called a method which failed
   * @throws NoSuchPropertyException   if the expression referred to a nonexistent property
   * @throws InappropriateExpressionException
   *                                   if the expression can't be used in this context
   * @throws OgnlException             if there is a pathological environmental problem
   * @see #parseExpression(String)
   * @see #getValue(Object,Object)
   */
  public static Object getValue(String expression, Object root, Class resultType) throws OgnlException {
    return getValue(parseExpression(expression), root, resultType);
  }

  /**
   * Evaluates the given OGNL expression tree to insert a value into the object graph
   * rooted at the given root object.  The default context is set for the given
   * context and root via <CODE>addDefaultContext()</CODE>.
   *
   * @param tree    the OGNL expression tree to evaluate, as returned by parseExpression()
   * @param context the naming context for the evaluation
   * @param root    the root object for the OGNL expression
   * @param value   the value to insert into the object graph
   * @throws MethodFailedException   if the expression called a method which failed
   * @throws NoSuchPropertyException if the expression referred to a nonexistent property
   * @throws InappropriateExpressionException
   *                                 if the expression can't be used in this context
   * @throws OgnlException           if there is a pathological environmental problem
   */
  public static void setValue(Object tree, Map context, Object root, Object value) throws OgnlException {
    OgnlContext ognlContext = (OgnlContext) addDefaultContext(root, context);
    Node n = (Node) tree;

    n.setValue(ognlContext, root, value);
  }

  /**
   * Evaluates the given OGNL expression to insert a value into the object graph
   * rooted at the given root object given the context.
   *
   * @param expression the OGNL expression to be parsed
   * @param root       the root object for the OGNL expression
   * @param context    the naming context for the evaluation
   * @param value      the value to insert into the object graph
   * @throws MethodFailedException   if the expression called a method which failed
   * @throws NoSuchPropertyException if the expression referred to a nonexistent property
   * @throws InappropriateExpressionException
   *                                 if the expression can't be used in this context
   * @throws OgnlException           if there is a pathological environmental problem
   */
  public static void setValue(String expression, Map context, Object root, Object value) throws OgnlException {
    setValue(parseExpression(expression), context, root, value);
  }

  /**
   * Evaluates the given OGNL expression tree to insert a value into the object graph
   * rooted at the given root object.
   *
   * @param tree  the OGNL expression tree to evaluate, as returned by parseExpression()
   * @param root  the root object for the OGNL expression
   * @param value the value to insert into the object graph
   * @throws MethodFailedException   if the expression called a method which failed
   * @throws NoSuchPropertyException if the expression referred to a nonexistent property
   * @throws InappropriateExpressionException
   *                                 if the expression can't be used in this context
   * @throws OgnlException           if there is a pathological environmental problem
   */
  public static void setValue(Object tree, Object root, Object value) throws OgnlException {
    setValue(tree, createDefaultContext(root), root, value);
  }

  /**
   * Convenience method that combines calls to <code> parseExpression </code> and
   * <code> setValue</code>.
   *
   * @param expression the OGNL expression to be parsed
   * @param root       the root object for the OGNL expression
   * @param value      the value to insert into the object graph
   * @throws ExpressionSyntaxException if the expression is malformed
   * @throws MethodFailedException     if the expression called a method which failed
   * @throws NoSuchPropertyException   if the expression referred to a nonexistent property
   * @throws InappropriateExpressionException
   *                                   if the expression can't be used in this context
   * @throws OgnlException             if there is a pathological environmental problem
   * @see #parseExpression(String)
   * @see #setValue(Object,Object,Object)
   */
  public static void setValue(String expression, Object root, Object value) throws OgnlException {
    setValue(parseExpression(expression), root, value);
  }

  public static boolean isConstant(Object tree, Map context) throws OgnlException {
    return ((SimpleNode) tree).isConstant((OgnlContext) addDefaultContext(null, context));
  }

  public static boolean isConstant(String expression, Map context) throws OgnlException {
    return isConstant(parseExpression(expression), context);
  }

  public static boolean isConstant(Object tree) throws OgnlException {
    return isConstant(tree, createDefaultContext(null));
  }

  public static boolean isConstant(String expression) throws OgnlException {
    return isConstant(parseExpression(expression), createDefaultContext(null));
  }

  public static boolean isSimpleProperty(Object tree, Map context) throws OgnlException {
    return ((SimpleNode) tree).isSimpleProperty((OgnlContext) addDefaultContext(null, context));
  }

  public static boolean isSimpleProperty(String expression, Map context) throws OgnlException {
    return isSimpleProperty(parseExpression(expression), context);
  }

  public static boolean isSimpleProperty(Object tree) throws OgnlException {
    return isSimpleProperty(tree, createDefaultContext(null));
  }

  public static boolean isSimpleProperty(String expression) throws OgnlException {
    return isSimpleProperty(parseExpression(expression), createDefaultContext(null));
  }

  public static boolean isSimpleNavigationChain(Object tree, Map context) throws OgnlException {
    return ((SimpleNode) tree).isSimpleNavigationChain((OgnlContext) addDefaultContext(null, context));
  }

  public static boolean isSimpleNavigationChain(String expression, Map context) throws OgnlException {
    return isSimpleNavigationChain(parseExpression(expression), context);
  }

  public static boolean isSimpleNavigationChain(Object tree) throws OgnlException {
    return isSimpleNavigationChain(tree, createDefaultContext(null));
  }

  public static boolean isSimpleNavigationChain(String expression) throws OgnlException {
    return isSimpleNavigationChain(parseExpression(expression), createDefaultContext(null));
  }

  /**
   * You can't make one of these.
   */
  private Ognl() {
  }
}
