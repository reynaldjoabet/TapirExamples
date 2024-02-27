// class CompanyController private (companyService: CompanyService, jwtService: JWTService)
//     extends BaseController
//     with CompanyEndpoints {

//   val create: ServerEndpoint[Any, Task] = createEndpoint
//     .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
//     .serverLogic(_ => req => companyService.create(req).either)

//   val getAll: ServerEndpoint[Any, Task] = getAllEndpoint.serverLogic(_ => companyService.getAll.either)

//   val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogic { id =>
//     ZIO
//       .attempt(id.toLong)
//       .flatMap(companyService.getById)
//       .catchSome { case _: NumberFormatException =>
//         companyService.getBySlug(id)
//       }
//       .either
//   }

//   override val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, getById)
// }

// object CompanyController {
//   val makeZIO: URIO[CompanyService & JWTService, CompanyController] =
//     for {
//       reviewService <- ZIO.service[CompanyService]
//       jwtService    <- ZIO.service[JWTService]
//     } yield CompanyController(reviewService, jwtService)
// }
