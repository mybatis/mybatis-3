title: MyBatis 3 | Premiers pas
author: Clinton Begin, Dridi Boukelmoune

# Premiers pas

## Installation

Pour utiliser MyBatis, ajoutez le fichier [mybatis-x.x.x.jar](https://github.com/mybatis/mybatis-3/releases) au classpath.

Si vous utilisez Maven, ajoutez la dépendance suivante à votre pom.xml :

```xml
<dependency>
  <groupId>org.mybatis</groupId>
  <artifactId>mybatis</artifactId>
  <version>x.x.x</version>
</dependency>
```

## Créer une SqlSessionFactory à partir d'un fichier XML

Chaque application MyBatis tourne autour d'une instance de SqlSessionFactory. Une instance de SqlSessionFactory peut être obtenue à l'aide du SqlSessionFactoryBuilder. SqlSessionFactoryBuilder peut créer une instance de SqlSessionFactory à partir d'un fichier XML de configuration, ou à partir d'une instance personnalisée de la classe Configuration.

Créer une instance de SqlSessionFactory à partir d'un fichier XML est très simple. Il est recommandé d'utiliser un fichier présent dans le classpath, mais on peut utiliser n'importe quelle instance d'InputStream, y compris à partir d'un chemin vers un fichier ou d'une URL de type `file://`. MyBatis contient une classe utilitaire, Resources, qui contient plusieurs méthodes facilitant le chargement de ressources du classpath ou d'ailleurs.

```java
String resource = "org/mybatis/example/mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
```

Le fichier de configuration XML contient les réglages pour le noyau de MyBatis, y compris une DataSource pour obtenir les connexions vers la base de données, ainsi qu'un TransactionManager pour contrôler les transactions et déterminer leur périmètre. Le détail complet du fichier de configuration XML est décrit plus tard dans le document, mais voici un exemple simple:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "https://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
  <properties resource="org/mybatis/example/db.properties"/>
  <environments default="development">
    <environment id="development">
      <transactionManager type="JDBC"/>
      <dataSource type="POOLED">
        <property name="driver" value="${driver}"/>
        <property name="url" value="${url}"/>
        <property name="username" value="${username}"/>
        <property name="password" value="${password}"/>
      </dataSource>
    </environment>
  </environments>
  <mappers>
    <mapper resource="org/mybatis/example/BlogMapper.xml"/>
  </mappers>
</configuration>
```

Placez `org/mybatis/example/db.properties` dans le classpath et définissez-y `driver`, `url`, `username` et `password` pour votre base de données.

Le fichier de configuration XML est loin de se limiter à cela, mais l'exemple ci-dessus présente les points les plus critiques. La déclaration XML est facultative ; la déclaration DOCTYPE indique la DTD utilisée pour valider le document XML. L'élément environment contient la configuration du gestionnaire de transactions et du pool de connexions. L'élément mappers contient une liste de mappers : fichiers XML et/ou interfaces Java annotées contenant les requêtes SQL et les définitions de mapping.

## Créer une SqlSessionFactory sans XML

Il est possible de créer la configuration directement en Java, plutôt qu'en XML. MyBatis fournit une classe Configuration qui propose les mêmes options de configuration qu'avec un fichier XML.

```java
DataSource dataSource = BlogDataSourceFactory.getBlogDataSource();
TransactionFactory transactionFactory = new JdbcTransactionFactory();
Environment environment = new Environment("development", transactionFactory, dataSource);
Configuration configuration = new Configuration(environment);
configuration.addMapper(BlogMapper.class);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
```

On remarque dans ce cas l'ajout d'une classe mapper. Il est possible de configurer MyBatis à l'aide d'annotations, y compris pour mapper les résultats de jointures imbriquées. Pour certains mappings complexes, XML peut néanmoins être plus facile à lire. Lorsqu'un mapper est enregistré, MyBatis cherche aussi un fichier XML associé (dans le cas présent, BlogMapper.xml, à partir du classpath et du nom de BlogMapper.class). Le sujet sera approfondi plus tard.

## Acquérir une SqlSession à partir d'une SqlSessionFactory

Maintenant que nous avons une SqlSessionFactory, comme le nom le suggère, nous pouvons créer une instance de SqlSession. Elle contient la totalité des méthodes nécessaires à l'exécution de requêtes SQL. On peut directement exécuter une requête d'un mapper à partir d'une SqlSession. Par exemple :

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  Blog blog = session.selectOne("org.mybatis.example.BlogMapper.selectBlog", 101);
}
```

Même si cette approche fonctionne, et qu'elle est bien connue des vétérans de MyBatis, il existe une approche plus propre. En utilisant une interface (e.g. BlogMapper.class) qui décrit correctement les paramètres et la valeur de retour pour une requête particulière, on peut désormais exécuter du code plus propre et fortement typé et éviter les erreurs classiques de transtypage.

Par exemple:

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  BlogMapper mapper = session.getMapper(BlogMapper.class);
  Blog blog = mapper.selectBlog(101);
}
```

Explorons maintenant ce qui est exécuté ici.

## À la découverte des requêtes SQL mappées

Arrivé ici vous vous demandez peut-être ce qui est exécuté par la SqlSession ou le Mapper. Le sujet des requêtes SQL mappées est vaste, et il sera prépondérant dans la majorité de cette documentation. Mais pour vous donner une idée de ce qui est exécuté, voici quelques exemples.

Dans chacun des exemples ci-dessus, les requêtes auraient pu être définies aussi bien en XML qu'avec des annotations. Jetons d'abord un coup d'œil à l'XML. La totalité des fonctionnalités de MyBatis est accessible avec le DSL basé sur XML qui a fait le succès de MyBatis au travers des années. Si vous avez utilisé MyBatis par le passé, vous serez familier du concept, mais le mapping XML a connu de nombreuses améliorations qui seront mises en lumières. Voici un exemple de mapping XML compatible avec les exemples précédents d'utilisation de SqlSession.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.mybatis.example.BlogMapper">
  <select id="selectBlog" resultType="org.mybatis.example.Blog">
    select * from Blog where id = #{id}
  </select>
</mapper>
```

Cela peut paraître beaucoup d'overhead pour un exemple aussi simple, mais c'est en fait très léger. On peut définir autant de requêtes dans un même mapper XML, si bien que les entêtes XML paraissent insignifiants. Le reste du fichier parle de lui-même. On définit un nom pour la requête “selectBlog”, dans le namespace “org.mybatis.example.BlogMapper”, ce qui nous permettrait de l'exécuter par son nom canonique (fully qualified name) “org.mybatis.example.BlogMapper.selectBlog”, comme nous l'avions fait plus haut :

```java
Blog blog = session.selectOne("org.mybatis.example.BlogMapper.selectBlog", 101);
```

C'est très similaire à un appel de méthode Java à partir du nom canonique (sans import) de la classe, et il y a une raison pour ça. Ce nom peut directement être associé à une classe “Mapper” du même nom que le namespace, avec un nom de méthode qui correspond à celui de la requête, le paramètre, et le type de retour correspondant. Cela permet d'appeler facilement les méthodes de l'interface BlogMapper vue précédemment, mais on peut aussi l'utiliser comme dans l'exemple suivant:

```java
BlogMapper mapper = session.getMapper(BlogMapper.class);
Blog blog = mapper.selectBlog(101);
```

La seconde approche a de nombreux avantages. Premièrement, on ne dépend pas d'une chaîne de caractère mais d'un type, c'est plus sûr. Deuxièmement, dans un IDE supportant l'auto-completion, cela facilite la recherche des requêtes à exécuter.

---

<span class="label important">NOTE</span> **A propos des namespaces.**

Les **Namespaces** étaient optionnels dans les précédentes versions de MyBatis, ce qui n'aidait pas en plus d'être déroutant. Ils sont maintenant obligatoires et ont un but au-delà de la simple isolation des requêtes (noms canoniques).

On constate que les namespaces permettent de faire le lien avec les interfaces, et même si vous ne pensez pas les utiliser aujourd'hui, il est vivement recommandé de suivre ces pratiques. En déclarant un namespace, et en utilisant l'interface Java correspondante, à long terme MyBatis devient plus simple à utiliser et cela rend votre code plus fiable.

**Résolution des noms:** Afin de réduire la quantité de code à produire, MyBatis utilise les règles suivantes de résolution des éléments de configuration nommés, dont les requêtes, result maps, caches etc.

- Les noms canoniques (e.g. “com.mypackage.MyMapper.selectAllThings”) sont recherchés tels quels et utilisés lorsqu'ils existent.
- Les noms courts (e.g. “selectAllThings”) peuvent être utilisés pour référencer les symboles en l'absence d'ambiguïté. Cependant, s'il en existe au moins deux (e.g. “com.foo.selectAllThings et com.bar.selectAllThings”), une exception sera levée expliquant que le nom est ambigu et doit donc être canonique, complet.

---

Une dernière possibilité s'offre aux mappers comme BlogMapper. Leurs requêtes n'ont pas nécessairement besoin d'un mapping XML. Il est possible de passer par des annotations Java. On peut par exemple remplacer le mapping XML par:

```java
package org.mybatis.example;
public interface BlogMapper {
  @Select("SELECT * FROM blog WHERE id = #{id}")
  Blog selectBlog(int id);
}
```

Les annotations génèrent moins d'overhead pour les requêtes simples, elles sont cependant limitées et plus fouillies pour des requêtes plus complexes. De fait, pour tout ce qui est compliqué, XML est à privilégier.

Il sera de votre ressort de déterminer quelle approche vous convient le mieux, et quelle importance vous accordez à la cohérence dans la définition de vos requêtes SQL. Ceci-dit, MyBatis ne vous enferme pas dans une approche unique. Il est facile de migrer des annotations vers l'XML et vice versa.

## Périmètre et Cycle de vie

Il est très important de comprendre la portée des différentes classes qui ont été abordées jusqu'ici. Utilisées incorrectement, elles peuvent poser de sévères problèmes de concurrence.

---

<span class="label important">NOTE</span> **Cycle de vie au sein d'un framework d'injection de dépendance**

Les frameworks d'injection de dépendance (ou IoC, inversion de contrôle) peuvent créer des SqlSession et des mappers transactionnels, thread safe, et les injecter directement dans vos beans si bien que vous n'avez plus à vous soucier de leur cycle de vie. Vous serez probablement intéressé par les sous-projets MyBatis-Spring et MyBatis-Guice pour apprendre plus sur l'injection de dépendances avec MyBatis.

---

#### SqlSessionFactoryBuilder

Cette classe peut être instanciée, utilisée puis jetée aux oubliettes. Il n'y a aucun intérêt à la conserver une fois qu'on a créé la SqlSessionFactory. De fait, le mieux est de limiter la portée des SqlSessionFactoryBuilder à une méthode (i.e. variable locale). On peut réutiliser une instance pour construire plusieurs SqlSessionFactory, mais il est préférable de ne pas garder de SqlSessionFactoryBuilder pour permettre la libération des ressources qui ont servi à _parser_ les fichiers XML.

#### SqlSessionFactory

Une fois créée, la SqlSessionFactory devrait exister pendant toute l'exécution de l'application. Il n'y a pas ou peu de raisons de disposer d'une instance ou de la recréer. La bonne pratique est de ne pas créer de multiples instances pendant l'exécution d'une application. Par conséquent, la portée la plus adaptée est celle de l'application. Cela peut être fait de différentes façons. La plus simple est d'utiliser le pattern du Singleton ou du Static Singleton.

#### SqlSession

Chaque thread devrait avoir sa propre instance de SqlSession. Les instances de SqlSession n'ont pas été pensées pour être partagées et ne sont pas _thread safe_. De fait, les portées les plus adaptées sont méthode (scope method) ou requête (scope request). Ne conservez jamais de SqlSession dans un champ statique ni même dans un champ d'une classe (NDT sauf si la classe se trouve dans un scope de type request). Ne gardez pas non plus de référence de SqlSession dans un framework gérant ses propres cycles de vie (i.e. managed scope), comme par exemple HttpSession de l'API Servlet. Si vous utilisez un framework WEB, SqlSession devrait avoir la même portée qu'une requête HTTP (NDT une requête est généralement rattachée à un unique thread). En d'autres termes, lorsque vous recevez une requête HTTP, vous pouvez ouvrir une SqlSession, et lors de la réponse HTTP, la refermer. Fermer la session est très important. Utilisez try-with-resources pour vous assurer que la session est bien fermée. Le pattern classique à suivre pour assurer la fermeture de la connexion :

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  // do work
}
```

En utilisant ce pattern de façon systématique, votre code assurera la fermeture de toutes les ressources de base de données.

#### Mappers

Les mappers sont des interfaces qui sont liées aux requêtes déclarées. Les instances de mappers sont obtenues à partir d'une SqlSession. Donc, techniquement, leur portée ne peut pas excéder celle de la SqlSession dont ils sont issus. Cependant, il convient de limiter la portée d'un mapper à une méthode. C'est à dire, un mapper devrait être créé, utilisé et libéré au sein d'une même méthode. Ils n'ont pas explicitement besoin d'être fermés. Même s'il n'est pas gênant de les conserver pendant toute une requête (e.g. HTTP), à l'image de leur SqlSession, vous prenez le risque d'être débordé face à la consommation de ressources induite. Le plus simple, c'est de limiter la portée d'un mapper à une méthode. Voici un exemple de mise en pratique (la portée du mapper est le bloc try):

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  BlogMapper mapper = session.getMapper(BlogMapper.class);
  // do work
}

```
