package org.mulesoft.als.client.lsp.feature.telemetry

import org.mulesoft.lsp.feature.telemetry.TelemetryMessage

import scala.scalajs.js

@js.native
trait ClientTelemetryMessage extends js.Object {
  def event: String = js.native
  def messageType: Int = js.native
  def message: String = js.native
  def uri: String = js.native
  def time: String = js.native
  def uuid: String = js.native
}

object ClientTelemetryMessage {
  def apply(internal: TelemetryMessage): ClientTelemetryMessage =
    js.Dynamic
      .literal(
        event = internal.event,
        messageType = internal.messageType,
        message = internal.message,
        uri = internal.uri,
        time = internal.time.toString,
        uuid = internal.uuid,
      )
      .asInstanceOf[ClientTelemetryMessage]
}

