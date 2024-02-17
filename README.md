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