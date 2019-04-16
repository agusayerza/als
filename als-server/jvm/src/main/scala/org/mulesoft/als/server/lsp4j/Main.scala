package org.mulesoft.als.server.lsp4j

import java.net.ServerSocket

import org.eclipse.lsp4j.launch.LSPLauncher
import org.mulesoft.als.server.client.ClientConnection
import org.mulesoft.als.server.logger.{Logger, PrintLnLogger}

object Main {
  def main(args: Array[String]): Unit = {
//    val o = new PrintStream(new File(s"${System.getProperty("user.home")}/Downloads/log.txt"))
//    System.setOut(o)
//    System.setErr(o)
    System.out.println("This will be written to the text file")

    val port = if (args.length >= 1) args(0) else "4000"
    try {
      val serverSocket = new ServerSocket(port.toInt)
      do {
        val socket = serverSocket.accept()
        System.out.println("Incomming connection")

        val in  = socket.getInputStream
        val out = socket.getOutputStream

        val logger: Logger   = PrintLnLogger
        val clientConnection = ClientConnection(logger)
        val server           = new LanguageServerImpl(LanguageServerFactory.alsLanguageServer(clientConnection, logger))

        val workspaceService = new WorkspaceServiceImpl()

        val launcher = LSPLauncher.createServerLauncher(server, in, out)
        val client   = launcher.getRemoteProxy
        clientConnection.connect(LanguageClientWrapper(client))
        launcher.startListening
        println("ALS started")
      } while (true)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }
}
