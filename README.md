# TapirExamples

There are two types of server endpoints
- public( with empty security logic)
- default(the apply method Creates a server endpoint, with the given security and main logic functions, which match the shape defined by `endpoint`)



```scala
  private def emptySecurityLogic[E, F[_]]: MonadError[F] => Unit => F[Either[E, Unit]] = implicit m =>
    _ => (Right(()): Either[E, Unit]).unit
```


```scala
/** An [[Endpoint]] together with functions implementing the endpoint's security and main logic.
  *
  * @tparam R
  *   Requirements: The capabilities that are required by this endpoint's inputs/outputs. `Any`, if no requirements.
  * @tparam F
  *   The effect type constructor used in the provided server logic.
  */
abstract class ServerEndpoint[-R, F[_]] extends EndpointInfoOps[R] with EndpointMetaOps {
    outer =>

  /** Security input parameter types (abbreviated as `A`). */
  type SECURITY_INPUT

  /** The type of the value returned by the security logic, e.g. a user (abbreviated as `U`). */
  type PRINCIPAL

  /** Input parameter types (abbreviated as `I`). */
  type INPUT

  /** Error output parameter types (abbreviated as `E`). */
  type ERROR_OUTPUT

  /** Output parameter types (abbreviated as `O`). */
  type OUTPUT

  def endpoint: Endpoint[SECURITY_INPUT, INPUT, ERROR_OUTPUT, OUTPUT, R]
  def securityLogic: MonadError[F] => SECURITY_INPUT => F[Either[ERROR_OUTPUT, PRINCIPAL]]
  def logic: MonadError[F] => PRINCIPAL => INPUT => F[Either[ERROR_OUTPUT, OUTPUT]]

  override type ThisType[-_R] = ServerEndpoint.Full[SECURITY_INPUT, PRINCIPAL, INPUT, ERROR_OUTPUT, OUTPUT, _R, F]
  override def securityInput: EndpointInput[SECURITY_INPUT] = endpoint.securityInput
  override def input: EndpointInput[INPUT] = endpoint.input
  override def errorOutput: EndpointOutput[ERROR_OUTPUT] = endpoint.errorOutput
  override def output: EndpointOutput[OUTPUT] = endpoint.output
  override def info: EndpointInfo = endpoint.info
}
```


```scala
/** A description of an endpoint with the given inputs & outputs. The inputs are divided into two parts: security (`A`) and regular inputs
  * (`I`). There are also two kinds of outputs: error outputs (`E`) and regular outputs (`O`).
  *
  * In case there are no security inputs, the [[PublicEndpoint]] alias can be used, which omits the `A` parameter type.
  *
  * An endpoint can be interpreted as a server, client or documentation. The endpoint requires that server/client interpreters meet the
  * capabilities specified by `R` (if any).
  *
  * When interpreting an endpoint as a server, the inputs are decoded and the security logic is run first, before decoding the body in the
  * regular inputs. This allows short-circuiting further processing in case security checks fail. Server logic can be provided using
  * [[EndpointServerLogicOps.serverSecurityLogic]] variants for secure endpoints, and [[EndpointServerLogicOps.serverLogic]] variants for
  * public endpoints.
  *
  * A concise description of an endpoint can be generated using the [[EndpointMetaOps.show]] method.
  *
  * @tparam SECURITY_INPUT
  *   Security input parameter types, abbreviated as `A`.
  * @tparam INPUT
  *   Input parameter types, abbreviated as `I`.
  * @tparam ERROR_OUTPUT
  *   Error output parameter types, abbreviated as `E`.
  * @tparam OUTPUT
  *   Output parameter types, abbreviated as `O`.
  * @tparam R
  *   The capabilities that are required by this endpoint's inputs/outputs. This might be `Any` (no requirements),
  *   [[sttp.capabilities.Effect]] (the interpreter must support the given effect type), [[sttp.capabilities.Streams]] (the ability to send
  *   and receive streaming bodies) or [[sttp.capabilities.WebSockets]] (the ability to handle websocket requests).
  */
case class Endpoint[SECURITY_INPUT, INPUT, ERROR_OUTPUT, OUTPUT, -R](
    securityInput: EndpointInput[SECURITY_INPUT],
    input: EndpointInput[INPUT],
    errorOutput: EndpointOutput[ERROR_OUTPUT],
    output: EndpointOutput[OUTPUT],
    info: EndpointInfo
)
```


`serverLogic` produces `ServerEndpoints`


```scala
trait EndpointServerLogicOps[A, I, E, O, -R] { outer: Endpoint[A, I, E, O, R] =>

  /** Combine this public endpoint description with a function, which implements the server-side logic. The logic returns a result, which is
    * either an error or a successful output, wrapped in an effect type `F`. For secure endpoints, use [[serverSecurityLogic]].
    *
    * A server endpoint can be passed to a server interpreter. Each server interpreter supports effects of a specific type(s).
    *
    * Both the endpoint and logic function are considered complete, and cannot be later extended through the returned [[ServerEndpoint]]
    * value (except for endpoint meta-data). Secure endpoints allow providing the security logic before all the inputs and outputs are
    * specified.
    */
  def serverLogic[F[_]](f: I => F[Either[E, O]])(implicit aIsUnit: A =:= Unit): ServerEndpoint.Full[Unit, Unit, I, E, O, R, F] = {
    import sttp.monad.syntax._
    ServerEndpoint.public(this.asInstanceOf[Endpoint[Unit, I, E, O, R]], implicit m => i => f(i).map(x => x))
  }

    def serverSecurityLogic[PRINCIPAL, F[_]](f: A => F[Either[E, PRINCIPAL]]): PartialServerEndpoint[A, PRINCIPAL, I, E, O, R, F] =
    PartialServerEndpoint(this, _ => f)


}
```



```scala
/** An endpoint with the security logic provided, and the main logic yet unspecified. See [[Endpoint.serverSecurityLogic]].
  *
  * The provided security part of the server logic transforms inputs of type `SECURITY_INPUT`, either to an error of type `ERROR_OUTPUT`, or
  * value of type `PRINCIPAL`.
  *
  * The part of the server logic which is not provided, will have to transform both `PRINCIPAL` and the rest of the input `INPUT` either
  * into an error, or a value of type `OUTPUT`.
  *
  * Inputs/outputs can be added to partial endpoints as to regular endpoints. The shape of the error outputs can be adjusted in a limited
  * way, by adding new error output variants, similar as if they were defined using [[Tapir.oneOf]]; the variants and the existing error
  * outputs should usually have a common supertype (other than `Any`). Hence, it's possible to create a base, secured input, and then
  * specialise it with inputs, outputs and logic as needed.
  *
  * @tparam SECURITY_INPUT
  *   Security input parameter types, which the security logic accepts and returns a `PRINCIPAL` or an error `ERROR_OUTPUT`.
  * @tparam PRINCIPAL
  *   The type of the value returned by the security logic.
  * @tparam INPUT
  *   Input parameter types.
  * @tparam ERROR_OUTPUT
  *   Error output parameter types.
  * @tparam OUTPUT
  *   Output parameter types.
  * @tparam R
  *   The capabilities that are required by this endpoint's inputs/outputs. `Any`, if no requirements.
  * @tparam F
  *   The effect type used in the provided partial server logic.
  */
case class PartialServerEndpoint[SECURITY_INPUT, PRINCIPAL, INPUT, ERROR_OUTPUT, OUTPUT, -R, F[_]](
    endpoint: Endpoint[SECURITY_INPUT, INPUT, ERROR_OUTPUT, OUTPUT, R],
    securityLogic: MonadError[F] => SECURITY_INPUT => F[Either[ERROR_OUTPUT, PRINCIPAL]]) extends EndpointInputsOps[SECURITY_INPUT, INPUT, ERROR_OUTPUT, OUTPUT, R]{

          def serverLogic(
      f: PRINCIPAL => INPUT => F[Either[ERROR_OUTPUT, OUTPUT]]
  ): ServerEndpoint.Full[SECURITY_INPUT, PRINCIPAL, INPUT, ERROR_OUTPUT, OUTPUT, R, F] = ServerEndpoint(endpoint, securityLogic, _ => f)
    }

```
An endpoint is represented as a value of type `Endpoint[A, I, E, O, R]`, where:

    A is the type of security input parameters

    I is the type of input parameters

    E is the type of error-output parameters

    O is the type of output parameters

    R are the capabilities that are required by this endpoint’s inputs/outputs, such as support for websockets or a particular non-blocking streaming implementation. Any, if there are no such requirements.

Input/output parameters (A, I, E and O) can be:

    of type Unit, when there’s no input/output

    a single type

    a tuple of types

Hence, an empty, initial endpoint, with no inputs and no outputs, from which all other endpoints are derived has the type:

```scala
import sttp.tapir._

val endpoint: Endpoint[Unit, Unit, Unit, Unit, Any] = ???
```

For endpoints which have no security inputs, a type alias is provided which fixes A to Unit:

```scala
import sttp.tapir._

type PublicEndpoint[I, E, O, -R] = Endpoint[Unit, I, E, O, R]
```
When interpreted as a server, these endpoints are decoded first, and are used to run the security logic, which should authenticate (or reject) the request. The result of the security logic is typically a value, such as an authenticated User instance. What’s important is that this takes place before body decoding is done; hence, the body is read and parsed only if the request passed authentication

The last security-related feature that has been added is the possibility to hide endpoints that need authentication. By default, if an endpoint needs e.g. an API key in the Authorization header, and such a value is missing from the request, the tapir server interpreter will return a 401 Unauthorized with the appropriate WWW-Authenticate header. This is not always desired; you might want to return a 404 Not Found instead, hiding information that such endpoints exist at all. 




[security-tapir](https://softwaremill.com/security-improvements-in-tapir-0-19/)




## Git Worktree
allows you to work with multiple working directories attached to the same repository.
Each worktree operates independently, allowing you to switch branches and make changes without affecting other worktrees or the main repository.Remember that worktrees share the same repository data, so any commits or changes made in one worktree will be visible in all other worktrees and the main repository.
Isolation: Each worktree has its own working directory and index, allowing you to work on different branches or changes simultaneously without interference.

Efficiency: Worktrees are lightweight and do not require copying the entire repository, saving disk space and time compared to cloning a new repository for each task.

Shared Repository Data: While worktrees provide isolation for your working directory and index, they share the same repository data, including commits, branches, tags, and configuration.

Branch Management: You can create new branches within a worktree, switch branches, merge changes, and perform other branch-related operations independently in each worktree.

Visibility: Worktrees are visible to Git commands as if they were regular directories within the repository. However, they are stored in a separate location from the main repository (by default, in a directory named .git/worktrees).

Cleaning Up: It's essential to manage your worktrees properly to avoid cluttering your system. You can use the git worktree prune command to remove stale worktrees (ones that have been deleted or moved) from the repository's metadata.

Use Cases: Git worktrees are particularly useful for scenarios like working on multiple features simultaneously, testing changes in different environments, maintaining long-lived branches for specific tasks, or experimenting with new ideas without affecting the main working directory.

"working tree" are the actual files you see in the folder when you checkout a branch (excluding the special .git folder). When you checkout a different branch, git updates all the files on disk to match the files in the new branch. You can have many branches in your repository, but only one of these will be "checked out" as the working-tree so that you can work on it and make changes.

git worktree adds the concept of additional working trees. This means you can have two (or more) branches checked-out at once

# Creating a working tree from a new branch
git worktree has a handy -b option to both create a new branch and check it out in the new working tree

```bash
git worktree add ../app-example-2 origin/main -b bug-fix
Preparing worktree (new branch 'bug-fix')
Branch 'bug-fix' set up to track remote branch 'main' from 'origin'.
HEAD is now at 37ae55f Merge pull request #417 from some-natalie/main
```


This example creates a new branch, bug-fix from the origin/main branch, and then checks it out at ../app-example-2. It can be removed from the main working tree by running git worktree remove ../app-example-2




[schemas](https://disneystreaming.github.io/smithy4s/docs/design/schemas/)

## Schemas

For a Scala type called Foo, formulating a Schema[Foo] is equivalent to exhaustively capturing the information needed for the serialisation and deserialisation of Foo in any format (JSON, XML, ...). Indeed, for any Codec[_] construct provided by third-party libraries, it is possible to write a generic def compile(schema: Schema[A]): Codec[A] function that produces the Codec for A based on the information held by the Schema.


Describes the type `T`: its low-level representation, meta-data and validation rules.

## Structures
A structure, also referred to as product, or record, is a construct that groups several values together. Typically, it translates naturally to a case class.

Tapir allows you to define endpoints using a combination of types and functions. Schemas play a crucial role in defining these types, particularly in specifying the shape of the input and output data. Here's how Tapir uses schemas:

Defining Input and Output Data: Tapir lets you define the input and output data of your API endpoints using case classes, tuples, or primitive types. These data structures effectively act as schemas describing the format of the data being sent or received.

Automatic Conversion: Tapir can automatically convert between Scala types and various serialization formats such as JSON or XML. Schemas help Tapir understand how to serialize and deserialize data between these formats and Scala types.

Validation: Schemas can be used for input validation. Tapir allows you to define constraints on input data using schemas, ensuring that incoming data adheres to the expected format and structure.

Documentation: Schemas serve as documentation for your API endpoints. Tapir can generate documentation for your API based on the schemas you provide, making it easier for developers to understand how to interact with your API.

Error Handling: Schemas can also be used to define error responses in Tapir. By specifying the schema for error responses, you can ensure that error messages returned by your API endpoints adhere to a consistent format. This can simplify error handling on the client side and improve the overall robustness of your API.

OpenAPI Documentation: Tapir can automatically generate OpenAPI documentation for your Scala web APIs based on the endpoint descriptions you provide. Schemas are a crucial part of this documentation generation process, as they define the structure of request and response bodies, query parameters, path parameters, etc. Tapir leverages these schemas to produce accurate and comprehensive API documentation that developers can use to understand and interact with your API.

Testing: Schemas can be used in conjunction with property-based testing frameworks like ScalaCheck to automatically generate test data for your API endpoints. By defining schemas for input and output types, you can generate a wide range of test cases to ensure that your API behaves correctly under various conditions. This can help uncover edge cases and potential bugs in your API implementation.
Schema Generation: Tapir can also generate JSON Schemas or other schema formats from your Scala data types. These generated schemas can be useful for various purposes, such as interoperability with other systems, data validation outside of your API endpoints, or documentation generation in formats other than OpenAPI. Tapir's ability to automatically generate schemas from Scala types eliminates the need for manual schema definition, reducing duplication and ensuring consistency between your data models and API endpoints.

Schema-Based Code Generation: Tapir can be used in conjunction with code generation tools to generate Scala code based on your API schemas. This capability allows you to automate the generation of client libraries, server implementations, or other components that interact with your API. By using schemas as the source of truth for code generation, Tapir ensures that the generated code accurately reflects the structure and constraints of your API endpoints, reducing the risk of inconsistencies or errors in your codebase.

```scala

trait TapirJsonCirce {
  def jsonBody[T: Encoder: Decoder: Schema]: EndpointIO.Body[String, T] = stringBodyUtf8AnyFormat(circeCodec[T])

  def jsonBodyWithRaw[T: Encoder: Decoder: Schema]: EndpointIO.Body[String, (String, T)] = stringBodyUtf8AnyFormat(
    implicitly[JsonCodec[(String, T)]]
  )


  implicit val schemaForCirceJson: Schema[Json] = Schema.any
  implicit val schemaForCirceJsonObject: Schema[JsonObject] = Schema.anyObject[JsonObject].name(SName("io.circe.JsonObject"))
}

```


Codec instances are used as implicit values, and are looked up when defining endpoint inputs/outputs. Depending on a particular endpoint
   input/output, it might require a codec which uses a specific format, or a specific low-level value.

a codec is a component or library that provides the functionality to encode (serialize) and decode (deserialize) data between different representations or formats.

   ```scala
implicit val schemaForLanguageCode: Schema[LanguageCode] = Schema.string
implicit val schemaForProductName: Schema[ProductName] = Schema.string
   ```

### things we can do with Schemas
  - Encoding and decoding( separating the structure from how to encode the structure)
  - Migration
  -Validation

we can generate a Codec based on the Schema
we can generate binaryCodec, Json Codec based on the Schema

Using schemas, you can automate the generation of codecs for data types. By introspecting the structure of a schema, you can generate encoder and decoder implementations that serialize and deserialize data according to the schema's specifications

ZIO Schema allows us to create representations of our data types as values
Once we have a representation of our data types, we can use it to

    Serialize and deserialize our types
    Validate our types
    Transform our types
    Create instances of your types

We can then use one of the various codecs (or create our own) to serialize and deserialize your types.

Example of possible codecs are:

- CSV Codec
- JSON Codec (already available)
- Apache Avro Codec (in progress)
- Apache Thrift Codec (in progress)
- XML Codec
- YAML Codec
- Protobuf Codec (already available)
- QueryString Codec
- etc.

Example use cases that are possible:

- Serializing and deserializing JSON
- Serializing and deserializing XML
- Validating JSON
- Validating XML
- Transforming JSON
- Transforming XML
- Transforming JSON to XML
- Transforming XML to JSON
- Creating diffs from arbitrary data structures
- Creating migrations / evolutions e.g. of Events used in Event-Sourcing
- Transformation pipelines, e.g.
- Convert from protobuf to object, e.g. PersonDTO,
- Transform to another representation, e.g. Person,
- Validate
- Transform to JSON JsonObject
- Serialize to String


ZIO Json Encoder converts from case class to string while the decoder converts from string to case class

in ZIO Schema-json, the Schema  structure is used to encode or decode to json using internals of zio-json. it generates codecs without making use of macros



In http4s, circe encoder is used to convert from case class to Json and EntityEncoders take from Json to Bytes


the Decoders convert from  Json to case class but before that, EntityDecoders convert from bytes to Json


In tapir, 

```scala
  def jsonBody[T: Encoder: Decoder: Schema]: EndpointIO.Body[String, T] = stringBodyUtf8AnyFormat(circeCodec[T])
 implicit val schemaForCirceJson: Schema[Json] = Schema.any

  implicit def circeCodec[T: Encoder: Decoder: Schema]: JsonCodec[T] =
    sttp.tapir.Codec.json[T] { s =>
      io.circe.parser.decodeAccumulating[T](s) match {
        case Validated.Valid(v) => Value(v)
        case Validated.Invalid(circeFailures) =>
          val tapirJsonErrors = circeFailures.map {
            case ParsingFailure(msg, _) => JsonError(msg, path = List.empty)
            case failure: DecodingFailure =>
              val path = CursorOp.opsToPath(failure.history)
              val fields = path.split("\\.").toList.filter(_.nonEmpty).map(FieldName.apply)
              JsonError(failure.message, fields)
          }

          Error(
            original = s,
            error = JsonDecodeException(
              errors = tapirJsonErrors.toList,
              underlying = Errors(circeFailures)
            )
          )
      }
    } { t => jsonPrinter.print(t.asJson) }
```
is used to convert from Json to  string


```scala
trait CodecFormat {
  def mediaType: MediaType
}

object CodecFormat {
  case class Json() extends CodecFormat {
    override val mediaType: MediaType = MediaType.ApplicationJson
  }

  case class Grpc() extends CodecFormat {
    override val mediaType: MediaType = MediaType.unsafeApply(mainType = "application", subType = "grpc")
  }

  case class Xml() extends CodecFormat {
    override val mediaType: MediaType = MediaType.ApplicationXml
  }

  case class TextPlain() extends CodecFormat {
    override val mediaType: MediaType = MediaType.TextPlain
  }

  case class TextHtml() extends CodecFormat {
    override val mediaType: MediaType = MediaType.TextHtml
  }

  case class OctetStream() extends CodecFormat {
    override val mediaType: MediaType = MediaType.ApplicationOctetStream
  }

  case class XWwwFormUrlencoded() extends CodecFormat {
    override val mediaType: MediaType = MediaType.ApplicationXWwwFormUrlencoded
  }

  case class MultipartFormData() extends CodecFormat {
    override val mediaType: MediaType = MediaType.MultipartFormData
  }

  case class Zip() extends CodecFormat {
    override val mediaType: MediaType = MediaType.ApplicationZip
  }

  case class TextEventStream() extends CodecFormat {
    override val mediaType: MediaType = MediaType.TextEventStream
  }

  case class TextJavascript() extends CodecFormat {
    override val mediaType: MediaType = MediaType.TextJavascript
  }
}
  /*
  * @tparam L
  *   The type of the low-level value.
  * @tparam H
  *   The type of the high-level value.
  * @tparam CF
  *   The format of encoded values. Corresponds to the media type
  */ 
trait Codec[L, H, +CF <: CodecFormat] { outer =>
def rawDecode(l: L): DecodeResult[H]
  def encode(h: H): L
def schema: Schema[H]
def format: CF
/*
By providing both forward (f: H => HH) and backward (g: HH => H) transformations, Tapir ensures that you can round-trip values through the codec without losing information.
This is essential for maintaining data integrity and consistency, especially in scenarios where you need to encode and decode values repeatedly
*/
def map[HH](f: H => HH)(g: HH => H): Codec[L, HH, CF]= ???
}
object Codec{
    type PlainCodec[T] = Codec[String, T, CodecFormat.TextPlain]
  type JsonCodec[T] = Codec[String, T, CodecFormat.Json]
  type XmlCodec[T] = Codec[String, T, CodecFormat.Xml]

// identity
  def id[L, CF <: CodecFormat](f: CF, s: Schema[L]): Codec[L, L, CF] =
    new Codec[L, L, CF] {
      override def rawDecode(l: L): DecodeResult[L] = Value(l)
      override def encode(h: L): L = h
      override def schema: Schema[L] = s
      override def format: CF = f
    }
}

```

First we need to understand that a ZIO Schema is basically built-up from these three sealed traits: Record[R], Enum[A] and Sequence[Col, Elem], along with the case class Primitive[A]. Every other type is just a specialisation of one of these 
The core data type of ZIO Schema is a Schema[A] which is invariant in A by necessity, because a Schema allows us to derive operations that produce an A but also operations that consume an A and that imposes limitations on the types of transformation operators and composition operators that we can provide based on a Schema.

```scala
sealed trait Schema[A] { self =>
  def zip[B](that: Schema[B]): Schema[(A, B)]

  def transform[B](f: A => B, g: B => A): Schema[B]
}

```

To describe scalar data type A, we use the Primitive[A] data type which basically is a wrapper around StandardType:
```scala
case class Primitive[A](standardType: StandardType[A]) extends Schema[A]
```

Primitive values are represented using the `Primitive[A]` type class and represent the elements, that we cannot further define through other means. If we visualize our data structure as a tree, primitives are the leaves

ZIO Schema provides a number of built-in primitive types, that we can use to represent our data. These can be found in the StandardType companion-object:

```scala

sealed trait StandardType[A]
object StandardType {
  implicit object UnitType   extends StandardType[Unit]
  implicit object StringType extends StandardType[String]
  implicit object BoolType   extends StandardType[Boolean]
  // ...
}
```


Inside Schema's companion object, we have an implicit conversion from StandardType[A] to Schema[A]:

```scala    
  implicit def primitive[A](implicit standardType: StandardType[A]): Schema[A] =
    Primitive(standardType, Chunk.empty)
```
So we can easily create a Schema for a primitive type A either by calling Schema.`primitive[A]` or by calling `Schema.apply[A]`:
```scala
val intSchema1: Schema[Int] = Schema[Int]
val intSchema2: Schema[Int] = Schema.primitive[Int]
```

A Schema like your database Schema describes the structure of your data

[schema](https://zio.dev/zio-schema/basic-building-blocks/#map)
## Optics
It is nothing more than a data type that models as a value the concept of a field(lens) and the concept of some number of terms of an enumeration(prism). A prism reifies the concept of selecting one these terms. a traversal reifies the concept of selecting all the elements of a collection



```scala

lazy val `tapir-anyof` = module
  .settings(scalacOptions += "-Ymacro-annotations")
  .settings(libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.9.10")
  .settings(libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.10")
  .settings(libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.1" % Test)
  .settings(libraryDependencies += "com.alejandrohdezma" %% "http4s-munit" % "0.15.1" % Test)
  .settings(libraryDependencies += "io.circe" %% "circe-generic-extras" % "0.14.3" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-circe" % "0.23.25" % Test)
  .settings(libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-derevo" % "1.9.10" % Test)
  .settings(libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.9.10" % Test)
  .settings(libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.9.10" % Test)
  .settings(libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % "1.9.10" % Test)
  .settings(libraryDependencies += "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml" % "0.7.4" % Test)
  .settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full)) 

```
  