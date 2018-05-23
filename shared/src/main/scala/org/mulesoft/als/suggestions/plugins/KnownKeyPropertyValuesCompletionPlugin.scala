package org.mulesoft.als.suggestions.plugins

import amf.core.remote.{Oas, Raml10, Vendor}
import org.mulesoft.als.suggestions.implementation.Suggestion
import org.mulesoft.als.suggestions.interfaces._
import org.mulesoft.typesystem.nominal_interfaces.IArrayType
import org.mulesoft.typesystem.nominal_interfaces.extras.PropertySyntaxExtra

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Future, Promise}

class KnownKeyPropertyValuesCompletionPlugin extends ICompletionPlugin {

    override def id: String = KnownKeyPropertyValuesCompletionPlugin.ID

    override def languages: Seq[Vendor] = StructureCompletionPlugin.supportedLanguages
    
    override def isApplicable(request: ICompletionRequest): Boolean = if(request.astNode.isEmpty || !request.astNode.get.isElement) {
        false;
    } else if(request.actualYamlLocation.isEmpty) {
        false;
    } else if(request.kind != LocationKind.KEY_COMPLETION) {
        false;
    } else if(request.actualYamlLocation.isEmpty || request.yamlLocation.isEmpty) {
        false;
    } else {
        request.actualYamlLocation.get.parentStack.nonEmpty && request.actualYamlLocation.get.parentStack.last.hasSameValue(request.yamlLocation.get);
    }
    
    override def suggest(request: ICompletionRequest): Future[Seq[ISuggestion]] = {
        var result:ListBuffer[ISuggestion] = ListBuffer()
        var isYAML = request.config.astProvider.get.syntax == Syntax.YAML
        var postfix = if(isYAML) ":" else ""
        request.astNode.get.asElement.get.definition.allProperties.foreach(p=>{
            var isEmbeddedInMaps = false
            p.getExtra(PropertySyntaxExtra).foreach(extra=>{
                isEmbeddedInMaps = extra.isEmbeddedInMaps
            })
            if(p.isMultiValue && !isEmbeddedInMaps){
                p.range.foreach(range=>{
                    if(range.isArray && range.asInstanceOf[IArrayType].componentType.isDefined) {
                        range.asInstanceOf[IArrayType].componentType.get.properties.foreach(p => {
                            p.getExtra(PropertySyntaxExtra).foreach(extra => {
                                if (extra.isKey) {
                                    extra.enum.foreach(x => {
                                        var text = x.toString
                                        result += Suggestion(text + postfix, id, text, request.prefix)
                                    })
                                }
                            })
                        })
                    }
                })
            }
        })
        result
        Promise.successful(result).future
    }
 }

object KnownKeyPropertyValuesCompletionPlugin {
    val ID = "known.key.property.values.completion";

    val supportedLanguages:List[Vendor] = List(Raml10, Oas);

    def apply():KnownKeyPropertyValuesCompletionPlugin = new KnownKeyPropertyValuesCompletionPlugin();
}

