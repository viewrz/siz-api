package models

case class Token(id: String,
                 viewerProfileId: String,
                 userId: Option[String] = None,
                 lastSeenIp: Option[String] = None,
                 lastSeenStoryId: Option[String] = None)