package org.qirx.nomqondo.validation.mappings

import scala.annotation.implicitNotFound
import scala.language.existentials
import scala.language.higherKinds

import org.qirx.nomqondo.api.Failure
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.Success
import org.qirx.nomqondo.api.Violation
import org.qirx.nomqondo.validation.DataProviderTree

import shapeless.::
import shapeless.HList
import shapeless.HNil
import shapeless.Poly2
import shapeless.ops.hlist.RightFolder

/**
 * A mapping represents a function from input to a possible output.
 */
sealed trait Mapping[I, O, +V <: Violation] {
  def apply(input: I): Result[O, V]
}

/**
 * Abstract representation of a mapping for a specific key.
 *
 * This is also a mapping that takes a DataProviderTree as input.
 *
 * There is no sense in keeping the type of violations as we introduce
 * special ones for key mappings.
 *
 * The result of a `get` from a data provider returns either a value or
 * a new data provider. Subclasses are used to handle these differences.
 */
sealed abstract class KeyMapping[K, I, O](
  key: K)
  extends Mapping[DataProviderTree[K, I], O, Violation] {

  def apply(input: DataProviderTree[K, I]): Result[O, Violation] = {
    val possibleKey = input.get(key)
    possibleKey
      .map(toResult andThen wrapResultIfFailure)
      .getOrElse(expectedKeyFailure(key))
  }

  private def expectedKeyFailure(key: K) =
    Failure(KeyViolation(key, ExpectedKey))

  protected type ToResultType = Either[I, DataProviderTree[K, I]] => Result[O, Violation]
  protected val toResult: ToResultType

  private def wrapResultIfFailure(result: Result[O, Violation]) =
    result match {
      case Failure(violations) => Failure(KeyViolation(key, violations))
      case success => success
    }
}

/**
 * Specialized key mapping for mappings that can map from input to output.
 *
 * Types of key, input and output are inferred from key and mapping.
 */
class KeyValueMapping[K, I, O](
  key: K,
  mapping: => MappingType forSome {
    type MappingType <: Mapping[I, O, _ <: Violation]
  })
  extends KeyMapping[K, I, O](key) {

  protected val toResult: ToResultType = {
    case Left(value) => mapping(value)
    case Right(dataProvider) => Failure(ExpectedValue)
  }
}

/**
 * Specialized key mapping for mappings that can map from data provider to output.
 *
 * Types of key, input and output are inferred from key and mapping.
 */
class KeyTreeMapping[K, I, O](
  key: K,
  mapping: => MappingType forSome {
    type MappingType <: Mapping[DataProviderTree[K, I], O, _ <: Violation]
  })
  extends KeyMapping[K, I, O](key) {

  protected val toResult: ToResultType = {
    case Left(value) => Failure(ExpectedDataProviderTree)
    case Right(dataProvider) => mapping(dataProvider)
  }
}

/**
 * Represents an mapping from input to output
 *
 * Types of key, input and output are inferred from mapping function.
 */
class ValueMapping[I, O, +V <: Violation](
  mapping: I => R forSome { type R <: Result[O, V] })
  extends Mapping[I, O, V] {

  def apply(input: I): Result[O, V] = mapping(input)
}

/**
 * A specialized mapping for trees. Trees are typed HLists that contain mappings.
 *
 * The complex implicit allows the result to be known (type-wise) and
 * also infers the type of key, input and output.
 */
class TreeMapping[K, I, KeyMappings <: HList, O <: HList](
  mappings: KeyMappings)(
    implicit infer: KeyAndInputTypesFrom[KeyMappings, K, I],
    toResultFolder: TreeMapping.mappingToResult.Folder[K, I, KeyMappings, O])
  extends Mapping[DataProviderTree[K, I], O, Nothing] {

  def apply(dataProvider: DataProviderTree[K, I]): Result[O, Nothing] = {
    val acc: HNil = HNil
    val (_, results) = mappings.foldRight(dataProvider -> acc)(TreeMapping.mappingToResult)
    Result(results)
  }
}

object TreeMapping {

  /**
   * Used to fold an HList of mappings into an HList of results
   */
  object mappingToResult extends Poly2 {
    @implicitNotFound("Could not find the correct folder to convert a mappings in ${InputList} to results using a DataProvider[${K}, ${I}].")
    type Folder[K, I, InputList <: HList, OutputList <: HList] =
      RightFolder.Aux[InputList, (DataProviderTree[K, I], HNil), this.type, (DataProviderTree[K, I], OutputList)]

    implicit def all[K, I, OutputType, M, Acc <: HList](
      implicit ev: M <:< KeyMapping[K, I, OutputType]) =
      at[M, (DataProviderTree[K, I], Acc)] { (mapping, dataProviderAndAcc) =>
        val (dataProvider, acc) = dataProviderAndAcc
        (dataProvider, mapping(dataProvider) :: acc)
      }
  }
}

/**
 * A specialized mapping that allows you to take a list of mappings and use
 * them as an input for another mapping. This allows you to construct objects.
 */
class ObjectMapping[K, I, TreeResultsType <: HList, Values <: HList, O](
  mappings: TreeMapping[K, I, _, TreeResultsType],
  mapping: => ValueMapping[Values, O, Violation])(
    implicit sequenceResultFolder: ObjectMapping.sequenceResult.Folder[TreeResultsType, Values])
  extends Mapping[DataProviderTree[K, I], O, Violation] {

  def apply(dataProvider: DataProviderTree[K, I]): Result[O, Violation] = {
    val treeResults = mappings.apply(dataProvider)

    treeResults
      .flatMap { treeResults =>
        val acc: Result[HNil, Nothing] = Success(HNil)
        treeResults.foldRight(acc)(ObjectMapping.sequenceResult)
      }
      .flatMap(mapping.apply)
  }
}

object ObjectMapping {

  /**
   * Converts an HList[Result[...]] into a Result[HList[...]]
   */
  object sequenceResult extends Poly2 {

    @implicitNotFound("Could not find the correct folder to convert an HList of Result to a Result of an HList.")
    type Folder[L <: HList, O <: HList] = RightFolder.Aux[L, Result[HNil, Nothing], this.type, Result[O, Violation]]

    implicit def x[O, Acc <: HList, V <: Violation] =
      at[Result[O, Violation], Result[Acc, V]] { (elem, acc) =>
        val result: Result[O :: Acc, Violation] =
          elem match {
            case Success(value) => acc.map { acc => value :: acc }
            case Failure(violations) =>
              val allViolations =
                acc match {
                  case Failure(accViolations) => violations ++ accViolations
                  case _ => violations
                }
              Failure(allViolations)
          }

        result
      }
  }
}
