package org.mulesoft.als.suggestions.plugins.aml.webapi.raml

import amf.core.model.domain.DomainElement
import amf.core.model.domain.templates.ParametrizedDeclaration
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorDeclaration
import org.mulesoft.als.common.YPartBranch
import org.mulesoft.als.suggestions.RawSuggestion
import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.als.suggestions.plugins.aml.AMLRamlStyleDeclarationsReferences
import org.yaml.model.{YMapEntry, YNode}

import scala.concurrent.Future

trait RamlAbstractDeclarationReference extends AMLCompletionPlugin {

  val elementClass: Class[_ <: DomainElement]
  val abstractDeclarationClass: Class[_ <: ParametrizedDeclaration]
  val errorDeclarationClass: Class[_ <: ErrorDeclaration]

  def entryKey: String
  def iriDeclaration: String

  override def resolve(params: AmlCompletionRequest): Future[Seq[RawSuggestion]] = {
    Future.successful(
      if ((elementClass.isInstance(params.amfObject)
          || abstractDeclarationClass.isInstance(params.amfObject) || errorDeclarationClass.isInstance(
            params.amfObject))
          && isTypeDef(params.yPartBranch)) {

        val brothers = getBrothers(params)

        val suggestions =
          new AMLRamlStyleDeclarationsReferences(Seq(iriDeclaration),
                                                 stringValue(params.yPartBranch),
                                                 params.declarationProvider,
                                                 None).resolve().filter(r => !brothers.contains(r.newText))

        if (params.yPartBranch.isKey)
          suggestions.map(s => s.copy(options = s.options.copy(isKey = true), whiteSpacesEnding = params.indentation))
        else suggestions
      } else Nil)

  }

  private def getBrothers(params: AmlCompletionRequest): Seq[String] = {
    val element =
      if (elementClass.isInstance(params.amfObject))
        Some(params.amfObject.asInstanceOf[DomainElement])
      else if (abstractDeclarationClass.isInstance(params.amfObject))
        params.branchStack.headOption.collectFirst({
          case e if elementClass.isInstance(e) => e.asInstanceOf[DomainElement]
        })
      else None

    element.map(_.extend).getOrElse(Nil).collect({ case pm: ParametrizedDeclaration => pm }).flatMap(_.name.option())
  }

  private def isTypeDef(yPartBranch: YPartBranch) =
    isValueInType(yPartBranch) || isKeyInTypeMap(yPartBranch)

  protected def isValue(yPartBranch: YPartBranch): Boolean

  /**
    * /endpoint:
    *   type: * || type: res*
    *case type 1 is object enpoint other cases are parametrized declaration parser
    */
  private def isValueInType(yPartBranch: YPartBranch) =
    isValue(yPartBranch) && yPartBranch.parentEntryIs(entryKey)

  /**
    * /endpoint:
    *   type:
    *     res*
    */
  private def isKeyInTypeMap(yPartBranch: YPartBranch): Boolean =
    yPartBranch.isKey && yPartBranch
      .ancestorOf(classOf[YMapEntry])
      .exists(_.key.asScalar.exists(_.text == entryKey))

  private def stringValue(yPart: YPartBranch) = {
    yPart.node match {
      case n: YNode => n.asScalar.map(_.text).getOrElse("")
      case _        => ""
    }
  }
}
