package org.mulesoft.als.server.lsp4j.extension;

public class AlsServerCapabilities {

    private SerializationServerOptions serialization;

    private CleanDiagnosticTreeServerOptions cleanDiagnosticTree;

    public void setSerialization(SerializationServerOptions serialization) {
        this.serialization = serialization;
    }

    public void setCleanDiagnosticTree(CleanDiagnosticTreeServerOptions cleanDiagnosticTree) {
        this.cleanDiagnosticTree = cleanDiagnosticTree;
    }

    public SerializationServerOptions getSerialization() {
        return serialization;
    }

    public CleanDiagnosticTreeServerOptions getCleanDiagnosticTree() {
        return cleanDiagnosticTree;
    }
}
