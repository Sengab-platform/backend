package models

import play.api.libs.json.Json

case class contributors_gender(
                                male: Int,
                                female: Int,
                                unknown: Int

                              )

object contributors_gender {
  implicit val ContributorsGenderFormat = Json.format[contributors_gender]
}

case class Stats(
                  enrollments_count: Int,
                  contributions_count: Int,
                  contributors_gender: contributors_gender) {
}

object Stats {
  implicit val StatsFormat = Json.format[Stats]
}
