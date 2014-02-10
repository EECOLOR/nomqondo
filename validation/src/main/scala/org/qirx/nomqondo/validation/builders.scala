package org.qirx.nomqondo.validation

import scala.annotation.implicitNotFound

import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.Violation
import org.qirx.nomqondo.validation.mappings._

import shapeless.Generic
import shapeless.HList
import shapeless.Poly1
import shapeless.ops.function.FnToProduct
import shapeless.ops.hlist.Mapper

object builders extends Converters with Methods

trait Converters {
  /**
   * This trait represents a converter from the given type to a mapping
   */
  trait MappingConverter[T] {
    type MappingType

    def convert(m: T): MappingType
  }

  object MappingConverter extends LowerPriorityImplicits {

    type Aux[T, M] = MappingConverter[T] {
      type MappingType = M
    }

    implicit def forFunction[I, O, V <: Violation, R](
      implicit ev: (I => R) => (I => Result[O, V])) =
      new MappingConverter[I => R] {
        type MappingType = ValueMapping[I, O, V]

        def convert(m: I => R) = new ValueMapping(m)
      }

    implicit def forKeyMapping[K, I, O, M](
      implicit ev: M <:< KeyMapping[K, I, O]) =
      new MappingConverter[M] {
        type MappingType = M

        def convert(m: M) = m
      }

    implicit def forKeyTreeMapping[K, M, I, O, R](
      implicit converter: MappingConverter.Aux[M, R],
      keyIsNoMapping: NoMappingConverterFor[K],
      resultIsMapping: R <:< Mapping[DataProviderTree[K, I], O, _ <: Violation]) =
      new MappingConverter[(K, M)] {
        type MappingType = KeyTreeMapping[K, I, O]

        def convert(m: (K, M)) = {
          val (key, mapping) = m
          new KeyTreeMapping(key, converter.convert(mapping))
        }
      }

  }

  trait LowerPriorityImplicits {

    implicit def forMapping[I, O, V <: Violation, M](
      implicit ev: M <:< Mapping[I, O, V]) =
      new MappingConverter[M] {
        type MappingType = M

        def convert(m: M) = m
      }

    implicit def forKeyValueMapping[K, M, I, O, V <: Violation, R](
      implicit converter: MappingConverter.Aux[M, R],
      keyIsNoMapping: NoMappingConverterFor[K],
      resultIsMapping: R <:< Mapping[I, O, V]) =
      new MappingConverter[(K, M)] {
        type MappingType = KeyValueMapping[K, I, O]

        def convert(m: (K, M)) = {
          val (key, mapping) = m
          new KeyValueMapping(key, converter.convert(mapping))
        }
      }

    implicit def forMultiArgumentFunction[F, L <: HList, R, M](
      implicit toSingleArgumentFunction: FnToProduct.Aux[F, L => R],
      converter: MappingConverter.Aux[L => R, M]) =
      new MappingConverter[F] {
        type MappingType = M

        def convert(m: F) =
          converter.convert(toSingleArgumentFunction(m))
      }
  }

  /**
   * Helper trait to ensure that the given type can not be converted
   * to a mapping
   */
  trait NoMappingConverterFor[K]

  object NoMappingConverterFor {

    private def noMappingConverter[M] =
      new MappingConverter[M] {
        type MappingType = NoMappingConverterFor[M]
        def convert(m: M) = ???
      }

    implicit def not[M, R](
      implicit converter: MappingConverter.Aux[M, R] = noMappingConverter[M],
      ev: R =:= NoMappingConverterFor[M]) =
      new NoMappingConverterFor[M] {}
  }
}

trait Methods { self: Converters =>

  /**
   * Constructs a mapping for a given type if it can find an apropriate converter
   */
  def mapping[MappingType, Out](m: MappingType)(
    implicit converter: MappingConverter.Aux[MappingType, Out]): Out =
    converter.convert(m)

  /**
   * Constructs a tree mapping. This method will only accept key -> value
   * instances. Note that the key types should be the same as well as
   * the input types.
   *
   * The reason for this is that the data provider tree is invariant in it's
   * keys and values.
   *
   * The process of implicits is as follows:
   *
   * 1. Create an HList of the incoming product (for example a tuple)
   * 2. Map the contents of the HList to key mappings
   * 3. Determine the key and input type from that HList
   * 4. Apply the data provider to the key mappings
   */
  def mappings[K, I, P <: Product, T <: HList, O <: HList, M, KeyMappings <: HList, MappingsWithoutOutput <: HList](p: P)(
    implicit gen: Generic.Aux[P, T],
    converter: Mapper.Aux[toKeyMapping.type, T, KeyMappings],
    infer: KeyAndInputTypesFrom[KeyMappings, K, I],
    toResultFolder: TreeMapping.mappingToResult.Folder[K, I, KeyMappings, O]) =
    new TreeMapping(gen.to(p).map(toKeyMapping))

  /**
   * Type level method to convert an arbitrary HList to an HList of mappings
   */
  object toKeyMapping extends Poly1 {
    implicit def caseKeyMapping[M, R, K, I, O](
      implicit converter: MappingConverter.Aux[M, R],
      ev: R <:< KeyMapping[K, I, O]) =
      at[M] { m =>
        ev(converter convert m)
      }
  }

  /**
   * Helper class to add a method for easy creation of an object mapping.
   */
  implicit class TreeMappingOps[K, I, TreeResultsType <: HList](treeMapping: TreeMapping[K, I, _, TreeResultsType]) {

    def to[Values <: HList, O, M, MappingResultType](mapping: M)(
      implicit converter: MappingConverter.Aux[M, MappingResultType],
      ev: MappingResultType <:< ValueMapping[Values, O, Violation],
      sequenceResultFolder: ObjectMapping.sequenceResult.Folder[TreeResultsType, Values]) =
      new ObjectMapping(treeMapping, converter.convert(mapping))
  }

  /**
   * Turns an ordinary multi-argument function into a mapping
   */
  def asMapping[F, L <: HList, R, O](f: F)(
    implicit toSingleArgumentFunction: FnToProduct.Aux[F, L => R],
    converter: MappingConverter.Aux[L => Result[R,Nothing], O]) = {

    val singleArgumentFunction: (L => R) = toSingleArgumentFunction(f)
    mapping(singleArgumentFunction andThen Result.apply)
  }
}
