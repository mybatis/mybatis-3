/**
 *    Copyright 2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.lombok.javac.handlers;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.core.ImportList;
import lombok.core.TypeLibrary;
import lombok.core.TypeResolver;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;

import org.mangosdk.spi.ProviderFor;

import org.apache.ibatis.annotations.LombokParam;
import org.apache.ibatis.annotations.Param;

/**
 * Created by Liu DongMiao &lt;liudongmiao@gmail.com&gt; on 2016/06/06.
 *
 * @author thom
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleLombokParam extends JavacAnnotationHandler<LombokParam> {

    private static final String PARAM = Param.class.getName();

    @Override
    public void handle(AnnotationValues<LombokParam> annotation, JCTree.JCAnnotation ast, JavacNode annotationNode) {
        handle(annotationNode);
    }

    private static void handle(JavacNode annotationNode) {
        JavacNode node = annotationNode.up();
        if (node != null) {
            for (JavacNode child : node.down()) {
                if (AST.Kind.METHOD.equals(child.getKind())) {
                    handleMethod(child);
                }
            }
        }
    }
    private static void handleMethod(JavacNode method) {
        JavacTreeMaker maker = method.getTreeMaker();
        JCTree.JCExpression type = JavacHandlerUtil.chainDotsString(method, PARAM);
        for (JCTree.JCVariableDecl param : ((JCTree.JCMethodDecl) method.get()).params) {
            if (!hasParam(method, param.mods.annotations)) {
                List<JCTree.JCExpression> args = List.<JCTree.JCExpression>of(maker.Literal(param.name.toString()));
                JCTree.JCAnnotation annotation = maker.Annotation(type, args);
                param.mods.annotations = param.mods.annotations.append(annotation);
            }
        }
    }

    private static boolean hasParam(JavacNode method, List<JCTree.JCAnnotation> annotations) {
        ImportList importList = method.getImportList();
        TypeLibrary typeLibrary = TypeLibrary.createLibraryForSingleType(PARAM);
        TypeResolver typeResolver = new TypeResolver(importList);
        for (JCTree.JCAnnotation annotation : annotations) {
            String type = annotation.annotationType.toString();
            String fqn = typeResolver.typeRefToFullyQualifiedName(method, typeLibrary, type);
            if (PARAM.equals(fqn)) {
                return true;
            }
        }
        return false;
    }

}

