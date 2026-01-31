package io.github.broot5.komicinfo.internal

internal class BiDirectionalEnumMapper<M, X>(
    private val modelToXml: Map<M, X>,
    private val defaultXml: X,
) {
  private val xmlToModel: Map<X, M> = modelToXml.entries.associate { (k, v) -> v to k }

  fun toXml(model: M?): X = model?.let { modelToXml[it] } ?: defaultXml

  fun toModel(xml: X?): M? = xml?.let { xmlToModel[it] }
}
