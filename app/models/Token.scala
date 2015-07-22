package models

case class Token(id: String,
                 viewerProfileId: String,
                 userId: Option[String] = None,
                 storyIdToShow: Option[String] = None)