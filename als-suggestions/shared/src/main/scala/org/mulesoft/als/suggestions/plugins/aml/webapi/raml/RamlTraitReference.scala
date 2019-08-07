package org.mulesoft.als.suggestions.plugins.aml.webapi.raml

import amf.core.model.domain.DomainElement
import amf.core.model.domain.templates.ParametrizedDeclaration
import amf.plugins.domain.webapi.metamodel.templates.TraitModel
import amf.plugins.domain.webapi.models.Operation
import amf.plugins.domain.webapi.models.templates.ParametrizedTrait
import org.mulesoft.als.common.YPartBranch
import org.yaml.model.YSequence

object RamlTraitReference extends RamlAbstractDeclarationReference {

  override def id: String = "RamlTraitReference"

  override def entryKey: String = "is"

  override def iriDeclaration: String = TraitModel.`type`.head.iri()

  override protected def isValue(yPartBranch: YPartBranch): Boolean =
    yPartBranch.isValue || yPartBranch.parent.exists(_.isInstanceOf[YSequence])

  override val elementClass: Class[_ <: DomainElement]                       = classOf[Operation]
  override val abstractDeclarationClass: Class[_ <: ParametrizedDeclaration] = classOf[ParametrizedTrait]
}
