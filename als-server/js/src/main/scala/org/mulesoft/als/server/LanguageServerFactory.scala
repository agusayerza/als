package org.mulesoft.als.server

import amf.client.convert.ClientPayloadPluginConverter
import amf.client.plugins.ClientAMFPayloadValidationPlugin
import amf.client.resource.ClientResourceLoader
import org.mulesoft.als.configuration.{
  ClientDirectoryResolver,
  DefaultJsServerSystemConf,
  EmptyJsDirectoryResolver,
  JsServerSystemConf
}
import org.mulesoft.als.server.client.{AlsClientNotifier, ClientNotifier}
import org.mulesoft.als.server.logger.PrintLnLogger
import org.mulesoft.als.server.modules.WorkspaceManagerFactoryBuilder
import org.mulesoft.als.server.modules.diagnostic.DiagnosticNotificationsKind
import org.mulesoft.als.server.protocol.LanguageServer
import org.mulesoft.amfintegration.AmfInstance
import org.yaml.builder.{DocBuilder, JsOutputBuilder}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("LanguageServerFactory")
object LanguageServerFactory {

  def fromLoaders(clientNotifier: ClientNotifier,
                  serializationProps: JsSerializationProps,
                  clientLoaders: js.Array[ClientResourceLoader] = js.Array(),
                  clientDirResolver: ClientDirectoryResolver = EmptyJsDirectoryResolver,
                  logger: js.UndefOr[ClientLogger] = js.undefined,
                  withDiagnostics: Boolean = true,
                  notificationKind: js.UndefOr[DiagnosticNotificationsKind] = js.undefined,
                  amfPlugins: js.Array[ClientAMFPayloadValidationPlugin] = js.Array.apply()): LanguageServer = {
    fromSystemConfig(clientNotifier,
                     serializationProps,
                     JsServerSystemConf(clientLoaders, clientDirResolver),
                     amfPlugins,
                     logger,
                     withDiagnostics,
                     notificationKind)
  }

  def fromSystemConfig(clientNotifier: ClientNotifier,
                       serializationProps: JsSerializationProps,
                       jsServerSystemConf: JsServerSystemConf = DefaultJsServerSystemConf,
                       plugins: js.Array[ClientAMFPayloadValidationPlugin] = js.Array(),
                       logger: js.UndefOr[ClientLogger] = js.undefined,
                       withDiagnostics: Boolean = true,
                       notificationKind: js.UndefOr[DiagnosticNotificationsKind] = js.undefined): LanguageServer = {

    val factory =
      new WorkspaceManagerFactoryBuilder(clientNotifier,
                                         logger.toOption.map(l => ClientLoggerAdapter(l)).getOrElse(PrintLnLogger),
                                         jsServerSystemConf.environment)
        .withAmfConfiguration(new AmfInstance(plugins.toSeq.map(ClientPayloadPluginConverter.convert),
                                              jsServerSystemConf.platform,
                                              jsServerSystemConf.environment))
        .withPlatform(jsServerSystemConf.platform)
        .withDirectoryResolver(jsServerSystemConf.directoryResolver)

    notificationKind.toOption.foreach(factory.withNotificationKind)

    val diagnosticManager     = factory.diagnosticManager()
    val filesInProjectManager = factory.filesInProjectManager(serializationProps.alsClientNotifier)
    val serializationManager  = factory.serializationManager(serializationProps)

    val builders = factory.buildWorkspaceManagerFactory()
    val languageBuilder =
      new LanguageServerBuilder(builders.documentManager,
                                builders.workspaceManager,
                                builders.configurationManager,
                                builders.resolutionTaskManager)
        .addInitializableModule(serializationManager)
        .addInitializableModule(filesInProjectManager)
        .addInitializable(builders.cleanDiagnosticManager)
        .addInitializable(builders.workspaceManager)
        .addInitializable(builders.resolutionTaskManager)
        .addInitializable(builders.configurationManager)
        .addRequestModule(builders.cleanDiagnosticManager)
        .addRequestModule(builders.completionManager)
        .addRequestModule(builders.conversionManager)
        .addRequestModule(builders.structureManager)
        .addRequestModule(builders.definitionManager)
        .addRequestModule(builders.typeDefinitionManager)
        .addRequestModule(builders.hoverManager)
        .addRequestModule(builders.implementationManager)
        .addRequestModule(builders.referenceManager)
        .addRequestModule(builders.fileUsageManager)
        .addRequestModule(builders.documentLinksManager)
        .addRequestModule(builders.renameManager)
        .addRequestModule(builders.documentHighlightManager)
        .addRequestModule(builders.foldingRangeManager)
        .addRequestModule(builders.selectionRangeManager)
        .addRequestModule(builders.renameFileActionManager)
        .addRequestModule(builders.codeActionManager)
        .addRequestModule(builders.documentFormattingManager)
        .addRequestModule(builders.documentRangeFormattingManager)
        .addInitializable(builders.telemetryManager)
    diagnosticManager.foreach(languageBuilder.addInitializableModule)
    builders.serializationManager.foreach(languageBuilder.addRequestModule)
    languageBuilder
      .build()
  }
}

@JSExportAll
@JSExportTopLevel("JsSerializationProps")
case class JsSerializationProps(override val alsClientNotifier: AlsClientNotifier[js.Any])
    extends SerializationProps[js.Any](alsClientNotifier) {
  override def newDocBuilder(): DocBuilder[js.Any] = JsOutputBuilder()
}
