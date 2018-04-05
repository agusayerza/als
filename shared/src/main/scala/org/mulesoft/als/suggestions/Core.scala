package org.mulesoft.als.suggestions

import org.mulesoft.als.suggestions.interfaces.Syntax
import org.mulesoft.als.suggestions.interfaces.Syntax._
import org.mulesoft.als.suggestions.plugins.StructureCompletionPlugin
import org.mulesoft.als.suggestions.plugins.oas.{DefinitionReferenceCompletionPlugin, OasDeclarationReferencePlugin}
import org.mulesoft.als.suggestions.plugins.raml._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

object Core {
    def init():Future[Unit] = org.mulesoft.high.level.Core.init().map(x => {
        CompletionPluginsRegistry.registerPlugin(StructureCompletionPlugin());
        CompletionPluginsRegistry.registerPlugin(TemplateReferencesCompletionPlugin());
        CompletionPluginsRegistry.registerPlugin(OasDeclarationReferencePlugin());
        CompletionPluginsRegistry.registerPlugin(DefinitionReferenceCompletionPlugin());
        //TODO: uncoment after fs access fixes
        //CompletionPluginsRegistry.registerPlugin(MasterReferenceCompletionPlugin());
        CompletionPluginsRegistry.registerPlugin(TypeReferenceCompletionPlugin());
        CompletionPluginsRegistry.registerPlugin(AnnotationReferencesCompletionPlugin());
        CompletionPluginsRegistry.registerPlugin(FacetsCompletionPlugin());
    });
    
    def prepareText(text:String, offset:Int, syntax:Syntax):String = {
        var isJSON = text.trim.startsWith("{")
        if(isJSON){
            CompletionProvider.prepareJsonContent(text,offset);
        }
        else {
            syntax match {
                case YAML => CompletionProvider.prepareYamlContent(text, offset);
                case _ => throw new Error(s"Syntax not supported: $syntax");
            }
        }
    }
}
