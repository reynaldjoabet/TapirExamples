package domain
package data

import sttp.tapir.Schema
final case class Country(code: CountryCode, name: String)

object Country {

  implicit class ModelToView(model: Country) {

    def toView(): CountryView =
      CountryView(model.code, model.name)

  }

  implicit class ModelSeqToView(models: Seq[Country]) {

    def toView(): Seq[CountryView] =
      models.map(_.toView())

  }

  implicit val schema: Schema[Country] = Schema.derived[Country]

}
